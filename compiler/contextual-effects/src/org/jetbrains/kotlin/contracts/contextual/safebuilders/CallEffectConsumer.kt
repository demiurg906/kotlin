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
        assert(expectedCallKind != InvocationKind.ZERO)
    }

    override val family = CallEffectFamily

    override fun consume(context: ContextualEffectsContext): Pair<ContextualEffectsContext, String?> {
        if (context !is CallEffectsContext) throw AssertionError()

        val actualCallKind = context.calls[function] ?: InvocationKind.ZERO
        val resultCalls = context.calls.minus(function)

        val warning = if (!CallEffectLattice.check(
                expectedCallKind,
                actualCallKind
            )
        ) "${function.name} call mismatch: expected $expectedCallKind, actual $actualCallKind" else null

        return CallEffectsContext(resultCalls) to warning
    }

    override fun toString(): String = "Call consumer: ${function.name} must be invoked $expectedCallKind"
}

data class CallAnalysisResult(val expected: InvocationKind, val actual: InvocationKind)