/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.exceptions

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectSupplier
import org.jetbrains.kotlin.contracts.parsing.ContextualEffectParser
import org.jetbrains.kotlin.contracts.parsing.isConsumesEffectDescriptor
import org.jetbrains.kotlin.contracts.parsing.isSuppliesEffectDescriptor
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.types.KotlinType

class ExceptionEffectParser(context: BindingContext) : ContextualEffectParser(context) {
    companion object {
        private const val EFFECT_NAME = "ExceptionEffectDescription"
    }

    override fun parseDeclarationForSupplier(declaration: KtExpression): ContextualEffectSupplier? {
        val exception = getExceptionType(
            declaration,
            DeclarationDescriptor::isSuppliesEffectDescriptor
        ) ?: return null
        return ExceptionEffectSupplier(exception)
    }

    override fun parseDeclarationForConsumer(declaration: KtExpression): ContextualEffectConsumer? {
        val exception = getExceptionType(
            declaration,
            DeclarationDescriptor::isConsumesEffectDescriptor
        ) ?: return null
        return ExceptionEffectConsumer(exception)
    }

    private fun getExceptionType(expression: KtExpression, checker: CallableDescriptor.() -> Boolean): KotlinType? {
        if (expression !is KtCallExpression) return null
        val resolvedCall = expression.getResolvedCall(context) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        val constructorName = extractConstructorName(descriptor) ?: return null
        if (constructorName != EFFECT_NAME) return null

        return resolvedCall.typeArguments.values.firstOrNull()
    }
}