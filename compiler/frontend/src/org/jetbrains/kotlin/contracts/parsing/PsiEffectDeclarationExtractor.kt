/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing

import org.jetbrains.kotlin.contracts.facts.CleanerDeclaration
import org.jetbrains.kotlin.contracts.facts.ProviderDeclaration
import org.jetbrains.kotlin.contracts.facts.VerifierDeclaration
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall

abstract class PsiEffectDeclarationExtractor(val context: BindingContext, val dispatcher: PsiContractParserDispatcher) {
    abstract fun extractDeclarations(declaration: KtExpression, dslFunctionName: Name): ContextDeclarations

    internal fun extractDeclarationsOrNull(declaration: KtExpression, dslFunctionName: Name): ContextDeclarations? {
        val declarations = extractDeclarations(declaration, dslFunctionName)
        return if (declarations.isEmpty()) null else declarations
    }

    protected fun extractConstructorName(descriptor: CallableDescriptor) =
        (descriptor as? ClassConstructorDescriptor)?.constructedClass?.name?.asString()

    protected fun KtExpression.getResolverCallAndResultingDescriptor(
        context: BindingContext
    ): Pair<ResolvedCall<*>, CallableDescriptor>? {
        val resolvedCall = getResolvedCall(context) ?: return null
        val descriptor = resolvedCall.resultingDescriptor
        return resolvedCall to descriptor
    }
}

data class ContextDeclarations(
    val provider: ProviderDeclaration? = null,
    val verifier: VerifierDeclaration? = null,
    val cleaner: CleanerDeclaration? = null
) {
    internal fun isEmpty() = provider == null && verifier == null && cleaner == null
}