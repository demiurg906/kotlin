/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.exceptions

import org.jetbrains.kotlin.contracts.contextual.old.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.old.ContextualEffectsContext
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isSubtypeOf

class ExceptionEffectConsumer(private val consumedExceptionType: KotlinType) : ContextualEffectConsumer() {
    override val family = ExceptionEffectFamily

    override fun consume(context: ContextualEffectsContext): Pair<ContextualEffectsContext, String?> {
        if (context !is ExceptionEffectsContext) {
            throw AssertionError()
        }
        val newContext = context.exceptions.filterNotTo(mutableSetOf()) { exceptionType ->
            exceptionType.isSubtypeOf(consumedExceptionType)
        }
        return ExceptionEffectsContext(newContext) to null
    }

    override fun toString(): String {
        return "Consumer of $consumedExceptionType"
    }
}