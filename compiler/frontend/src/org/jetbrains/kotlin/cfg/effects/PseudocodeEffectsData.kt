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
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.InlinedLocalFunctionDeclarationInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.SubroutineEnterInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.SubroutineExitInstruction
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.LocalFunctionAnalysisStrategy
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.TraversalOrder
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.newCollectData
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.traverse
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

    val controlFlowInfo: FactsControlFlowInfo? = computeEffectsControlFlowInfo(pseudocode)

    private fun computeEffectsControlFlowInfo(pseudocode: Pseudocode): FactsControlFlowInfo? {
        // collect info via CFA
        val edgesMap = pseudocode.newCollectData(
            TraversalOrder.FORWARD,
            ::merge,
            ::update,
            FactsControlFlowInfo.EMPTY,
            LocalFunctionAnalysisStrategy.ONLY_INLINED_LAMBDAS
        )
        // verify resulting context
        pseudocode.traverse(
            TraversalOrder.FORWARD,
            edgesMap,
            ::verify
        )
        return edgesMap[pseudocode.exitInstruction]?.incoming
    }

    // -------------------------- Verification --------------------------

    private fun verify(instruction: Instruction, incoming: FactsControlFlowInfo, outgoing: FactsControlFlowInfo) {
        when (instruction) {
            is SubroutineExitInstruction -> verifySubroutineExitInstruction(instruction, incoming)
            is CallInstruction -> verifyCallInstruction(instruction, incoming)
        }
    }

    private fun verifySubroutineExitInstruction(instruction: SubroutineExitInstruction, incoming: FactsControlFlowInfo) {
        val lambdaExpression = instruction.subroutine.parent as? KtLambdaExpression ?: return
        val verifiers = FactsEffectSystem.declaredVerifiers(lambdaExpression, bindingContext)
        verifyContext(verifiers, incoming)
    }

    private fun verifyCallInstruction(instruction: CallInstruction, incoming: FactsControlFlowInfo) {
        val callExpression = instruction.element as? KtCallExpression ?: return
        val (_, verifiers) = FactsEffectSystem.declaredFactsAndCheckers(callExpression, bindingContext)
        verifyContext(verifiers, incoming)
    }

    private fun verifyContext(verifiers: Collection<ContextVerifier>, info: FactsControlFlowInfo) {
        for (verifier in verifiers) {
            val family = verifier.family
            val context = info[family].getOrElse(family.emptyContext)
            verifier.verify(context, bindingTrace)
        }
    }

    // -------------------------- Collecting data --------------------------

    private fun merge(incoming: Collection<FactsControlFlowInfo>): FactsControlFlowInfo {
        when (incoming.size) {
            0 -> return FactsControlFlowInfo.EMPTY
            1 -> return incoming.first()
        }
        val families = incoming.flatMap { it.keySet() }.toSet()

        val contextsGroupedByFamily = mutableMapOf<ContextFamily, List<Context>>()
        for (family in families) {
            val contexts = incoming.map {
                it[family].getOrElse(family.emptyContext)
            }
            contextsGroupedByFamily[family] = contexts
        }

        val reducedContextsGroupedByFamily = contextsGroupedByFamily
            .mapValues { (family, contexts) -> contexts.reduce(family.combiner::or) }

        return FactsControlFlowInfo(ImmutableHashMap.ofAll(reducedContextsGroupedByFamily))
    }

    private fun update(
        instruction: Instruction,
        info: FactsControlFlowInfo
    ): FactsControlFlowInfo =
        when (instruction) {
            is SubroutineEnterInstruction -> visitSubroutineEnter(instruction, info)
            is SubroutineExitInstruction -> visitSubroutineExit(instruction, info)
            is CallInstruction -> visitCallInstruction(instruction, info)
            is InlinedLocalFunctionDeclarationInstruction -> visitInlinedLocalFunctionDeclarationInstruction(instruction, info)
            else -> info
        }

    // collect facts
    private fun visitSubroutineEnter(
        instruction: SubroutineEnterInstruction,
        info: FactsControlFlowInfo
    ): FactsControlFlowInfo {
        val lambdaExpression = instruction.subroutine.parent as? KtLambdaExpression ?: return info

        val contexts = FactsEffectSystem.declaredContexts(lambdaExpression, bindingContext)
        val contextsGroupedByFamily= info.toMutableMap()
        combineContexts(contextsGroupedByFamily, contexts)
        return FactsControlFlowInfo(contextsGroupedByFamily)
    }

    // collect checkers
    private fun visitSubroutineExit(
        instruction: SubroutineExitInstruction,
        info: FactsControlFlowInfo
    ): FactsControlFlowInfo {
        val lambdaExpression = instruction.subroutine.parent as? KtLambdaExpression ?: return info

        val verifiers = FactsEffectSystem.declaredVerifiers(lambdaExpression, bindingContext)
        val context = info.toMutableMap()
        applyVerifiers(verifiers, context)
        return FactsControlFlowInfo(context)
    }

    // collect facts and checkers
    private fun visitCallInstruction(
        instruction: CallInstruction,
        info: FactsControlFlowInfo
    ): FactsControlFlowInfo {
        val contextsGroupedByFamily = info.toMutableMap()

        val callExpression = instruction.element as? KtCallExpression ?: return info

        val (contexts, verifiers) = FactsEffectSystem.declaredFactsAndCheckers(callExpression, bindingContext)
        combineContexts(contextsGroupedByFamily, contexts)
        applyVerifiers(verifiers, contextsGroupedByFamily)

        return FactsControlFlowInfo(contextsGroupedByFamily)
    }

    private fun visitInlinedLocalFunctionDeclarationInstruction(
        instruction: InlinedLocalFunctionDeclarationInstruction,
        info: FactsControlFlowInfo
    ): FactsControlFlowInfo {
        val kind = instruction.kind
        val context = info.toMutableMap()
        for (family in context.keys) {
            val combiner = family.combiner
            context[family] = combiner.updateWithInvocationKind(context[family]!!, kind)
        }
        return FactsControlFlowInfo(context)
    }

    private fun combineContexts(
        contextsGroupedByFamily: MutableMap<ContextFamily, Context>,
        contexts: Collection<Context>
    ) {
        for (context in contexts) {
            val family = context.family
            val existedContext = contextsGroupedByFamily[family] ?: family.emptyContext
            contextsGroupedByFamily[family] = family.combiner.combine(existedContext, context)
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

    private fun FactsControlFlowInfo.convertToMap() = iterator().map { it._1 to it._2 }.toList().toMap()

    private fun FactsControlFlowInfo.toMutableMap() = convertToMap().toMutableMap()
}