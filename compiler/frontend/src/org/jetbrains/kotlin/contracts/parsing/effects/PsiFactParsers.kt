/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing.effects

import org.jetbrains.kotlin.contracts.FactsEffectSystem
import org.jetbrains.kotlin.contracts.description.*
import org.jetbrains.kotlin.contracts.description.expressions.FunctionReference
import org.jetbrains.kotlin.contracts.facts.CleanerDeclaration
import org.jetbrains.kotlin.contracts.facts.ContextDeclaration
import org.jetbrains.kotlin.contracts.facts.ContextEntityDeclaration
import org.jetbrains.kotlin.contracts.facts.VerifierDeclaration
import org.jetbrains.kotlin.contracts.parsing.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

// requires / provides without block: KFunction<*> parsing
internal class PsiFactParser(
    collector: ContractParsingDiagnosticsCollector,
    callContext: ContractCallContext,
    dispatcher: PsiContractParserDispatcher
) : AbstractPsiEffectParser(collector, callContext, dispatcher) {
    override fun tryParseEffect(expression: KtExpression): Collection<EffectDeclaration> {
        if (expression !is KtCallExpression) return emptyList()

        val bindingContext = callContext.bindingContext

        val resolvedCall = expression.getResolvedCall(bindingContext) ?: return emptyList()
        val descriptor = resolvedCall.resultingDescriptor

        val argumentExpression = resolvedCall.firstArgumentAsExpressionOrNull() ?: return emptyList()
        val owner = expression.parents.firstOrNull { it is KtNamedFunction } as? KtNamedFunction ?: return emptyList()
        val ownerDescriptor = bindingContext[BindingContext.FUNCTION, owner] ?: return emptyList()

        val descriptorName = descriptor.name

        return when {
            descriptor.isVerifierOrCleanerDescriptor() -> {
                val declarations = mutableListOf<EffectDeclaration?>()

                declarations += parseContextVerifierDeclaration(
                    argumentExpression,
                    descriptorName,
                    bindingContext,
                    contractParserDispatcher
                )
                    ?.let { ContextVerifierEffectDeclaration(it, it.references, FunctionReference(ownerDescriptor)) }

                declarations += parseContextCleanerDeclaration(argumentExpression, descriptorName, bindingContext, contractParserDispatcher)
                    ?.let { ContextCleanerEffectDeclaration(it, it.references, FunctionReference(ownerDescriptor)) }

                declarations.filterNotNull()
            }

            descriptor.isProviderDescriptor() -> {
                val declaration =
                    parseContextProviderDeclaration(argumentExpression, descriptorName, bindingContext, contractParserDispatcher)
                        ?: return emptyList()
                listOf(ContextProviderEffectDeclaration(declaration, declaration.references, FunctionReference(ownerDescriptor)))
            }

            else -> emptyList()
        }
    }
}

// requires / provides with block: KFunction<*> parsing
internal class PsiLambdaFactParser(
    collector: ContractParsingDiagnosticsCollector,
    callContext: ContractCallContext,
    dispatcher: PsiContractParserDispatcher
) : AbstractPsiEffectParser(collector, callContext, dispatcher) {
    override fun tryParseEffect(expression: KtExpression): Collection<EffectDeclaration> {
        if (expression !is KtCallExpression) return emptyList()

        val bindingContext = callContext.bindingContext

        val resolvedCall = expression.getResolvedCall(bindingContext) ?: return emptyList()
        val descriptor = resolvedCall.resultingDescriptor

        val ownerExpression = resolvedCall.argumentAsExpressionOrNull(0) ?: return emptyList()
        val owner = contractParserDispatcher.parseVariable(ownerExpression) ?: return emptyList()

        val argumentExpression = resolvedCall.argumentAsExpressionOrNull(1) ?: return emptyList()

        val descriptorName = descriptor.name

        return when {
            descriptor.isVerifierOrCleanerDescriptor() -> {
                val declarations = mutableListOf<EffectDeclaration?>()

                declarations += parseContextVerifierDeclaration(
                    argumentExpression,
                    descriptorName,
                    bindingContext,
                    contractParserDispatcher
                )
                    ?.let { LambdaContextVerifierEffectDeclaration(it, it.references, owner) }

                declarations += parseContextCleanerDeclaration(argumentExpression, descriptorName, bindingContext, contractParserDispatcher)
                    ?.let { LambdaContextCleanerEffectDeclaration(it, it.references, owner) }

                declarations.filterNotNull()
            }

            descriptor.isProviderDescriptor() -> {
                val declaration =
                    parseContextProviderDeclaration(argumentExpression, descriptorName, bindingContext, contractParserDispatcher)
                        ?: return emptyList()
                listOf(LambdaContextProviderEffectDeclaration(declaration, declaration.references, owner))
            }

            else -> emptyList()
        }
    }
}


// Declaration of Fact/Checker parsing
internal fun <T : ContextEntityDeclaration> parseAbstractFactoryDeclaration(
    expression: KtExpression,
    name: Name,
    bindingContext: BindingContext,
    dispatcher: PsiContractParserDispatcher,
    parseFunc: PsiEffectDeclarationExtractor.(KtExpression, Name) -> T?
): T? {
    val parsers = FactsEffectSystem.getParsers()
    return parsers.asSequence()
        .map { it(bindingContext, dispatcher) }
        .map { it.parseFunc(expression, name) }
        .filterNotNull()
        .firstOrNull()
}

internal fun parseContextProviderDeclaration(
    expression: KtExpression,
    name: Name,
    bindingContext: BindingContext,
    dispatcher: PsiContractParserDispatcher
): ContextDeclaration? = parseAbstractFactoryDeclaration(
    expression,
    name,
    bindingContext,
    dispatcher,
    PsiEffectDeclarationExtractor::extractContextDeclaration
)

internal fun parseContextVerifierDeclaration(
    expression: KtExpression,
    name: Name,
    bindingContext: BindingContext,
    dispatcher: PsiContractParserDispatcher
): VerifierDeclaration? = parseAbstractFactoryDeclaration(
    expression,
    name,
    bindingContext,
    dispatcher,
    PsiEffectDeclarationExtractor::extractVerifierDeclaration
)

internal fun parseContextCleanerDeclaration(
    expression: KtExpression,
    name: Name,
    bindingContext: BindingContext,
    dispatcher: PsiContractParserDispatcher
): CleanerDeclaration? = parseAbstractFactoryDeclaration(
    expression,
    name,
    bindingContext,
    dispatcher,
    PsiEffectDeclarationExtractor::extractCleanerDeclaration
)