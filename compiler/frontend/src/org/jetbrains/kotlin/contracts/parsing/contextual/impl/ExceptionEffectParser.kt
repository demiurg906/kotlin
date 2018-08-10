/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.EffectConsumer
import org.jetbrains.kotlin.contracts.contextual.EffectSupplier
import org.jetbrains.kotlin.contracts.contextual.impl.ExceptionEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.impl.ExceptionEffectSupplier
import org.jetbrains.kotlin.contracts.parsing.contextual.ContextualEffectParser
import org.jetbrains.kotlin.contracts.parsing.firstArgumentAsExpressionOrNull
import org.jetbrains.kotlin.contracts.parsing.isConsumesEffectDescriptor
import org.jetbrains.kotlin.contracts.parsing.isSuppliesEffectDescriptor
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

typealias ExceptionType = String

class ExceptionEffectParser(val context: BindingContext) : ContextualEffectParser {
    companion object {
        private const val EFFECT_NAME = "ExceptionEffectDescription"
    }

    override fun parseDeclarationForSupplier(declaration: KtCallExpression): EffectSupplier? {
        val exception = parseDeclaration(
            declaration,
            DeclarationDescriptor::isSuppliesEffectDescriptor
        ) ?: return null
        return ExceptionEffectSupplier(exception)
    }

    override fun parseDeclarationForConsumer(declaration: KtCallExpression): EffectConsumer? {
        val exception = parseDeclaration(
            declaration,
            DeclarationDescriptor::isConsumesEffectDescriptor
        ) ?: return null
        return ExceptionEffectConsumer(exception)
    }

    private fun parseDeclaration(declaration: KtCallExpression, checker: CallableDescriptor.() -> Boolean): ExceptionType? {
        val resolvedCall = declaration.getResolvedCall(context) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        if (!descriptor.checker()) return null

        val argumentExpression = resolvedCall.firstArgumentAsExpressionOrNull() ?: return null
        return argumentExpression.accept(PsiExceptionDefinitionParser(context), Unit)
    }

    internal class PsiExceptionDefinitionParser(val context: BindingContext) : KtVisitor<ExceptionType?, Unit>() {
        override fun visitKtElement(element: KtElement, data: Unit?): ExceptionType? = null

        override fun visitCallExpression(expression: KtCallExpression, data: Unit?): ExceptionType? {
            val resolvedCall = expression.getResolvedCall(context) ?: return null
            val descriptor = resolvedCall.resultingDescriptor

            val constructorName = (descriptor as? ClassConstructorDescriptor)?.constructedClass?.name?.asString() ?: return null
            if (constructorName != EFFECT_NAME) return null

            val argumentExpression = resolvedCall.firstArgumentAsExpressionOrNull() ?: return null

            return argumentExpression.accept(PsiStringParser(context), Unit)
        }

    }

    internal class PsiStringParser(val context: BindingContext) : KtVisitor<String?, Unit>() {
        override fun visitKtElement(element: KtElement, data: Unit?): String? = null

        override fun visitStringTemplateExpression(expression: KtStringTemplateExpression, data: Unit?): String? {
            if (expression.entries.isEmpty()) return null
            return expression.entries.joinToString("") { it.text }
        }
    }
}