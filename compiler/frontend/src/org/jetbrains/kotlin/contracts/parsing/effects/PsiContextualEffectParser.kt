/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing.effects

import org.jetbrains.kotlin.contracts.ContextualEffectSystem
import org.jetbrains.kotlin.contracts.contextual.EffectConsumer
import org.jetbrains.kotlin.contracts.contextual.EffectSupplier
import org.jetbrains.kotlin.contracts.description.ConsumesContextualEffectDeclaration
import org.jetbrains.kotlin.contracts.description.EffectDeclaration
import org.jetbrains.kotlin.contracts.description.SuppliesContextualEffectDeclaration
import org.jetbrains.kotlin.contracts.parsing.AbstractPsiEffectParser
import org.jetbrains.kotlin.contracts.parsing.PsiContractParserDispatcher
import org.jetbrains.kotlin.contracts.parsing.contextual.ContextualEffectParser
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingTrace

internal abstract class PsiContextualEffectParser<T : Any>(
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher,
    private val effectDeclarationConstructor: (T) -> EffectDeclaration,
    private val parseFunction: (ContextualEffectParser, KtCallExpression) -> T?
) : AbstractPsiEffectParser(trace, dispatcher) {
    override fun tryParseEffect(expression: KtExpression): EffectDeclaration? {
        if (expression !is KtCallExpression) {
            return null
        }
        val entity = ContextualEffectSystem.ALL_PARSERS.asSequence()
            .map { it(trace.bindingContext) }
            .map { parser -> parseFunction(parser, expression) }
            .filterNotNull()
            .firstOrNull() ?: return null
        return effectDeclarationConstructor(entity)
    }
}

internal class PsiConsumesEffectParser(
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
) : PsiContextualEffectParser<EffectConsumer>(
    trace,
    dispatcher,
    ::ConsumesContextualEffectDeclaration,
    ContextualEffectParser::parseDeclarationForConsumer
)

internal class PsiSuppliesEffectParser(
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
) : PsiContextualEffectParser<EffectSupplier>(
    trace,
    dispatcher,
    ::SuppliesContextualEffectDeclaration,
    ContextualEffectParser::parseDeclarationForSupplier
)