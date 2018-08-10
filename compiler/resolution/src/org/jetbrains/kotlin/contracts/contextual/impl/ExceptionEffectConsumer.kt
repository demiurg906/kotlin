/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsHolder
import org.jetbrains.kotlin.contracts.contextual.EffectConsumer
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isSubtypeOf

class ExceptionEffectConsumer(private val consumedExceptionType: KotlinType) : EffectConsumer {
    override val family = ContextualEffectFamily.EXCEPTION

    override fun consume(context: ContextualEffectsHolder): ExceptionEffectsHolder {
        if (context !is ExceptionEffectsHolder) {
            throw AssertionError()
        }

        val newContext = mutableSetOf<KotlinType>()
        for (exceptionType in context.exceptions) {
            if (!exceptionType.isSubtypeOf(consumedExceptionType)) {
                newContext += exceptionType
            }
        }
        return ExceptionEffectsHolder(newContext)
    }

    override fun toString(): String {
        return "Consumer of $consumedExceptionType"
    }
}