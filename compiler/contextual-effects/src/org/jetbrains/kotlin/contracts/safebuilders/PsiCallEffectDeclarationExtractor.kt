/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.description.expressions.ContractDescriptionValue
import org.jetbrains.kotlin.contracts.facts.ProviderDeclaration
import org.jetbrains.kotlin.contracts.parsing.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

class PsiCallEffectDeclarationExtractor(context: BindingContext, dispatcher: PsiContractVariableParserDispatcher) :
    PsiEffectDeclarationExtractor(context, dispatcher) {
    companion object {
        private const val CALLS = "Calls"
        private const val CALL_KIND = "CallKind"
    }

    override fun extractDeclarations(declaration: KtExpression, dslFunctionName: Name): ContextDeclarations {
        return when (dslFunctionName) {
            ContextDslNames.PROVIDES -> {
                val provider = extractProviderDeclaration(declaration, dslFunctionName)
                ContextDeclarations(provider = provider)
            }
            ContextDslNames.REQUIRES -> {
                val (kind, references) = extractKindAndReferences(declaration) ?: return ContextDeclarations()
                val verifier = CallVerifierDeclaration(kind, references)
                val cleaner = CallCleanerDeclaration(kind, references)
                ContextDeclarations(verifier = verifier, cleaner = cleaner)
            }
            else -> ContextDeclarations()
        }
    }

    private fun extractProviderDeclaration(declaration: KtExpression, dslFunctionName: Name): ProviderDeclaration? {
        if (declaration !is KtCallExpression) return null

        val (resolvedCall, descriptor) = declaration.getResolverCallAndResultingDescriptor(context) ?: return null

        val constructorName = extractConstructorName(descriptor) ?: return null
        if (constructorName != CALLS) return null

        val functionReference = dispatcher.parseFunction(resolvedCall.argumentAsExpressionOrNull(0)) ?: return null
        val thisReference = dispatcher.parseVariable(resolvedCall.argumentAsExpressionOrNull(1)) ?: return null

        val references = listOf(functionReference, thisReference)
        return CallProviderDeclaration(references)
    }

    private fun extractKindAndReferences(declaration: KtExpression): Pair<InvocationKind, List<ContractDescriptionValue>>? {
        if (declaration !is KtCallExpression) return null

        val (resolvedCall, descriptor) = declaration.getResolverCallAndResultingDescriptor(context) ?: return null

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

        val references = listOf(functionReference, receiverReference)

        return kind to references
    }

    private fun parseKind(kind: String) = when (kind) {
        "AT_MOST_ONCE" -> InvocationKind.AT_MOST_ONCE
        "EXACTLY_ONCE" -> InvocationKind.EXACTLY_ONCE
        "AT_LEAST_ONCE" -> InvocationKind.AT_LEAST_ONCE
        else -> throw AssertionError("Unknown kind $kind")
    }
}