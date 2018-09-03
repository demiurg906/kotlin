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
import org.jetbrains.kotlin.cfg.pseudocode.instructions.Instruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.eval.CallInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.SubroutineEnterInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.SubroutineExitInstruction
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.*
import org.jetbrains.kotlin.contracts.FactsEffectSystem
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextFamily
import org.jetbrains.kotlin.contracts.facts.ContextVerifier
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
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

    val resultingContexts: Map<ContextFamily, Context>? = computeEffectsControlFlowInfo(pseudocode)

    private fun computeEffectsControlFlowInfo(pseudocode: Pseudocode): Map<ContextFamily, Context>? {
        // collect info via CFA
        val edgesMap = pseudocode.collectData(
            traversalOrder = TraversalOrder.FORWARD,
            mergeEdges = ::mergeEdges,
            updateEdge = { _, _, info -> info },
            initialInfo = ContractsContextsInfo.EMPTY,
            localFunctionAnalysisStrategy = LocalFunctionAnalysisStrategy.ONLY_IN_PLACE_LAMBDAS
        )

        // verify resulting context
        pseudocode.traverse(
            traversalOrder = TraversalOrder.FORWARD,
            edgesMap = edgesMap,
            analyzeInstruction = ::verify
        )
        return edgesMap[pseudocode.exitInstruction]?.incoming?.toMutableMap()
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
            val context = info[family].getOrElse(family.emptyContext)
            verifier.verify(context, bindingTrace)
        }
    }

    // -------------------------- Collecting data --------------------------

    private fun mergeEdges(
        instruction: Instruction,
        incoming: Collection<ContractsContextsInfo>,
        depth: Int
    ): Edges<ContractsContextsInfo> {
        val mergedData = merge(incoming)
        val updatedData = update(instruction, mergedData, depth)
        return Edges(mergedData, updatedData)
    }

    private fun merge(incoming: Collection<ContractsContextsInfo>): ContractsContextsInfo {
        when (incoming.size) {
            0 -> return ContractsContextsInfo.EMPTY
            1 -> return incoming.first()
        }
        val families = incoming.flatMap { it.keySet() }.toSet()

        val reducedContextsGroupedByFamily = mutableMapOf<ContextFamily, Context>()
        for (family in families) {
            val incomingContexts = incoming.map {
                it[family].getOrElse(family.emptyContext)
            }
            reducedContextsGroupedByFamily[family] = incomingContexts.reduce(family.combiner::or)
        }

        return ContractsContextsInfo(ImmutableHashMap.ofAll(reducedContextsGroupedByFamily))
    }

    private fun update(
        instruction: Instruction,
        info: ContractsContextsInfo,
        depth: Int
    ): ContractsContextsInfo =
        when (instruction) {
            is SubroutineEnterInstruction -> visitSubroutineEnter(instruction, info, depth)
            is SubroutineExitInstruction -> visitSubroutineExit(instruction, info, depth)
            is CallInstruction -> visitCallInstruction(instruction, info)
            else -> info
        }

    // collect facts
    private fun visitSubroutineEnter(
        instruction: SubroutineEnterInstruction,
        info: ContractsContextsInfo,
        depth: Int
    ): ContractsContextsInfo {
        val lambdaExpression = instruction.subroutine.parent as? KtLambdaExpression ?: return info

        val contexts = FactsEffectSystem.declaredContexts(lambdaExpression, bindingContext)
        val contextsGroupedByFamily = info.toMutableMap()
        combineContexts(contextsGroupedByFamily, contexts, depth)
        return ContractsContextsInfo(contextsGroupedByFamily)
    }

    // collect checkers
    private fun visitSubroutineExit(
        instruction: SubroutineExitInstruction,
        info: ContractsContextsInfo,
        depth: Int
    ): ContractsContextsInfo {
        val lambdaExpression = instruction.subroutine.parent as? KtLambdaExpression ?: return info

        val verifiers = FactsEffectSystem.declaredVerifiers(lambdaExpression, bindingContext)
        val context = info.toMutableMap()
        applyVerifiers(verifiers, context)
        cleanUpContextAtExit(context, depth)
        return ContractsContextsInfo(context)
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
        contextsGroupedByFamily: MutableMap<ContextFamily, Context>,
        contexts: Collection<Context>,
        depth: Int?
    ) {
        for (context in contexts) {
            val family = context.family
            val existedContext = contextsGroupedByFamily[family] ?: family.emptyContext
            contextsGroupedByFamily[family] = family.combiner.combine(existedContext, context, depth)
        }
    }

    private fun applyVerifiers(
        verifiers: Collection<ContextVerifier>,
        contextsGroupedByFamily: MutableMap<ContextFamily, Context>
    ) {
        for (verifier in verifiers) {
            val family = verifier.family
            val context = contextsGroupedByFamily[family] ?: family.emptyContext
            contextsGroupedByFamily[family] = verifier.cleanupProcessed(context)
        }
    }

    private fun cleanUpContextAtExit(
        contextsGroupedByFamily: MutableMap<ContextFamily, Context>,
        depth: Int
    ) {
        for ((family, context) in contextsGroupedByFamily) {
            contextsGroupedByFamily[family] = family.combiner.cleanupContextAtBlockExit(context, depth)
        }
    }

    private fun ContractsContextsInfo.convertToMap() = iterator().map { it._1 to it._2 }.toList().toMap()

    private fun ContractsContextsInfo.toMutableMap() = convertToMap().toMutableMap()
}