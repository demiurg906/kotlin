/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.EffectConsumer
import org.jetbrains.kotlin.contracts.contextual.EffectSupplier
import org.jetbrains.kotlin.contracts.contextual.impl.ExceptionEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.impl.ExceptionEffectSupplier
import org.jetbrains.kotlin.contracts.parsing.contextual.ContextualEffectParser
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

class ExceptionEffectParser(val context: BindingContext) : ContextualEffectParser {
    companion object {
        private const val EFFECT_NAME = "ExceptionEffectDescription"
    }

    override fun parseDeclarationForSupplier(declaration: KtCallExpression): EffectSupplier? {
        val exception = parseDeclaration(declaration) ?: return null
        return ExceptionEffectSupplier(exception)
    }

    override fun parseDeclarationForConsumer(declaration: KtCallExpression): EffectConsumer? {
        val exception = parseDeclaration(declaration) ?: return null
        return ExceptionEffectConsumer(exception)
    }

    private fun parseDeclaration(declaration: KtCallExpression): String? {
        val call = declaration.getResolvedCall(context) ?: return null
        val argument = call.valueArguments.entries.first().value
        val splitted = argument.toString().split("(\"", "\")")
        val name = splitted[0]
        if (name != EFFECT_NAME) {
            return null
        }
        return splitted[1]
    }
}