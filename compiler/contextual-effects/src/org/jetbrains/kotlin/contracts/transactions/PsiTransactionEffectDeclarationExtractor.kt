/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.transactions

import org.jetbrains.kotlin.contracts.description.expressions.VariableReference
import org.jetbrains.kotlin.contracts.facts.CleanerDeclaration
import org.jetbrains.kotlin.contracts.facts.ProviderDeclaration
import org.jetbrains.kotlin.contracts.facts.VerifierDeclaration
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames
import org.jetbrains.kotlin.contracts.parsing.PsiContractParserDispatcher
import org.jetbrains.kotlin.contracts.parsing.PsiEffectDeclarationExtractor
import org.jetbrains.kotlin.contracts.parsing.firstArgumentAsExpressionOrNull
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext

class PsiTransactionEffectDeclarationExtractor(
    context: BindingContext,
    dispatcher: PsiContractParserDispatcher
) : PsiEffectDeclarationExtractor(context, dispatcher) {
    companion object {
        private const val CONSTRUCTOR_NAME = "OpenedTransaction"
    }

    override fun extractProviderDeclaration(declaration: KtExpression, dslFunctionName: Name): ProviderDeclaration? {
        val thisReference = extractThisReference(declaration) ?: return null
        return TransactionProviderDeclaration(listOf(thisReference))
    }

    override fun extractVerifierDeclaration(declaration: KtExpression, dslFunctionName: Name): VerifierDeclaration? {
        val thisReference = extractThisReference(declaration) ?: return null
        return TransactionVerifierDeclaration(listOf(thisReference))
    }

    override fun extractCleanerDeclaration(declaration: KtExpression, dslFunctionName: Name): CleanerDeclaration? {
        if (dslFunctionName != ContractsDslNames.CLOSES) return null
        val thisReference = extractThisReference(declaration) ?: return null
        return TransactionCleanerDeclaration(listOf(thisReference))
    }

    private fun extractThisReference(declaration: KtExpression): VariableReference? {
        if (declaration !is KtCallExpression) return null
        val (resolvedCall, descriptor) = declaration.getResolverCallAndResultingDescriptor(context) ?: return null

        if (extractConstructorName(descriptor) != CONSTRUCTOR_NAME) return null

        return dispatcher.parseVariable(resolvedCall.firstArgumentAsExpressionOrNull())
    }
}