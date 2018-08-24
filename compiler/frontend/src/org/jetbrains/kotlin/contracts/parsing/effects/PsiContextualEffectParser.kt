/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing.effects

import org.jetbrains.kotlin.contracts.contextual.old.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.old.ContextualEffectSupplier
import org.jetbrains.kotlin.contracts.description.EffectDeclaration
import org.jetbrains.kotlin.contracts.parsing.*
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

/**
 * Parses T ([ContextualEffectSupplier] or [ContextualEffectConsumer]) from psi declaration
 */
internal abstract class PsiContextualEffectParser<T : EffectDeclaration>(
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
) : AbstractPsiEffectParser(trace, dispatcher) {
    protected abstract val effectParser: AbstractPsiContextualEffectDeclarationParser<T>

    protected abstract fun CallableDescriptor.checker(): Boolean

    override fun tryParseEffect(expression: KtExpression): EffectDeclaration? {
        if (expression !is KtCallExpression) {
            return null
        }

        val resolvedCall = expression.getResolvedCall(trace.bindingContext) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        if (!descriptor.checker()) return null
        val argumentExpression = resolvedCall.firstArgumentAsExpressionOrNull() ?: return null

        return effectParser.tryParseEffect(argumentExpression)
    }
}


internal class PsiConsumesEffectParser(
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
) : PsiContextualEffectParser<ContextualEffectConsumer>(trace, dispatcher) {
    override val effectParser = PsiContextualEffectDeclarationConsumesParser(trace, dispatcher)
    override fun CallableDescriptor.checker() = isConsumesEffectDescriptor()
}


internal class PsiSuppliesEffectParser(
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
) : PsiContextualEffectParser<ContextualEffectSupplier>(
    trace,
    dispatcher
) {
    override val effectParser = PsiContextualEffectDeclarationSuppliesParser(trace, dispatcher)
    override fun CallableDescriptor.checker() = isSuppliesEffectDescriptor()
}