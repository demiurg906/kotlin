/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing.effects

import org.jetbrains.kotlin.contracts.contextual.old.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.old.ContextualEffectSupplier
import org.jetbrains.kotlin.contracts.description.BlockConsumesContextualEffectDeclaration
import org.jetbrains.kotlin.contracts.description.BlockSuppliesContextualEffectDeclaration
import org.jetbrains.kotlin.contracts.description.EffectDeclaration
import org.jetbrains.kotlin.contracts.description.expressions.VariableReference
import org.jetbrains.kotlin.contracts.parsing.AbstractPsiEffectParser
import org.jetbrains.kotlin.contracts.parsing.PsiContractParserDispatcher
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ExpressionValueArgument


internal abstract class AbstractPsiProvideContextualEffectParser<T : EffectDeclaration, U : EffectDeclaration>(
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
) : AbstractPsiEffectParser(trace, dispatcher) {
    override fun tryParseEffect(expression: KtExpression): U? {
        val call = expression.getResolvedCall(trace.bindingContext) ?: return null
        val args = call.valueArgumentsByIndex ?: return null

        val blockElement = (args.getOrNull(0) as? ExpressionValueArgument)
            ?.valueArgument?.getArgumentExpression() ?: return null
        val blockVariable = contractParserDispatcher.parseVariable(blockElement) ?: return null

        val effectElement = (args.getOrNull(1) as? ExpressionValueArgument)
            ?.valueArgument?.getArgumentExpression() ?: return null
        val effect = parseEffect(effectElement) ?: return null

        return newEffectDeclaration(blockVariable, effect)
    }

    protected abstract fun newEffectDeclaration(variableReference: VariableReference, entity: T): U

    protected abstract fun parseEffect(expression: KtExpression): T?
}

internal class PsiProvideSuppliesEffectParser(
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
) : AbstractPsiProvideContextualEffectParser<ContextualEffectSupplier, BlockSuppliesContextualEffectDeclaration>(trace, dispatcher) {

    override fun newEffectDeclaration(variableReference: VariableReference, entity: ContextualEffectSupplier) =
        BlockSuppliesContextualEffectDeclaration(variableReference, entity)

    override fun parseEffect(expression: KtExpression) = effectParser.tryParseEffect(expression) as? ContextualEffectSupplier

    private val effectParser = PsiContextualEffectDeclarationSuppliesParser(trace, dispatcher)
}

internal class PsiProvideConsumesEffectParser(
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
) : AbstractPsiProvideContextualEffectParser<ContextualEffectConsumer, BlockConsumesContextualEffectDeclaration>(trace, dispatcher) {

    override fun newEffectDeclaration(variableReference: VariableReference, entity: ContextualEffectConsumer) =
        BlockConsumesContextualEffectDeclaration(variableReference, entity)

    override fun parseEffect(expression: KtExpression) = effectParser.tryParseEffect(expression) as? ContextualEffectConsumer

    private val effectParser = PsiContextualEffectDeclarationConsumesParser(trace, dispatcher)
}

//internal class PsiProvideConsumesEffectParser(
//    trace: BindingTrace,
//    dispatcher: PsiContractParserDispatcher
//) : AbstractPsiEffectParser(trace, dispatcher) {
//
//    override fun tryParseEffect(expression: KtExpression): EffectDeclaration? {
//        val call = expression.getResolvedCall(trace.bindingContext) ?: return null
//        val args = call.valueArgumentsByIndex ?: return null
//
//        val blockElement = (args.getOrNull(0) as? ExpressionValueArgument)
//            ?.valueArgument?.getArgumentExpression() ?: return null
//        val blockVariable = contractParserDispatcher.parseVariable(blockElement) ?: return null
//
//        val effectElement = (args.getOrNull(1) as? ExpressionValueArgument)
//            ?.valueArgument?.getArgumentExpression() ?: return null
//        val effect = effectParser.tryParseEffect(effectElement) as? ContextualEffectConsumer ?: return null
//
//        return BlockConsumesContextualEffectDeclaration(blockVariable, effect)
//    }
//
//    val effectParser = PsiContextualEffectDeclarationConsumesParser(trace, dispatcher)
//}
