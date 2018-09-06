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
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext

object FactsEffectSystem {
    private val contextEffectsFamiliesProvider: ContextFamiliesProvider = ContextFamiliesService

    fun getFamilies(): Collection<ContextFamily> = contextEffectsFamiliesProvider.getFamilies()
    fun getParsers(): Collection<(BindingContext, PsiContractParserDispatcher) -> PsiEffectDeclarationExtractor> =
        contextEffectsFamiliesProvider.getParsers()

    // calls
    fun declaredFactsAndCheckers(
        callExpression: KtExpression,
        context: BindingContext
    ): Pair<Collection<Context>, Collection<ContextVerifier>> {
        val (contexts, verifiers) = context[BindingContext.CONTEXT_FACTS, callExpression]
            ?: return emptyList() to emptyList()
        return contexts to verifiers
    }

    // blocks
    fun declaredContexts(expression: KtExpression, context: BindingContext): Collection<Context> {
        val (contexts, _) = context[BindingContext.CONTEXT_FACTS, expression] ?: return emptyList()
        return contexts
    }

    fun declaredVerifiers(expression: KtExpression, context: BindingContext): Collection<ContextVerifier> {
        val (_, verifiers) = context[BindingContext.CONTEXT_FACTS, expression] ?: return emptyList()
        return verifiers
    }
}

data class FactsBindingInfo(
    val contexts: Collection<Context> = listOf(),
    val verifiers: Collection<ContextVerifier> = listOf()
)