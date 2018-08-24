/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.exceptions

import org.jetbrains.kotlin.contracts.description.expressions.VariableReference
import org.jetbrains.kotlin.contracts.facts.ContextCheckerFactoryDeclaration
import org.jetbrains.kotlin.contracts.facts.ContextFactFactoryDeclaration
import org.jetbrains.kotlin.contracts.parsing.ContextFactParser
import org.jetbrains.kotlin.contracts.parsing.PsiContractParserDispatcher
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.types.KotlinType

class ExceptionFactParser(context: BindingContext, dispatcher: PsiContractParserDispatcher) : ContextFactParser(context, dispatcher) {
    companion object {
        private const val CONSTRUCTOR_NAME = "CatchesException"
    }

    override fun parseDeclarationForFactFactory(declaration: KtExpression): Pair<ContextFactFactoryDeclaration, List<VariableReference>>? {
        val exceptionType = getExceptionType(declaration) ?: return null
        val factory = ExceptionFactFactoryDeclaration(exceptionType)
        return factory to emptyList()
    }

    override fun parseDeclarationForCheckerFactory(declaration: KtExpression): Pair<ContextCheckerFactoryDeclaration, List<VariableReference>>? {
        val exceptionType = getExceptionType(declaration) ?: return null
        val factory = ExceptionCheckerFactoryDeclaration(exceptionType)
        return factory to emptyList()
    }

    private fun getExceptionType(expression: KtExpression): KotlinType? {
        if (expression !is KtCallExpression) return null
        val resolvedCall = expression.getResolvedCall(context) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        val constructorName = extractConstructorName(descriptor) ?: return null
        if (constructorName != CONSTRUCTOR_NAME) return null

        return resolvedCall.typeArguments.values.firstOrNull()
    }
}