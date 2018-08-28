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
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.AdditionalControlFlowInfo
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.LocalFunctionAnalysisStrategy
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.TraversalOrder
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.newCollectData
import org.jetbrains.kotlin.contracts.FactsEffectSystem
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextChecker
import org.jetbrains.kotlin.contracts.facts.ContextFact
import org.jetbrains.kotlin.contracts.facts.ContextFamily
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
        val data = pseudocode.newCollectData(
            TraversalOrder.FORWARD,
            ::merge,
            ::update,
            FactsControlFlowInfo.EMPTY,
            LocalFunctionAnalysisStrategy.ONLY_INLINED_LAMBDAS
        )
        return data[pseudocode.exitInstruction]?.incoming
    }

    private fun merge(incoming: Collection<FactsControlFlowInfo>): FactsControlFlowInfo {
        when (incoming.size) {
            0 -> return FactsControlFlowInfo.EMPTY
            1 -> return incoming.first()
        }
        val families = incoming.flatMap { it.keySet() }.toSet()

        val contextsGroupedByFamily = mutableMapOf<ContextFamily, List<Context>>()
        for (family in families) {
            val contexts = incoming.map { it[family].getOrElse(family.emptyContext) }
            contextsGroupedByFamily[family] = contexts
        }

        val reducedContextsGroupedByFamily = contextsGroupedByFamily
            .mapValues { (family, contexts) -> contexts.reduce(family.combiner::or) }

        return FactsControlFlowInfo(ImmutableHashMap.ofAll(reducedContextsGroupedByFamily))
    }

    private fun update(
        instruction: Instruction,
        info: FactsControlFlowInfo,
        additionalInfo: AdditionalControlFlowInfo
    ): FactsControlFlowInfo =
        when (instruction) {
            is SubroutineEnterInstruction -> visitSubroutineEnter(instruction, info)
            is SubroutineExitInstruction -> visitSubroutineExit(instruction, info, additionalInfo)
            is CallInstruction -> visitCallInstruction(instruction, info, additionalInfo)
            is InlinedLocalFunctionDeclarationInstruction -> visitInlinedLocalFunctionDeclarationInstruction(instruction, info)
            else -> info
        }

    // collect facts
    private fun visitSubroutineEnter(
        instruction: SubroutineEnterInstruction,
        info: FactsControlFlowInfo
    ): FactsControlFlowInfo {
        val lambdaExpression = instruction.subroutine.parent as? KtLambdaExpression ?: return info

        val facts = FactsEffectSystem.declaredFacts(lambdaExpression, bindingContext)
        val context = info.toMutableMap()
        addFactsToContext(facts, context)
        return FactsControlFlowInfo(context)
    }

    // collect checkers
    private fun visitSubroutineExit(
        instruction: SubroutineExitInstruction,
        info: FactsControlFlowInfo,
        additionalInfo: AdditionalControlFlowInfo
    ): FactsControlFlowInfo {
        val lambdaExpression = instruction.subroutine.parent as? KtLambdaExpression ?: return info

        val checkers = FactsEffectSystem.declaredCheckers(lambdaExpression, bindingContext)
        val context = info.toMutableMap()
        applyCheckers(checkers, context, additionalInfo)
        return FactsControlFlowInfo(context)
    }

    // collect facts and checkers
    private fun visitCallInstruction(
        instruction: CallInstruction,
        info: FactsControlFlowInfo,
        additionalInfo: AdditionalControlFlowInfo
    ): FactsControlFlowInfo {
        val context = info.toMutableMap()

        val callExpression = instruction.element as? KtCallExpression ?: return info

        val (facts, checkers) = FactsEffectSystem.declaredFactsAndCheckers(callExpression, bindingContext)
        addFactsToContext(facts, context)
        applyCheckers(checkers, context, additionalInfo)

        return FactsControlFlowInfo(context)
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

    private fun addFactsToContext(
        facts: Collection<ContextFact>,
        contextsGroupedByFamily: MutableMap<ContextFamily, Context>
    ) {
        for (fact in facts) {
            val family = fact.family
            val context = contextsGroupedByFamily[family] ?: family.emptyContext
            contextsGroupedByFamily[family] = family.combiner.combine(context, fact)
        }
    }

    private fun applyCheckers(
        checkers: Collection<ContextChecker>,
        contextsGroupedByFamily: MutableMap<ContextFamily, Context>,
        additionalInfo: AdditionalControlFlowInfo
    ) {
        for (checker in checkers) {
            val family = checker.family
            val context = contextsGroupedByFamily[family] ?: family.emptyContext
            contextsGroupedByFamily[family] = checker.verifyContext(context, bindingTrace, additionalInfo.lastCfaPass)
        }
    }

    private fun FactsControlFlowInfo.convertToMap() = iterator().map { it._1 to it._2 }.toList().toMap()

    private fun FactsControlFlowInfo.toMutableMap() = convertToMap().toMutableMap()
}