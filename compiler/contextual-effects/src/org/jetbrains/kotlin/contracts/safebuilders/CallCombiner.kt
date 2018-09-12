/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.description.InvocationKind.EXACTLY_ONCE
import org.jetbrains.kotlin.contracts.description.InvocationKind.ZERO
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextCombiner
import org.jetbrains.kotlin.contracts.facts.ContextProvider

object CallCombiner : ContextCombiner {
    override fun or(a: Context, b: Context): Context {
        if (a !is CallContext || b !is CallContext) throw AssertionError()

        val functions = a.calls.keys union b.calls.keys
        val updatedCalls = functions.mapNotNull { functionReference ->
            val aInfo = a.calls[functionReference]
            val bInfo = b.calls[functionReference]

            val aKind = aInfo?.kind ?: InvocationKind.ZERO
            val bKind = bInfo?.kind ?: InvocationKind.ZERO
            val resKind = InvocationKind.or(aKind, bKind)

            if (resKind == ZERO) {
                return@mapNotNull null
            }

            val sourceElement = aInfo?.sourceElement ?: bInfo?.sourceElement ?: throw AssertionError()

            functionReference to CallInfo(sourceElement, resKind)
        }.toMap()
        return CallContext(updatedCalls)
    }

    override fun combine(context: Context, provider: ContextProvider): Context {
        if (context !is CallContext || provider !is CallContextProvider) throw AssertionError()

        val (functionReference, sourceElement) = provider
        val calls = context.calls.toMutableMap()
        if (functionReference in calls) {
            val callInfo = calls[functionReference]!!
            val kind = callInfo.kind
            calls[functionReference] = CallInfo(callInfo.sourceElement, InvocationKind.combine(kind, EXACTLY_ONCE))
        } else {
            calls[functionReference] = CallInfo(sourceElement, EXACTLY_ONCE)
        }

        return CallContext(calls)
    }
}