/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextChecker
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.BindingTrace

class CallChecker(
    val functionReference: FunctionReference,
    val expectedKind: InvocationKind,
    val calledElement: KtElement
) :
    ContextChecker() {
    override val family = CallFamily

    override fun verifyContext(context: Context, trace: BindingTrace, shouldReport: Boolean): Context {
        if (context !is CallContext) throw AssertionError()

        val calls = context.calls.toMutableMap()

        val actualKind = calls.remove(functionReference)?.kind ?: InvocationKind.ZERO
        if (!isSatisfied(expectedKind, actualKind) && shouldReport) {
            val message = "${functionReference.functionDescriptor.name} call mismatch: expected $expectedKind, actual $actualKind"
            trace.report(Errors.CONTEXTUAL_EFFECT_WARNING.on(calledElement, message))
        }

        return CallContext(calls)
    }

    private fun isSatisfied(expected: InvocationKind, actual: InvocationKind): Boolean {
        if (expected == InvocationKind.ZERO || expected == InvocationKind.UNKNOWN) throw AssertionError()
        if (actual == InvocationKind.UNKNOWN) return false

        if (actual == expected) return true
        if (expected == InvocationKind.AT_MOST_ONCE && (actual == InvocationKind.ZERO || actual == InvocationKind.EXACTLY_ONCE)) return true
        if (expected == InvocationKind.AT_LEAST_ONCE && actual == InvocationKind.EXACTLY_ONCE) return true

        return false
    }
}