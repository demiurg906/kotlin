/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isSubtypeOf

class ExceptionEffectConsumer(private val consumedExceptionType: KotlinType) : ContextualEffectConsumer() {
    override val family = ExceptionEffectFamily

    override fun consume(context: ContextualEffectsContext): ExceptionEffectsContext {
        if (context !is ExceptionEffectsContext) {
            throw AssertionError()
        }

        val newContext = mutableSetOf<KotlinType>()
        // TODO: filterTo
        for (exceptionType in context.exceptions) {
            if (!exceptionType.isSubtypeOf(consumedExceptionType)) {
                newContext += exceptionType
            }
        }
        return ExceptionEffectsContext(newContext)
    }

    override fun toString(): String {
        return "Consumer of $consumedExceptionType"
    }
}