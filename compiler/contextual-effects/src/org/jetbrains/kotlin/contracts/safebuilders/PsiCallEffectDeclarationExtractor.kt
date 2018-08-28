/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.description.expressions.ContractDescriptionValue
import org.jetbrains.kotlin.contracts.facts.ContextDeclaration
import org.jetbrains.kotlin.contracts.facts.VerifierDeclaration
import org.jetbrains.kotlin.contracts.parsing.PsiContractParserDispatcher
import org.jetbrains.kotlin.contracts.parsing.PsiEffectDeclarationExtractor
import org.jetbrains.kotlin.contracts.parsing.argumentAsExpressionOrNull
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

class PsiCallEffectDeclarationExtractor(context: BindingContext, dispatcher: PsiContractParserDispatcher) :
    PsiEffectDeclarationExtractor(context, dispatcher) {
    companion object {
        private const val CALLS = "Calls"
        private const val CALL_KIND = "CallKind"
    }

    override fun extractContextDeclaration(declaration: KtExpression): Pair<ContextDeclaration, List<ContractDescriptionValue>>? {
        if (declaration !is KtCallExpression) return null

        val resolvedCall = declaration.getResolvedCall(context) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        val constructorName = extractConstructorName(descriptor) ?: return null
        if (constructorName != CALLS) return null

        val functionReference = dispatcher.parseFunction(resolvedCall.argumentAsExpressionOrNull(0)) ?: return null
        val thisReference = dispatcher.parseVariable(resolvedCall.argumentAsExpressionOrNull(1)) ?: return null

        return CallDeclaration() to listOf(functionReference, thisReference)
    }

    override fun extractVerifierDeclaration(declaration: KtExpression): Pair<VerifierDeclaration, List<ContractDescriptionValue>>? {
        if (declaration !is KtCallExpression) return null

        val resolvedCall = declaration.getResolvedCall(context) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        val constructorName = extractConstructorName(descriptor) ?: return null
        if (constructorName != CALL_KIND) return null

        val functionReference = dispatcher.parseFunction(resolvedCall.argumentAsExpressionOrNull(0)) ?: return null
        val kindName = resolvedCall.argumentAsExpressionOrNull(1)
            ?.getResolvedCall(context)
            ?.resultingDescriptor
            ?.name
            ?.asString()
            ?: return null
        val kind = parseKind(kindName)
        val receiverReference = dispatcher.parseReceiver(resolvedCall.argumentAsExpressionOrNull(2)) ?: return null

        return CallVerifierDeclaration(kind) to listOf(functionReference, receiverReference)
    }

    private fun parseKind(kind: String) = when (kind) {
        "AT_MOST_ONCE" -> InvocationKind.AT_MOST_ONCE
        "EXACTLY_ONCE" -> InvocationKind.EXACTLY_ONCE
        "AT_LEAST_ONCE" -> InvocationKind.AT_LEAST_ONCE
        else -> throw AssertionError("Unknown kind $kind")
    }
}