/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing.effects

import org.jetbrains.kotlin.contracts.ContextualEffectSystem
import org.jetbrains.kotlin.contracts.description.ConsumesContextualEffectDeclaration
import org.jetbrains.kotlin.contracts.description.EffectDeclaration
import org.jetbrains.kotlin.contracts.description.SuppliesContextualEffectDeclaration
import org.jetbrains.kotlin.contracts.parsing.AbstractPsiEffectParser
import org.jetbrains.kotlin.contracts.parsing.PsiContractParserDispatcher
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingTrace

internal class PsiConsumesEffectParser(
    trace: BindingTrace,
    dispatcher: PsiContractParserDispatcher
) : AbstractPsiEffectParser(trace, dispatcher) {
    override fun tryParseEffect(expression: KtExpression): EffectDeclaration? {
        if (expression !is KtCallExpression) {
            return null
        }
        val consumer = ContextualEffectSystem.ALL_PARSERS.asSequence()
            .map { it(trace.bindingContext) }
            .map { parser -> parser.parseDeclarationForConsumer(expression) }
            .filterNotNull()
            .firstOrNull() ?: return null
        return ConsumesContextualEffectDeclaration(consumer)
    }
}
