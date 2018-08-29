/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.exceptions

import org.jetbrains.kotlin.contracts.facts.ContextDeclaration
import org.jetbrains.kotlin.contracts.facts.VerifierDeclaration
import org.jetbrains.kotlin.contracts.parsing.PsiContractParserDispatcher
import org.jetbrains.kotlin.contracts.parsing.PsiEffectDeclarationExtractor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.types.KotlinType

class PsiExceptionEffectDeclarationExtractor(context: BindingContext, dispatcher: PsiContractParserDispatcher) :
    PsiEffectDeclarationExtractor(context, dispatcher) {
    companion object {
        private const val CONSTRUCTOR_NAME = "CatchesException"
    }

    override fun extractContextDeclaration(declaration: KtExpression): ContextDeclaration? {
        val exceptionType = getExceptionType(declaration) ?: return null
        return ExceptionContextDeclaration(exceptionType)
    }

    override fun extractVerifierDeclaration(declaration: KtExpression): VerifierDeclaration? {
        val exceptionType = getExceptionType(declaration) ?: return null
        return ExceptionVerifierDeclaration(exceptionType)
    }

    private fun getExceptionType(expression: KtExpression): KotlinType? {
        if (expression !is KtCallExpression) return null

        val (resolvedCall, descriptor) = expression.getResolverCallAndResultingDescriptor(context) ?: return null

        val constructorName = extractConstructorName(descriptor) ?: return null
        if (constructorName != CONSTRUCTOR_NAME) return null

        return resolvedCall.typeArguments.values.firstOrNull()
    }
}