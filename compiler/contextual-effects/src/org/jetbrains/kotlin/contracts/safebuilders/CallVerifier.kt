/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextVerifier
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtElement

class CallVerifier(
    val functionReference: FunctionReference,
    val expectedKind: InvocationKind,
    val sourceElement: KtElement
) : ContextVerifier() {
    override val family = CallFamily

    override fun verify(context: Context, diagnosticSink: DiagnosticSink) {
        val (_, actualKind) = extractKindFromContext(context)
        if (!isSatisfied(expectedKind, actualKind)) {
            val message = "${functionReference.functionDescriptor.name} call mismatch: expected $expectedKind, actual $actualKind"
            diagnosticSink.report(Errors.CONTEXTUAL_EFFECT_WARNING.on(sourceElement, message))
        }
    }

    override fun cleanupProcessed(context: Context): Context {
        val (calls, _) = extractKindFromContext(context)
        return CallContext(calls)
    }

    private fun extractKindFromContext(context: Context): Pair<Map<FunctionReference, CallInfo>, InvocationKind> {
        if (context !is CallContext) throw AssertionError()
        val calls = context.calls.toMutableMap()
        val kind = calls.remove(functionReference)?.kind ?: InvocationKind.ZERO
        return calls to kind
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