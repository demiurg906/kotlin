/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing.effects

import org.jetbrains.kotlin.contracts.FactsEffectSystem
import org.jetbrains.kotlin.contracts.description.*
import org.jetbrains.kotlin.contracts.description.expressions.FunctionReference
import org.jetbrains.kotlin.contracts.facts.ContextDeclaration
import org.jetbrains.kotlin.contracts.facts.ContextEntityDeclaration
import org.jetbrains.kotlin.contracts.facts.VerifierDeclaration
import org.jetbrains.kotlin.contracts.parsing.*
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
    override fun tryParseEffect(expression: KtExpression): EffectDeclaration? {
        if (expression !is KtCallExpression) return null

        val bindingContext = callContext.bindingContext

        val resolvedCall = expression.getResolvedCall(bindingContext) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        val argumentExpression = resolvedCall.firstArgumentAsExpressionOrNull() ?: return null
        val owner = expression.parents.firstOrNull { it is KtNamedFunction } as? KtNamedFunction ?: return null
        val ownerDescriptor = bindingContext[BindingContext.FUNCTION, owner] ?: return null

        return when {
            descriptor.isRequiresContextDescriptor() || descriptor.isRequiresNotContextDescriptor() -> {
                val declaration = parseContextCheckerFactoryDeclaration(argumentExpression, bindingContext, contractParserDispatcher) ?: return null
                RequiresContextEffectDeclaration(declaration, declaration.references, FunctionReference(ownerDescriptor))
            }

            descriptor.isProvidesFactDescriptor() -> {
                val declaration = parseContextFactFactoryDeclaration(argumentExpression, bindingContext, contractParserDispatcher) ?: return null
                ProvidesFactEffectDeclaration(declaration, declaration.references, FunctionReference(ownerDescriptor))
            }

            else -> null
        }
    }
}

// requires / provides with block: KFunction<*> parsing
internal class PsiLambdaFactParser(
    collector: ContractParsingDiagnosticsCollector,
    callContext: ContractCallContext,
    dispatcher: PsiContractParserDispatcher
) : AbstractPsiEffectParser(collector, callContext, dispatcher) {
    override fun tryParseEffect(expression: KtExpression): EffectDeclaration? {
        if (expression !is KtCallExpression) return null

        val bindingContext = callContext.bindingContext

        val resolvedCall = expression.getResolvedCall(bindingContext) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        val ownerExpression = resolvedCall.argumentAsExpressionOrNull(0) ?: return null
        val owner = contractParserDispatcher.parseVariable(ownerExpression) ?: return null

        val argumentExpression = resolvedCall.argumentAsExpressionOrNull(1) ?: return null

        return when {
            descriptor.isRequiresContextDescriptor() || descriptor.isRequiresNotContextDescriptor() -> {
                val declaration = parseContextCheckerFactoryDeclaration(argumentExpression, bindingContext, contractParserDispatcher) ?: return null
                LambdaRequiresContextEffectDeclaration(declaration, declaration.references, owner)
            }

            descriptor.isProvidesFactDescriptor() -> {
                val declaration = parseContextFactFactoryDeclaration(argumentExpression, bindingContext, contractParserDispatcher) ?: return null
                LambdaProvidesFactEffectDeclaration(declaration, declaration.references, owner)
            }

            else -> null
        }
    }
}


// Declaration of Fact/Checker parsing
internal fun <T : ContextEntityDeclaration> parseAbstractFactoryDeclaration(
    expression: KtExpression,
    bindingContext: BindingContext,
    dispatcher: PsiContractParserDispatcher,
    parseFunc: PsiEffectDeclarationExtractor.(KtExpression) -> T?
): T? {
    val parsers = FactsEffectSystem.getParsers()
    return parsers.asSequence()
        .map { it(bindingContext, dispatcher) }
        .map { it.parseFunc(expression) }
        .filterNotNull()
        .firstOrNull()
}

internal fun parseContextFactFactoryDeclaration(
    expression: KtExpression,
    bindingContext: BindingContext,
    dispatcher: PsiContractParserDispatcher
): ContextDeclaration? = parseAbstractFactoryDeclaration(
    expression,
    bindingContext,
    dispatcher,
    PsiEffectDeclarationExtractor::extractContextDeclaration
)

internal fun parseContextCheckerFactoryDeclaration(
    expression: KtExpression,
    bindingContext: BindingContext,
    dispatcher: PsiContractParserDispatcher
): VerifierDeclaration? = parseAbstractFactoryDeclaration(
    expression,
    bindingContext,
    dispatcher,
    PsiEffectDeclarationExtractor::extractVerifierDeclaration
)