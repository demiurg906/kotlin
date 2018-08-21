/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.safebuilders

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectSupplier
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext
import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

class CallEffectSupplier(val function: FunctionDescriptor) : ContextualEffectSupplier() {
    override val family = CallEffectFamily

    override fun supply(context: ContextualEffectsContext): ContextualEffectsContext {
        if (context !is CallEffectsContext) throw AssertionError()
        val newCalls = context.calls.toMutableMap()
        if (function in context.calls) {
            val currentKind = newCalls[function]!!
            newCalls[function] = CallEffectLattice.combine(
                currentKind,
                InvocationKind.EXACTLY_ONCE
            )
        } else {
            newCalls[function] = InvocationKind.EXACTLY_ONCE
        }
        return CallEffectsContext(newCalls)
    }

    override fun toString() = "Call supplier: invokes ${function.name} once"
}