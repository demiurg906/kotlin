/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cfg.effects

import org.jetbrains.kotlin.cfg.ImmutableHashMap
import org.jetbrains.kotlin.cfg.pseudocode.Pseudocode
import org.jetbrains.kotlin.cfg.pseudocode.instructions.BlockScope
import org.jetbrains.kotlin.cfg.pseudocode.instructions.Instruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.eval.CallInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.SubroutineExitInstruction
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.*
import org.jetbrains.kotlin.contracts.FactsEffectSystem
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextFamily
import org.jetbrains.kotlin.contracts.facts.ContextVerifier
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace

class PseudocodeEffectsData(
    val pseudocode: Pseudocode,
    val bindingTrace: BindingTrace
) {
    val bindingContext: BindingContext = bindingTrace.bindingContext

    private val myDiagnostics = mutableListOf<Pair<KtElement, String>>()
    val diagnostics: List<Pair<KtElement, String>>
        get() = myDiagnostics

    val resultingContexts: Map<ContextFamily, List<Context>>? = computeEffectsControlFlowInfo(pseudocode)

    private fun computeEffectsControlFlowInfo(pseudocode: Pseudocode): Map<ContextFamily, List<Context>>? {
        // collect info via CFA
        val edgesMap = pseudocode.collectData(
            traversalOrder = TraversalOrder.FORWARD,
            mergeEdges = ::mergeEdges,
            updateEdge = ::updateEdge,
            initialInfo = ContractsContextsInfo.EMPTY,
            localFunctionAnalysisStrategy = LocalFunctionAnalysisStrategy.ONLY_IN_PLACE_LAMBDAS
        )

        // verify resulting context
        pseudocode.traverse(
            traversalOrder = TraversalOrder.FORWARD,
            edgesMap = edgesMap,
            analyzeInstruction = ::verify
        )

        val contextsGroupedByFamily = edgesMap[pseudocode.exitInstruction]?.incoming?.toMutableMap() ?: return null
        return contextsGroupedByFamily.mapValues { (_, contextsGropedByLevel) -> contextsGropedByLevel.map { it.value } }
    }

    // -------------------------- Verification --------------------------

    private fun verify(instruction: Instruction, incoming: ContractsContextsInfo, outgoing: ContractsContextsInfo) {
        when (instruction) {
            is SubroutineExitInstruction -> verifySubroutineExitInstruction(instruction, incoming)
            is CallInstruction -> verifyCallInstruction(instruction, incoming)
        }
    }

    private fun verifySubroutineExitInstruction(instruction: SubroutineExitInstruction, incoming: ContractsContextsInfo) {
        val lambdaExpression = instruction.subroutine.parent as? KtLambdaExpression ?: return
        val verifiers = FactsEffectSystem.declaredVerifiers(lambdaExpression, bindingContext)
        verifyContext(verifiers, incoming)
    }

    private fun verifyCallInstruction(instruction: CallInstruction, incoming: ContractsContextsInfo) {
        val callExpression = instruction.element as? KtCallExpression ?: return
        val (_, verifiers) = FactsEffectSystem.declaredFactsAndCheckers(callExpression, bindingContext)
        verifyContext(verifiers, incoming)
    }

    private fun verifyContext(verifiers: Collection<ContextVerifier>, info: ContractsContextsInfo) {
        for (verifier in verifiers) {
            val family = verifier.family
            val contextsGroupedByLevel = info[family].getOrElse(mapOf())
            val contexts = contextsGroupedByLevel.values
            verifier.verify(contexts, bindingTrace)
        }
    }

    // -------------------------- Collecting data --------------------------

    private fun updateEdge(
        previousInstruction: Instruction,
        instruction: Instruction,
        info: ContractsContextsInfo
    ): ContractsContextsInfo {
        val previousDepth = previousInstruction.blockScope.depth
        val currentDepth = instruction.blockScope.depth

        return when {
            previousDepth < currentDepth -> visitEnterBlock(instruction.blockScope, info)
            previousDepth > currentDepth -> visitExitBlock(previousInstruction.blockScope, info)
            else -> info
        }
    }

    // collect facts
    private fun visitEnterBlock(
        blockScope: BlockScope,
        info: ContractsContextsInfo
    ): ContractsContextsInfo {
        val block = blockScope.block as? KtExpression ?: return info
        val contexts = FactsEffectSystem.declaredContexts(block, bindingContext)
        val contextsGroupedByFamily = info.toMutableMap()
        combineContexts(contextsGroupedByFamily, contexts, blockScope.depth)
        return ContractsContextsInfo(contextsGroupedByFamily)
    }

    // collect checkers
    private fun visitExitBlock(
        blockScope: BlockScope,
        info: ContractsContextsInfo
    ): ContractsContextsInfo {
        val block = blockScope.block as? KtExpression ?: return info
        val verifiers = FactsEffectSystem.declaredVerifiers(block, bindingContext)
        val context = info.toMutableMap()
        applyVerifiers(verifiers, context)
        return ContractsContextsInfo(context)
    }

    private fun mergeEdges(
        instruction: Instruction,
        incoming: Collection<ContractsContextsInfo>
    ): Edges<ContractsContextsInfo> {
        val mergedData = merge(incoming)
        val updatedData = if (instruction is CallInstruction) visitCallInstruction(instruction, mergedData) else mergedData
        return Edges(mergedData, updatedData)
    }

    private fun merge(incoming: Collection<ContractsContextsInfo>): ContractsContextsInfo {
        when (incoming.size) {
            0 -> return ContractsContextsInfo.EMPTY
            1 -> return incoming.first()
        }
        val families = incoming.flatMap { it.keySet() }.toSet()

        val reducedContextsGroupedByFamily = mutableMapOf<ContextFamily, Map<Int, Context>>()
        for (family in families) {
            val incomingContextsGroupedByDepth = incoming
                .map { it[family].getOrElse(mapOf()) }
                .flatMap { it.toList() }
                .groupBy { it.first }
                .mapValues { (_, list) -> list.map { it.second } }
            // works
//            val reducedContextsGroupedByLevel = incomingContextsGroupedByDepth
//                .mapValues { (_, incomingContexts) -> incomingContexts.reduce(family.combiner::or) }
//            reducedContextsGroupedByFamily[family] = reducedContextsGroupedByLevel

            // don't works
            reducedContextsGroupedByFamily[family] = incomingContextsGroupedByDepth
                .mapValues<Int, List<Context>, Context> { (_, incomingContexts) -> incomingContexts.reduce<Context, Context>(family.combiner::or) }
        }

        return ContractsContextsInfo(ImmutableHashMap.ofAll(reducedContextsGroupedByFamily))
    }

    // collect facts and checkers
    private fun visitCallInstruction(
        instruction: CallInstruction,
        info: ContractsContextsInfo
    ): ContractsContextsInfo {
        val contextsGroupedByFamily = info.toMutableMap()

        val callExpression = instruction.element as? KtCallExpression ?: return info

        val (contexts, verifiers) = FactsEffectSystem.declaredFactsAndCheckers(callExpression, bindingContext)
        combineContexts(contextsGroupedByFamily, contexts, null)
        applyVerifiers(verifiers, contextsGroupedByFamily)

        return ContractsContextsInfo(contextsGroupedByFamily)
    }

    // TODO: contexts to Map<>
    private fun combineContexts(
        contextsGroupedByFamily: MutableMap<ContextFamily, MutableMap<Int, Context>>,
        contexts: Collection<Context>,
        depth: Int?
    ) {
        val level = depth ?: -1
        for (context in contexts) {
            val family = context.family
            if (family !in contextsGroupedByFamily) {
                contextsGroupedByFamily[family] = mutableMapOf()
            }
            val existedContext = contextsGroupedByFamily[family]!![level] ?: family.emptyContext
            contextsGroupedByFamily[family]!![level] = family.combiner.combine(existedContext, context)
        }
    }

    private fun applyVerifiers(
        verifiers: Collection<ContextVerifier>,
        contextsGroupedByFamily: MutableMap<ContextFamily, MutableMap<Int, Context>>
    ) {
        for (verifier in verifiers) {
            val family = verifier.family
            if (family !in contextsGroupedByFamily) {
                contextsGroupedByFamily[family] = mutableMapOf()
            }

            contextsGroupedByFamily[family] = contextsGroupedByFamily[family]!!.mapValues { (_, context) ->
                verifier.cleanupProcessed(context)
            }.toMutableMap()
        }
    }

    private fun ContractsContextsInfo.convertToMap() = iterator().map { it._1 to it._2 }.toList().toMap()

    private fun ContractsContextsInfo.toMutableMap() = convertToMap()
        .mapValues { (_, map) -> map.toMutableMap() }
        .toMutableMap()
}