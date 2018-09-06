/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts

import org.jetbrains.kotlin.contracts.facts.ContextCleaner
import org.jetbrains.kotlin.contracts.facts.ContextFamily
import org.jetbrains.kotlin.contracts.facts.ContextProvider
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
}

fun KtExpression.declaredFactsInfo(bindingContext: BindingContext): FactsBindingInfo = bindingContext[BindingContext.CONTEXT_FACTS, this] ?: FactsBindingInfo()

data class FactsBindingInfo(
    val providers: Collection<ContextProvider> = listOf(),
    val verifiers: Collection<ContextVerifier> = listOf(),
    val cleaners: Collection<ContextCleaner> = listOf()
)