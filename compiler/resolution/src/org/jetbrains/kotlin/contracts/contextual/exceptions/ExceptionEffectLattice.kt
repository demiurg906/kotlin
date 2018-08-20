/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.exceptions

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectLattice
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext
import org.jetbrains.kotlin.contracts.description.InvocationKind

object ExceptionEffectLattice : ContextualEffectLattice {
    override fun and(a: ContextualEffectsContext, b: ContextualEffectsContext): ContextualEffectsContext = merge(a, b)

    override fun or(a: ContextualEffectsContext, b: ContextualEffectsContext): ContextualEffectsContext = merge(a, b)

    private fun merge(a: ContextualEffectsContext, b: ContextualEffectsContext): ContextualEffectsContext {
        if (a !is ExceptionEffectsContext || b !is ExceptionEffectsContext) {
            throw IllegalArgumentException()
        }
        return ExceptionEffectsContext(a.exceptions + b.exceptions)
    }

    override fun bot(): ContextualEffectsContext {
        return ExceptionEffectsContext()
    }

    override fun top(): ContextualEffectsContext {
        return ExceptionEffectsContext()
    }

    override fun updateContextWithInvocationKind(context: ContextualEffectsContext, functionInvocationKind: InvocationKind): ContextualEffectsContext {
        return context
    }
}