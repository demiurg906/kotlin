/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.cfg.pseudocodeTraverser

import org.jetbrains.kotlin.cfg.ControlFlowInfo
import org.jetbrains.kotlin.cfg.pseudocode.Pseudocode
import org.jetbrains.kotlin.cfg.pseudocode.instructions.Instruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.InlinedLocalFunctionDeclarationInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.LocalFunctionDeclarationInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.SubroutineEnterInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.SubroutineSinkInstruction
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.TraversalOrder.FORWARD
import java.util.*

enum class LocalFunctionAnalysisStrategy {
    ANALYSE_EVERYTHING {
        override val isolatedLocalFunctions = false
        override fun allowFunction(declaration: LocalFunctionDeclarationInstruction) = true
    },

//    DO_NOT_ANALYSE {
//        override fun allowFunction(declaration: LocalFunctionDeclarationInstruction): Boolean = false
//        override val isolatedLocalFunctions: Boolean = TODO("Not implemented")
//    },

    ONLY_INLINED_LAMBDAS {
        override val isolatedLocalFunctions = true

        override fun allowFunction(declaration: LocalFunctionDeclarationInstruction): Boolean {
            return declaration is InlinedLocalFunctionDeclarationInstruction
        }
    };

    abstract val isolatedLocalFunctions: Boolean
    abstract fun allowFunction(declaration: LocalFunctionDeclarationInstruction): Boolean
}

fun Pseudocode.traverse(
    traversalOrder: TraversalOrder,
    analyzeInstruction: (Instruction) -> Unit
) {
    val instructions = getInstructions(traversalOrder)
    for (instruction in instructions) {
        if (instruction is LocalFunctionDeclarationInstruction) {
            instruction.body.traverse(traversalOrder, analyzeInstruction)
        }
        analyzeInstruction(instruction)
    }
}

fun <D> Pseudocode.traverse(
    traversalOrder: TraversalOrder,
    edgesMap: Map<Instruction, Edges<D>>,
    analyzeInstruction: (Instruction, D, D) -> Unit
) {
    val instructions = getInstructions(traversalOrder)
    for (instruction in instructions) {
        if (instruction is LocalFunctionDeclarationInstruction) {
            instruction.body.traverse(traversalOrder, edgesMap, analyzeInstruction)
        }
        val edges = edgesMap[instruction]
        if (edges != null) {
            analyzeInstruction(instruction, edges.incoming, edges.outgoing)
        }
    }
}

fun <I : ControlFlowInfo<*, *, *>> Pseudocode.collectData(
    traversalOrder: TraversalOrder,
    mergeEdges: (Instruction, Collection<I>) -> Edges<I>,
    combineEdges: (Instruction, Collection<I>) -> Edges<I>,
    updateEdge: (Instruction, Instruction, I, AdditionalControlFlowInfo) -> I,
    initialInfo: I,
    localFunctionAnalysisStrategy: LocalFunctionAnalysisStrategy
): Map<Instruction, Edges<I>> {
    val edgesMap = LinkedHashMap<Instruction, Edges<I>>()
    edgesMap.put(getStartInstruction(traversalOrder), Edges(initialInfo, initialInfo))

    val changed = mutableMapOf<Instruction, Boolean>()
    var step = 0
    do {
        collectDataFromSubgraph(
            traversalOrder, edgesMap,
            mergeEdges, combineEdges, updateEdge, Collections.emptyList(), changed, false,
            localFunctionAnalysisStrategy, step
        )
        step += 1
    } while (changed.any { it.value })

    return edgesMap
}

data class AdditionalControlFlowInfo(val stepNumber: Int, val direction: UpdatedEdgeDirection)

enum class UpdatedEdgeDirection {
    INCOMING, OUTGOING
}

