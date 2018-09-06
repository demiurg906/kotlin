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
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.*
import org.jetbrains.kotlin.contracts.FactsEffectSystem
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextFamily
import org.jetbrains.kotlin.contracts.facts.ContextVerifier
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
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
        pseudocode.smartTraverse(
            traversalOrder = TraversalOrder.FORWARD,
            edgesMap = edgesMap,
            analyzeInstruction = ::verifyInstruction,
            analyzeIncomingEdge = ::verifyIncomingEdge,
            localFunctionAnalysisStrategy = LocalFunctionAnalysisStrategy.ONLY_IN_PLACE_LAMBDAS
        )

        val contextsGroupedByFamily = edgesMap[pseudocode.exitInstruction]?.incoming?.toMutableMap() ?: return null
        return contextsGroupedByFamily.mapValues { (_, contextsGropedByLevel) -> contextsGropedByLevel.map { it.value } }
    }

    // -------------------------- Verification --------------------------

    private fun verifyInstruction(
        instruction: Instruction,
        info: ContractsContextsInfo
    ) {
        if (instruction !is CallInstruction) return
        val callExpression = instruction.element as? KtCallExpression ?: return
        val (_, verifiers) = FactsEffectSystem.declaredFactsAndCheckers(callExpression, bindingContext)
        verifyContext(verifiers, info)
    }

    private fun verifyIncomingEdge(
        previousInstruction: Instruction,
        instruction: Instruction,
        info: ContractsContextsInfo
    ) {
        val previousDepth = previousInstruction.blockScope.depth
        val currentDepth = instruction.blockScope.depth
        if (previousDepth > currentDepth) {
            val block = instruction.blockScope.block as? KtExpression ?: return
            val verifiers= FactsEffectSystem.declaredFactsAndCheckers(block, bindingContext).second
            verifyContext(verifiers, info)
        }
    }

    private fun verifyContext(verifiers: Collection<ContextVerifier>, info: ContractsContextsInfo) {
        for (verifier in verifiers) {
            val family = verifier.family
            val contextsGroupedByLevel = info[family].getOrElse(mapOf())
            val contexts = contextsGroupedByLevel.values
            verifier.verify(contexts, bindingTrace)
        }
    }

    // -------------------------- Collecting data (updateEdge) --------------------------

    private fun updateEdge(
        previousInstruction: Instruction,
        instruction: Instruction,
        info: ContractsContextsInfo
    ): ContractsContextsInfo {
        val previousDepth = previousInstruction.blockScope.depth
        val currentDepth = instruction.blockScope.depth

        return when {
            previousDepth < currentDepth -> visitEnterBlock(previousInstruction.blockScope, info)
            previousDepth > currentDepth -> visitExitBlock(instruction.blockScope, info)
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
        val depth = blockScope.depth
        val context = info.toMutableMap()
        for (family in context.keys) {
            context[family]!!.remove(depth)
        }
        applyVerifiers(verifiers, context)
        return ContractsContextsInfo(context)
    }

    // -------------------------- Collecting data (mergeEdges) --------------------------

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

        val convertedIncoming = incoming.map { it.convertToMap() }

        val reducedContextsGroupedByFamily = mutableMapOf<ContextFamily, Map<Int, Context>>()
        for (family in families) {
            val familyContextsGroupedByDepth = convertedIncoming.map { it[family] }
            val depths = familyContextsGroupedByDepth.filterNotNull().flatMap { it.keys }.toSet()

            val incomingContextsGroupedByDepth = mutableMapOf<Int, List<Context>>()
            for (depth in depths) {
                val contexts = familyContextsGroupedByDepth.map { it?.get(depth) ?: family.emptyContext }
                incomingContextsGroupedByDepth[depth] = contexts
            }

            val reducedContextsGroupedByLevel = incomingContextsGroupedByDepth
                .mapValues { (_, incomingContexts) -> incomingContexts.reduce(family.combiner::or) }

            reducedContextsGroupedByFamily[family] = reducedContextsGroupedByLevel
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