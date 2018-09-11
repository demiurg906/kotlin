/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.transactions

import org.jetbrains.kotlin.contracts.description.expressions.VariableReference
import org.jetbrains.kotlin.contracts.parsing.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext

class PsiTransactionEffectDeclarationExtractor(
    context: BindingContext,
    dispatcher: PsiContractVariableParserDispatcher
) : PsiEffectDeclarationExtractor(context, dispatcher) {
    companion object {
        private const val CONSTRUCTOR_NAME = "OpenedTransaction"
    }

    override fun extractDeclarations(declaration: KtExpression, dslFunctionName: Name): ContextDeclarations {
        if (dslFunctionName !in setOf(ContractsDslNames.STARTS, ContractsDslNames.REQUIRES, ContractsDslNames.CLOSES))
            return ContextDeclarations()

        val thisReference = extractThisReference(declaration) ?: return ContextDeclarations()
        val references = listOf(thisReference)

        return when (dslFunctionName) {
            ContextDslNames.STARTS -> {
                val provider = TransactionProviderDeclaration(references)
                val verifier = ClosedTransactionVerifierDeclaration(references)
                ContextDeclarations(provider = provider, verifier = verifier)
            }
            ContextDslNames.REQUIRES -> {
                val verifier = OpenedTransactionVerifierDeclaration(references)
                ContextDeclarations(verifier = verifier)
            }
            ContextDslNames.CLOSES -> {
                val verifier = OpenedTransactionVerifierDeclaration(references)
                val cleaner = TransactionCleanerDeclaration(references)
                ContextDeclarations(verifier = verifier, cleaner = cleaner)
            }
            else -> ContextDeclarations()
        }
    }

    private fun extractThisReference(declaration: KtExpression): VariableReference? {
        if (declaration !is KtCallExpression) return null
        val (resolvedCall, descriptor) = declaration.getResolverCallAndResultingDescriptor(context) ?: return null

        if (extractConstructorName(descriptor) != CONSTRUCTOR_NAME) return null

        return dispatcher.parseVariable(resolvedCall.firstArgumentAsExpressionOrNull())
    }
}