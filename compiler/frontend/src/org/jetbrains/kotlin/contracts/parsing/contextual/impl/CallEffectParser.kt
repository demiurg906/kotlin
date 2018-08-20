/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectSupplier
import org.jetbrains.kotlin.contracts.contextual.safebuilders.CallEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.safebuilders.CallEffectSupplier
import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.parsing.contextual.ContextualEffectParser
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ExpressionValueArgument

class CallEffectParser(val context: BindingContext) : ContextualEffectParser() {
    companion object {
        private const val SUPPLIES_EFFECT_NAME = "CallEffect"
        private const val CONSUMES_EFFECT_NAME = "RequiresCallEffect"
    }

    override fun parseDeclarationForSupplier(declaration: KtExpression): ContextualEffectSupplier? {
        if (declaration !is KtCallExpression) return null
        val resolvedCall = declaration.getResolvedCall(context) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        val constructorName = extractConstructorName(descriptor) ?: return null
        if (constructorName != SUPPLIES_EFFECT_NAME) return null

        val argumentExpression = (resolvedCall.valueArgumentsByIndex?.get(0) as? ExpressionValueArgument)
            ?.valueArgument?.getArgumentExpression()

        val setter = extractFunction(argumentExpression) ?: return null
        return CallEffectSupplier(setter)
    }

    override fun parseDeclarationForConsumer(declaration: KtExpression): ContextualEffectConsumer? {
        if (declaration !is KtCallExpression) return null
        val resolvedCall = declaration.getResolvedCall(context) ?: return null
        val descriptor = extractDescriptor(declaration) ?: return null

        val constructorName = extractConstructorName(descriptor) ?: return null
        if (constructorName != CONSUMES_EFFECT_NAME) return null

        val arguments = resolvedCall.valueArgumentsByIndex
            ?.map { (it as? ExpressionValueArgument)?.valueArgument?.getArgumentExpression() ?: return null }
            ?: return null

        val setter = extractFunction(arguments.getOrNull(0)) ?: return null

        val kindName = arguments[1].getResolvedCall(context)?.resultingDescriptor?.name?.asString() ?: return null
        val kind = parseKind(kindName)

        return CallEffectConsumer(setter, kind)
    }

    private fun extractDescriptor(declaration: KtExpression): CallableDescriptor? {
        if (declaration !is KtCallExpression) return null
        val resolvedCall = declaration.getResolvedCall(context) ?: return null
        return resolvedCall.resultingDescriptor
    }

    private fun extractFunction(expression: KtExpression?): FunctionDescriptor? {
        val setterReference = expression as? KtCallableReferenceExpression ?: return null
        return context[BindingContext.REFERENCE_TARGET, setterReference.callableReference] as? FunctionDescriptor
    }

    private fun parseKind(kind: String) = when (kind) {
        "AT_MOST_ONCE" -> InvocationKind.AT_MOST_ONCE
        "EXACTLY_ONCE" -> InvocationKind.EXACTLY_ONCE
        "AT_LEAST_ONCE" -> InvocationKind.AT_LEAST_ONCE
        else -> throw AssertionError("Unknown kind $kind")
    }
}