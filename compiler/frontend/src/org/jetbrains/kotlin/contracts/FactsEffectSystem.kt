/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts

import org.jetbrains.kotlin.contracts.facts.*
import org.jetbrains.kotlin.contracts.parsing.ContextFactParser
import org.jetbrains.kotlin.contracts.parsing.PsiContractParserDispatcher
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.resolve.BindingContext

object FactsEffectSystem {
    private val contextEffectsFamiliesProvider: ContextFamiliesProvider = ContextFamiliesService

    fun getFamilies(): Collection<ContextFamily> = contextEffectsFamiliesProvider.getFamilies()
    fun getParsers(): Collection<(BindingContext, PsiContractParserDispatcher) -> ContextFactParser> =
        contextEffectsFamiliesProvider.getParsers()

    // calls
    fun declaredFactsAndCheckers(
        callExpression: KtCallExpression,
        context: BindingContext
    ): Pair<Collection<ContextFact>, Collection<ContextChecker>> {
        val (factFactories, checkerFactories) = context[BindingContext.CALL_CONTEXT_FACTS, callExpression] ?: return emptyList() to emptyList()
        return factFactories.map { it.createFact(callExpression) } to checkerFactories.map { it.createChecker(callExpression) }
    }

    // lambdas
    fun declaredFacts(lambdaExpression: KtLambdaExpression, context: BindingContext): Collection<ContextFact> {
        val (factFactories, _) = context[BindingContext.LAMBDA_CONTEXT_FACTS, lambdaExpression] ?: return emptyList()
        return factFactories.map { it.createFact(lambdaExpression) }
    }

    fun declaredCheckers(lambdaExpression: KtLambdaExpression, context: BindingContext): Collection<ContextChecker> {
        val (_, checkerFactories) = context[BindingContext.LAMBDA_CONTEXT_FACTS, lambdaExpression] ?: return emptyList()
        return checkerFactories.map { it.createChecker(lambdaExpression) }
    }
}

data class FactsBindingInfo(
    val factFactories: Collection<ContextFactFactory> = listOf(),
    val checkerFactories: Collection<ContextCheckerFactory> = listOf()
)