private fun <I : ControlFlowInfo<*, *, *>> Pseudocode.collectDataFromSubgraph(
    traversalOrder: TraversalOrder,
    edgesMap: MutableMap<Instruction, Edges<I>>,
    mergeEdges: (Instruction, Collection<I>) -> Edges<I>,
    combineEdges: (Instruction, Collection<I>) -> Edges<I>,
    updateEdge: (Instruction, Instruction, I, AdditionalControlFlowInfo) -> I,
    previousSubGraphInstructions: Collection<Instruction>,
    changed: MutableMap<Instruction, Boolean>,
    isLocal: Boolean,
    localFunctionAnalysisStrategy: LocalFunctionAnalysisStrategy,
    stepNumber: Int
) {
    val instructions = getInstructions(traversalOrder)
    val startInstruction = getStartInstruction(traversalOrder)

    for (instruction in instructions) {
        val isStart = instruction.isStartInstruction(traversalOrder)
        if (!isLocal && isStart)
            continue

        val previousInstructions = if (isStart && isLocal && localFunctionAnalysisStrategy.isolatedLocalFunctions)
            listOf()
        else getPreviousIncludingSubGraphInstructions(instruction, traversalOrder, startInstruction, previousSubGraphInstructions)

        if (instruction is LocalFunctionDeclarationInstruction && localFunctionAnalysisStrategy.allowFunction(instruction)) {
            val subroutinePseudocode = instruction.body
            subroutinePseudocode.collectDataFromSubgraph(
                traversalOrder, edgesMap, mergeEdges, combineEdges, updateEdge, previousInstructions, changed, true,
                localFunctionAnalysisStrategy, stepNumber
            )
            // Special case for inlined functions: take flow from EXIT instructions (it contains flow which exits declaration normally)
            val lastInstruction = if (instruction is InlinedLocalFunctionDeclarationInstruction && traversalOrder == FORWARD)
                subroutinePseudocode.exitInstruction
            else
                subroutinePseudocode.getLastInstruction(traversalOrder)
            val previousValue = edgesMap[instruction]
            val newValue = edgesMap[lastInstruction]
            val updatedValue = newValue?.let {
                Edges(
                    updateEdge(
                        lastInstruction,
                        instruction,
                        it.incoming,
                        AdditionalControlFlowInfo(stepNumber, UpdatedEdgeDirection.INCOMING)
                    ),
                    updateEdge(
                        lastInstruction,
                        instruction,
                        it.outgoing,
                        AdditionalControlFlowInfo(stepNumber, UpdatedEdgeDirection.OUTGOING)
                    )
                )
            }?.let { edges ->
                // if local function was analysed as isolated
                // there is need to merge exit edges of local function
                // and previous edge before function call
                if (localFunctionAnalysisStrategy.isolatedLocalFunctions) {
                    val incoming = previousInstructions.mapNotNull { edgesMap[it]?.outgoing }.toMutableList()
                    incoming.add(edges.outgoing)
                    combineEdges(instruction, incoming)
                } else {
                    edges
                }
            }

            updateEdgeDataForInstruction(instruction, previousValue, updatedValue, edgesMap, changed)
            continue
        }


        val previousDataValue = edgesMap[instruction]
        if (previousDataValue != null && previousInstructions.all { changed[it] == false }) {
            changed[instruction] = false
            continue
        }

        val incomingEdgesData = HashSet<I>()

        for (previousInstruction in previousInstructions) {
            val previousData = edgesMap[previousInstruction]
            if (previousData != null) {
                incomingEdgesData.add(
                    updateEdge(
                        previousInstruction,
                        instruction,
                        previousData.outgoing,
                        AdditionalControlFlowInfo(stepNumber, UpdatedEdgeDirection.OUTGOING)
                    )
                )
            }
        }
        val mergedData = mergeEdges(instruction, incomingEdgesData)
        updateEdgeDataForInstruction(instruction, previousDataValue, mergedData, edgesMap, changed)
    }
}

private fun getPreviousIncludingSubGraphInstructions(
    instruction: Instruction,
    traversalOrder: TraversalOrder,
    startInstruction: Instruction,
    previousSubGraphInstructions: Collection<Instruction>
): Collection<Instruction> {
    val previous = instruction.getPreviousInstructions(traversalOrder)
    if (instruction != startInstruction || previousSubGraphInstructions.isEmpty()) {
        return previous
    }
    val result = ArrayList(previous)
    result.addAll(previousSubGraphInstructions)
    return result
}

private fun <I : ControlFlowInfo<*, *, *>> updateEdgeDataForInstruction(
    instruction: Instruction,
    previousValue: Edges<I>?,
    newValue: Edges<I>?,
    edgesMap: MutableMap<Instruction, Edges<I>>,
    changed: MutableMap<Instruction, Boolean>
) {
    if (previousValue != newValue && newValue != null) {
        changed[instruction] = true
        edgesMap.put(instruction, newValue)
    } else {
        changed[instruction] = false
    }
}

data class Edges<out T>(val incoming: T, val outgoing: T)

enum class TraverseInstructionResult {
    CONTINUE,
    SKIP,
    HALT
}

// returns false when interrupted by handler
fun traverseFollowingInstructions(
    rootInstruction: Instruction,
    visited: MutableSet<Instruction> = HashSet(),
    order: TraversalOrder = FORWARD,
    // true to continue traversal
    handler: ((Instruction) -> TraverseInstructionResult)?
): Boolean {
    val stack = ArrayDeque<Instruction>()
    stack.push(rootInstruction)

    while (!stack.isEmpty()) {
        val instruction = stack.pop()
        if (!visited.add(instruction)) continue
        when (handler?.let { it(instruction) } ?: TraverseInstructionResult.CONTINUE) {
            TraverseInstructionResult.CONTINUE -> instruction.getNextInstructions(order).forEach { stack.push(it) }
            TraverseInstructionResult.SKIP -> {
            }
            TraverseInstructionResult.HALT -> return false
        }
    }
    return true
}

enum class TraversalOrder {
    FORWARD,
    BACKWARD
}

fun Pseudocode.getStartInstruction(traversalOrder: TraversalOrder): Instruction =
    if (traversalOrder == FORWARD) enterInstruction else sinkInstruction

fun Pseudocode.getLastInstruction(traversalOrder: TraversalOrder): Instruction =
    if (traversalOrder == FORWARD) sinkInstruction else enterInstruction

fun Pseudocode.getInstructions(traversalOrder: TraversalOrder): List<Instruction> =
    if (traversalOrder == FORWARD) instructions else reversedInstructions

fun Instruction.getNextInstructions(traversalOrder: TraversalOrder): Collection<Instruction> =
    if (traversalOrder == FORWARD) nextInstructions else previousInstructions

fun Instruction.getPreviousInstructions(traversalOrder: TraversalOrder): Collection<Instruction> =
    if (traversalOrder == FORWARD) previousInstructions else nextInstructions

fun Instruction.isStartInstruction(traversalOrder: TraversalOrder): Boolean =
    if (traversalOrder == FORWARD) this is SubroutineEnterInstruction else this is SubroutineSinkInstruction
