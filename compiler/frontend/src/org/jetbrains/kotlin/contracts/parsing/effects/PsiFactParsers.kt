/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing.effects

import org.jetbrains.kotlin.contracts.FactsEffectSystem
import org.jetbrains.kotlin.contracts.description.*
import org.jetbrains.kotlin.contracts.description.expressions.FunctionReference
import org.jetbrains.kotlin.contracts.description.expressions.VariableReference
import org.jetbrains.kotlin.contracts.parsing.*
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

// requires / provides without block: KFunction<*> parsing
internal class PsiFactParser(
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
) : AbstractPsiEffectParser(trace, dispatcher) { //AbstractPsiFactParser(trace, dispatcher) {
    override fun tryParseEffect(expression: KtExpression): EffectDeclaration? {
        if (expression !is KtCallExpression) return null

        val resolvedCall = expression.getResolvedCall(trace.bindingContext) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        val argumentExpression = resolvedCall.firstArgumentAsExpressionOrNull() ?: return null
        val owner = expression.parents.firstOrNull { it is KtNamedFunction } as? KtNamedFunction ?: return null
        val ownerDescriptor = trace.bindingContext[BindingContext.FUNCTION, owner] ?: return null

        return when {
            descriptor.isRequiresContextDescriptor() || descriptor.isRequiresNotContextDescriptor() -> {
                val (factory, references) = parseContextCheckerFactoryDeclaration(argumentExpression, trace, contractParserDispatcher) ?: return null
                RequiresContextEffectDeclaration(factory, references, FunctionReference(ownerDescriptor))
            }

            descriptor.isProvidesFactDescriptor() -> {
                val (factory, references) = parseContextFactFactoryDeclaration(argumentExpression, trace, contractParserDispatcher) ?: return null
                ProvidesFactEffectDeclaration(factory, references, FunctionReference(ownerDescriptor))
            }

            else -> null
        }
    }
}

// requires / provides with block: KFunction<*> parsing
internal class PsiLambdaFactParser(
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
) : AbstractPsiEffectParser(trace, dispatcher) {
    override fun tryParseEffect(expression: KtExpression): EffectDeclaration? {
        if (expression !is KtCallExpression) return null

        val resolvedCall = expression.getResolvedCall(trace.bindingContext) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        val ownerExpression = resolvedCall.argumentAsExpressionOrNull(0) ?: return null
        val owner = contractParserDispatcher.parseVariable(ownerExpression) ?: return null

        val argumentExpression = resolvedCall.argumentAsExpressionOrNull(1) ?: return null

        return when {
            descriptor.isRequiresContextDescriptor() || descriptor.isRequiresNotContextDescriptor() -> {
                val (factory, references) = parseContextCheckerFactoryDeclaration(argumentExpression, trace, contractParserDispatcher) ?: return null
                LambdaRequiresContextEffectDeclaration(factory, references, owner)
            }

            descriptor.isProvidesFactDescriptor() -> {
                val (factory, references) = parseContextFactFactoryDeclaration(argumentExpression, trace, contractParserDispatcher) ?: return null
                LambdaProvidesFactEffectDeclaration(factory, references, owner)
            }

            else -> null
        }
    }
}


// Declaration of Fact/Checker parsing
internal fun <T : ContextEntityFactory> parseAbstractFactoryDeclaration(
    expression: KtExpression,
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher,
    parseFunc: ContextFactParser.(KtExpression) -> Pair<T, List<VariableReference>>?
): Pair<T, List<VariableReference>>? {
    val parsers = FactsEffectSystem.getParsers()
    return parsers.asSequence()
        .map { it(trace.bindingContext, dispatcher) }
        .map { it.parseFunc(expression) }
        .filterNotNull()
        .firstOrNull() ?: return null
}

internal fun parseContextFactFactoryDeclaration(
    expression: KtExpression,
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
): Pair<ContextFactFactoryDeclaration, List<VariableReference>>? = parseAbstractFactoryDeclaration(
    expression,
    trace,
    dispatcher,
    ContextFactParser::parseDeclarationForFactFactory
)

internal fun parseContextCheckerFactoryDeclaration(
    expression: KtExpression,
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
): Pair<ContextCheckerFactoryDeclaration, List<VariableReference>>? = parseAbstractFactoryDeclaration(
    expression,
    trace,
    dispatcher,
    ContextFactParser::parseDeclarationForCheckerFactory
)