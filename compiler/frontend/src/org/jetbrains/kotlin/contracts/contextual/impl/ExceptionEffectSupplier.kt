/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsHolder
import org.jetbrains.kotlin.contracts.contextual.EffectSupplier

class ExceptionEffectSupplier(val exception: String) : EffectSupplier {
    override val family = ContextualEffectFamily.EXCEPTION

    override fun supply(context: ContextualEffectsHolder): ContextualEffectsHolder {
        if (context !is ExceptionEffectsHolder) {
            throw AssertionError()
        }
        val effect = ExceptionEffect(exception)
        return if (effect in context.effects) {
            context
        } else {
            ExceptionEffectsHolder(context.effects + effect)
        }
    }
}