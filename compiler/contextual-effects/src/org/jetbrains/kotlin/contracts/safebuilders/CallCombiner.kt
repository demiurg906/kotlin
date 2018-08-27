/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextFact
import org.jetbrains.kotlin.contracts.facts.FactsCombiner

object CallCombiner : FactsCombiner() {
    override fun or(a: Context, b: Context): Context {
        if (a !is CallContext || b !is CallContext) throw AssertionError()

        val functions = a.calls.keys union b.calls.keys
        val updatedCalls = functions.map { functionReference ->
            val aFact = a.calls[functionReference]
            val bFact = b.calls[functionReference]

            val aKind = aFact?.kind ?: InvocationKind.ZERO
            val bKind = bFact?.kind ?: InvocationKind.ZERO
            val resKind = or(aKind, bKind)

            val calledElement = aFact?.calledElement ?: bFact?.calledElement ?: throw AssertionError()

            functionReference to CallFact(functionReference, calledElement, resKind)
        }.toMap()
        return CallContext(updatedCalls)
    }

    override fun combine(context: Context, fact: ContextFact): Context {
        if (context !is CallContext || fact !is CallFact) throw AssertionError()

        val (function, calledElement, kind) = fact
        val calls = context.calls.toMutableMap()
        calls[function] = if (function in calls) {
            val (_, factCalledElement, factKind) = calls[function]!!
            val resKind = combine(factKind, kind)
            CallFact(function, factCalledElement, resKind)
        } else {
            fact
        }
        return CallContext(calls)
    }

    override fun updateWithInvocationKind(context: Context, invocationKind: InvocationKind): Context {
        if (context !is CallContext) throw AssertionError()

        val updatedCalls = context.calls.mapValues { (_, fact) ->
            val newKind = updateWithCallKind(fact.kind, invocationKind)
            CallFact(fact.functionReference, fact.calledElement, newKind)
        }
        return CallContext(updatedCalls)
    }

    private fun or(x: InvocationKind, y: InvocationKind) = when (x) {
        InvocationKind.ZERO -> when (y) {
            InvocationKind.ZERO -> InvocationKind.ZERO
            InvocationKind.AT_MOST_ONCE -> InvocationKind.AT_MOST_ONCE
            InvocationKind.EXACTLY_ONCE -> InvocationKind.AT_MOST_ONCE
            InvocationKind.AT_LEAST_ONCE -> InvocationKind.UNKNOWN
            InvocationKind.UNKNOWN -> InvocationKind.UNKNOWN
        }
        InvocationKind.AT_MOST_ONCE -> when (y) {
            InvocationKind.ZERO -> InvocationKind.AT_MOST_ONCE
            InvocationKind.AT_MOST_ONCE -> InvocationKind.AT_MOST_ONCE
            InvocationKind.EXACTLY_ONCE -> InvocationKind.AT_MOST_ONCE
            InvocationKind.AT_LEAST_ONCE -> InvocationKind.UNKNOWN
            InvocationKind.UNKNOWN -> InvocationKind.UNKNOWN
        }
        InvocationKind.EXACTLY_ONCE -> when (y) {
            InvocationKind.ZERO -> InvocationKind.AT_MOST_ONCE
            InvocationKind.AT_MOST_ONCE -> InvocationKind.AT_MOST_ONCE
            InvocationKind.EXACTLY_ONCE -> InvocationKind.EXACTLY_ONCE
            InvocationKind.AT_LEAST_ONCE -> InvocationKind.AT_LEAST_ONCE
            InvocationKind.UNKNOWN -> InvocationKind.UNKNOWN
        }
        InvocationKind.AT_LEAST_ONCE -> when (y) {
            InvocationKind.ZERO -> InvocationKind.UNKNOWN
            InvocationKind.AT_MOST_ONCE -> InvocationKind.UNKNOWN
            InvocationKind.EXACTLY_ONCE -> InvocationKind.AT_LEAST_ONCE
            InvocationKind.AT_LEAST_ONCE -> InvocationKind.AT_LEAST_ONCE
            InvocationKind.UNKNOWN -> InvocationKind.UNKNOWN
        }
        InvocationKind.UNKNOWN -> InvocationKind.UNKNOWN
    }

    private fun combine(x: InvocationKind, y: InvocationKind) = when (x) {
        InvocationKind.ZERO -> when (y) {
            InvocationKind.ZERO -> InvocationKind.ZERO
            InvocationKind.AT_MOST_ONCE -> InvocationKind.AT_MOST_ONCE
            InvocationKind.EXACTLY_ONCE -> InvocationKind.EXACTLY_ONCE
            InvocationKind.AT_LEAST_ONCE -> InvocationKind.AT_LEAST_ONCE
            InvocationKind.UNKNOWN -> InvocationKind.UNKNOWN
        }
        InvocationKind.AT_MOST_ONCE -> when (y) {
            InvocationKind.ZERO -> InvocationKind.AT_MOST_ONCE
            InvocationKind.AT_MOST_ONCE -> InvocationKind.UNKNOWN
            InvocationKind.EXACTLY_ONCE -> InvocationKind.AT_LEAST_ONCE
            InvocationKind.AT_LEAST_ONCE -> InvocationKind.AT_LEAST_ONCE
            InvocationKind.UNKNOWN -> InvocationKind.UNKNOWN
        }
        InvocationKind.EXACTLY_ONCE -> when (y) {
            InvocationKind.ZERO -> InvocationKind.EXACTLY_ONCE
            InvocationKind.AT_MOST_ONCE -> InvocationKind.AT_LEAST_ONCE
            InvocationKind.EXACTLY_ONCE -> InvocationKind.AT_LEAST_ONCE
            InvocationKind.AT_LEAST_ONCE -> InvocationKind.AT_LEAST_ONCE
            InvocationKind.UNKNOWN -> InvocationKind.UNKNOWN
        }
        InvocationKind.AT_LEAST_ONCE -> if (y == InvocationKind.UNKNOWN) InvocationKind.UNKNOWN else InvocationKind.AT_LEAST_ONCE
        InvocationKind.UNKNOWN -> InvocationKind.UNKNOWN
    }

    private fun updateWithCallKind(kind: InvocationKind, functionInvocationKind: InvocationKind) = when (functionInvocationKind) {
        InvocationKind.AT_MOST_ONCE -> when (kind) {
            InvocationKind.AT_MOST_ONCE -> InvocationKind.AT_MOST_ONCE
            InvocationKind.EXACTLY_ONCE -> InvocationKind.AT_MOST_ONCE
            InvocationKind.AT_LEAST_ONCE -> InvocationKind.UNKNOWN
            InvocationKind.ZERO -> InvocationKind.ZERO
            InvocationKind.UNKNOWN -> InvocationKind.UNKNOWN
        }
        InvocationKind.AT_LEAST_ONCE -> when (kind) {
            InvocationKind.AT_MOST_ONCE -> InvocationKind.UNKNOWN
            InvocationKind.EXACTLY_ONCE -> InvocationKind.AT_LEAST_ONCE
            InvocationKind.AT_LEAST_ONCE -> InvocationKind.AT_LEAST_ONCE
            InvocationKind.ZERO -> InvocationKind.ZERO
            InvocationKind.UNKNOWN -> InvocationKind.UNKNOWN
        }
        InvocationKind.EXACTLY_ONCE -> kind
        else -> throw AssertionError()
    }
}