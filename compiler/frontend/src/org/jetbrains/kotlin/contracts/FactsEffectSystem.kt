/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts

import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextFamily
import org.jetbrains.kotlin.contracts.facts.ContextVerifier
import org.jetbrains.kotlin.contracts.parsing.PsiContractParserDispatcher
import org.jetbrains.kotlin.contracts.parsing.PsiEffectDeclarationExtractor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.resolve.BindingContext

object FactsEffectSystem {
    private val contextEffectsFamiliesProvider: ContextFamiliesProvider = ContextFamiliesService

    fun getFamilies(): Collection<ContextFamily> = contextEffectsFamiliesProvider.getFamilies()
    fun getParsers(): Collection<(BindingContext, PsiContractParserDispatcher) -> PsiEffectDeclarationExtractor> =
        contextEffectsFamiliesProvider.getParsers()

    // calls
    fun declaredFactsAndCheckers(
        callExpression: KtCallExpression,
        context: BindingContext
    ): Pair<Collection<Context>, Collection<ContextVerifier>> {
        val (contexts, verifiers) = context[BindingContext.CALL_CONTEXT_FACTS, callExpression]
            ?: return emptyList() to emptyList()
        return contexts to verifiers
    }

    // lambdas
    fun declaredContexts(lambdaExpression: KtLambdaExpression, context: BindingContext): Collection<Context> {
        val (contexts, _) = context[BindingContext.LAMBDA_CONTEXT_FACTS, lambdaExpression] ?: return emptyList()
        return contexts
    }

    fun declaredVerifiers(lambdaExpression: KtLambdaExpression, context: BindingContext): Collection<ContextVerifier> {
        val (_, verifiers) = context[BindingContext.LAMBDA_CONTEXT_FACTS, lambdaExpression] ?: return emptyList()
        return verifiers
    }
}

data class FactsBindingInfo(
    val contexts: Collection<Context> = listOf(),
    val verifiers: Collection<ContextVerifier> = listOf()
)