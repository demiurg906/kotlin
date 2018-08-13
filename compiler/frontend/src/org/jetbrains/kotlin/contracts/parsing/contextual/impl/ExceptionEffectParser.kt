/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectSupplier
import org.jetbrains.kotlin.contracts.contextual.impl.ExceptionEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.impl.ExceptionEffectSupplier
import org.jetbrains.kotlin.contracts.parsing.contextual.ContextualEffectParser
import org.jetbrains.kotlin.contracts.parsing.firstArgumentAsExpressionOrNull
import org.jetbrains.kotlin.contracts.parsing.isConsumesEffectDescriptor
import org.jetbrains.kotlin.contracts.parsing.isSuppliesEffectDescriptor
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtVisitor
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.types.KotlinType

class ExceptionEffectParser(val context: BindingContext) : ContextualEffectParser {
    companion object {
        private const val EFFECT_NAME = "ExceptionEffectDescription"
    }

    override fun parseDeclarationForSupplier(declaration: KtCallExpression): ContextualEffectSupplier? {
        val exception = getExceptionType(
            declaration,
            DeclarationDescriptor::isSuppliesEffectDescriptor
        ) ?: return null
        return ExceptionEffectSupplier(exception)
    }

    override fun parseDeclarationForConsumer(declaration: KtCallExpression): ContextualEffectConsumer? {
        val exception = getExceptionType(
            declaration,
            DeclarationDescriptor::isConsumesEffectDescriptor
        ) ?: return null
        return ExceptionEffectConsumer(exception)
    }

    private fun getExceptionType(declaration: KtCallExpression, checker: CallableDescriptor.() -> Boolean): KotlinType? {
        val resolvedCall = declaration.getResolvedCall(context) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        if (!descriptor.checker()) return null

        val argumentExpression = resolvedCall.firstArgumentAsExpressionOrNull() ?: return null
        // TODO: delete visitor
        return argumentExpression.accept(PsiExceptionDefinitionParser(context), Unit)
    }

    internal class PsiExceptionDefinitionParser(val context: BindingContext) : KtVisitor<KotlinType?, Unit>() {
        override fun visitKtElement(element: KtElement, data: Unit?): KotlinType? = null

        override fun visitCallExpression(expression: KtCallExpression, data: Unit?): KotlinType? {
            val resolvedCall = expression.getResolvedCall(context) ?: return null
            val descriptor = resolvedCall.resultingDescriptor

            val constructorName = (descriptor as? ClassConstructorDescriptor)?.constructedClass?.name?.asString() ?: return null
            if (constructorName != EFFECT_NAME) return null

            return resolvedCall.typeArguments.values.firstOrNull()
        }
    }
}