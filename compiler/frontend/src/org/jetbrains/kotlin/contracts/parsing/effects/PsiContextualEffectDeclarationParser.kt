/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing.effects

import org.jetbrains.kotlin.contracts.ContextualEffectSystem
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectSupplier
import org.jetbrains.kotlin.contracts.description.EffectDeclaration
import org.jetbrains.kotlin.contracts.parsing.AbstractPsiEffectParser
import org.jetbrains.kotlin.contracts.parsing.ContextualEffectParser
import org.jetbrains.kotlin.contracts.parsing.PsiContractParserDispatcher
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingTrace

/**
 * parses part of contract: supplies(**that part**)
 */
internal abstract class AbstractPsiContextualEffectDeclarationParser<T : EffectDeclaration>(
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
) : AbstractPsiEffectParser(trace, dispatcher) {
    override fun tryParseEffect(expression: KtExpression): EffectDeclaration? {
        return ContextualEffectSystem.getParsers().asSequence()
            .map { it(trace.bindingContext) }
            .map { parser -> parse(parser, expression) }
            .filterNotNull()
            .firstOrNull()
    }

    abstract fun parse(parser: ContextualEffectParser, expression: KtExpression): T?
}

internal class PsiContextualEffectDeclarationSuppliesParser(
    trace: BindingTrace, dispatcher: PsiContractParserDispatcher
) : AbstractPsiContextualEffectDeclarationParser<ContextualEffectSupplier>(trace, dispatcher) {
    override fun parse(parser: ContextualEffectParser, expression: KtExpression) = parser.parseDeclarationForSupplier(expression)
}

internal class PsiContextualEffectDeclarationConsumesParser(
    trace: BindingTrace, dispatcher: PsiContractParserDispatcher
) : AbstractPsiContextualEffectDeclarationParser<ContextualEffectConsumer>(trace, dispatcher) {
    override fun parse(parser: ContextualEffectParser, expression: KtExpression) = parser.parseDeclarationForConsumer(expression)
}