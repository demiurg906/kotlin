/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectLattice
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext
import org.jetbrains.kotlin.contracts.description.InvocationKind

object ExceptionEffectLattice : ContextualEffectLattice {
    override val family = ExceptionEffectFamily()

    override fun and(a: ContextualEffectsContext, b: ContextualEffectsContext): ContextualEffectsContext {
        TODO("not implemented")
    }

    override fun or(a: ContextualEffectsContext, b: ContextualEffectsContext): ContextualEffectsContext {
        if (a !is ExceptionEffectsContext || b !is ExceptionEffectsContext) {
            throw IllegalArgumentException()
        }
        return ExceptionEffectsContext(a.exceptions + b.exceptions)
    }

    override fun bot(): ContextualEffectsContext {
        return ExceptionEffectsContext()
    }

    override fun top(): ContextualEffectsContext {
        TODO("not implemented")
    }

    override fun updateContextWithInvocationKind(context: ContextualEffectsContext, invocationKind: InvocationKind): ContextualEffectsContext {
        return context
    }
}