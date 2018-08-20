/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.safebuilders

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext
import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

class CallEffectConsumer(val function: FunctionDescriptor, val expectedCallKind: InvocationKind) : ContextualEffectConsumer() {
    init {
        assert(expectedCallKind != InvocationKind.UNKNOWN)
    }

    override val family = CallEffectFamily

    override fun consume(context: ContextualEffectsContext): ContextualEffectsContext {
        if (context !is CallEffectsContext) throw AssertionError()

        val actualCallKind = context.calls[function] ?: InvocationKind.ZERO
        val resultCalls = context.calls.minus(function)

        return if (CallEffectLattice.check(expectedCallKind, actualCallKind)) {
            CallEffectsContext(resultCalls, context.badCalls)
        } else {
            CallEffectsContext(resultCalls, context.badCalls.plus(function to listOf(CallAnalysisResult(expectedCallKind, actualCallKind))))
        }
    }

    override fun toString(): String = "Call consumer: ${function.name} must be invoked $expectedCallKind"
}

data class CallAnalysisResult(val expected: InvocationKind, val actual: InvocationKind)