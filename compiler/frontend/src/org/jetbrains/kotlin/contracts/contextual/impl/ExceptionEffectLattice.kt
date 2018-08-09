/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsHolder
import org.jetbrains.kotlin.contracts.contextual.EffectLattice
import org.jetbrains.kotlin.contracts.description.InvocationKind

object ExceptionEffectLattice : EffectLattice {
    override val family = ContextualEffectFamily.EXCEPTION

    override fun and(a: ContextualEffectsHolder, b: ContextualEffectsHolder): ContextualEffectsHolder {
        TODO("not implemented")
    }

    override fun or(a: ContextualEffectsHolder, b: ContextualEffectsHolder): ContextualEffectsHolder {
        if (a !is ExceptionEffectsHolder || b !is ExceptionEffectsHolder) {
            throw IllegalArgumentException()
        }
        return ExceptionEffectsHolder(a.effects + b.effects)
    }

    override fun bot(): ContextualEffectsHolder {
        return ExceptionEffectsHolder()
    }

    override fun top(): ContextualEffectsHolder {
        TODO("not implemented")
    }

    override fun updateContextWithInvocationKind(context: ContextualEffectsHolder, invocationKind: InvocationKind): ContextualEffectsHolder {
        return context
    }
}