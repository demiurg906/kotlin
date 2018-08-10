/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsHolder
import org.jetbrains.kotlin.contracts.contextual.EffectConsumer

class ExceptionEffectConsumer(val exception: String) : EffectConsumer {
    override val family = ContextualEffectFamily.EXCEPTION

    override fun consume(context: ContextualEffectsHolder): ExceptionEffectsHolder {
        if (context !is ExceptionEffectsHolder) {
            throw AssertionError()
        }

        val effect = ExceptionEffect(exception)
        if (effect in context.effects) {
            val effects = context.effects.toMutableSet()
            effects.remove(effect)
            return ExceptionEffectsHolder(effects)
        }
        return context
    }

    override fun toString(): String {
        return "Consumer of $exception"
    }
}