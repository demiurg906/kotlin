/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.safebuilders

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

class CallEffectConsumer(val function: FunctionDescriptor, val expectedCallKind: CallKind) : ContextualEffectConsumer() {
    init {
        assert(expectedCallKind != CallKind.ZERO)
    }

    override val family = CallEffectFamily

    override fun consume(context: ContextualEffectsContext): ContextualEffectsContext {
        if (context !is CallEffectsContext) throw AssertionError()

        val actualCallKind = context.calls[function] ?: CallKind.ZERO
        val resultCalls = context.calls.minus(function)

        return if (CallKind.check(expectedCallKind, actualCallKind)) {
            CallEffectsContext(resultCalls, context.badCalls)
        } else {
            CallEffectsContext(resultCalls, context.badCalls.plus(function to listOf(CallAnalysisResult(expectedCallKind, actualCallKind))))
        }
    }

    override fun toString(): String = "Call consumer: ${function.name} must be invoked $expectedCallKind"
}

data class CallAnalysisResult(val expected: CallKind, val actual: CallKind)