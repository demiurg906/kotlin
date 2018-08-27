/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.description.InvocationKind.*
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextFact
import org.jetbrains.kotlin.contracts.facts.FactsCombiner

object CallCombiner : FactsCombiner() {
    override fun or(a: Context, b: Context): Context {
        if (a !is CallContext || b !is CallContext) throw AssertionError()

        val functions = a.calls.keys union b.calls.keys
        val updatedCalls = functions.mapNotNull { functionReference ->
            val aFact = a.calls[functionReference]
            val bFact = b.calls[functionReference]

            val aKind = aFact?.kind ?: InvocationKind.ZERO
            val bKind = bFact?.kind ?: InvocationKind.ZERO
            val resKind = or(aKind, bKind)

            if (resKind == ZERO) {
                return@mapNotNull null
            }

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
        ZERO -> when (y) {
            ZERO -> ZERO
            AT_MOST_ONCE -> AT_MOST_ONCE
            EXACTLY_ONCE -> AT_MOST_ONCE
            AT_LEAST_ONCE -> UNKNOWN
            UNKNOWN -> UNKNOWN
        }
        AT_MOST_ONCE -> when (y) {
            ZERO -> AT_MOST_ONCE
            AT_MOST_ONCE -> AT_MOST_ONCE
            EXACTLY_ONCE -> AT_MOST_ONCE
            AT_LEAST_ONCE -> UNKNOWN
            UNKNOWN -> UNKNOWN
        }
        EXACTLY_ONCE -> when (y) {
            ZERO -> AT_MOST_ONCE
            AT_MOST_ONCE -> AT_MOST_ONCE
            EXACTLY_ONCE -> EXACTLY_ONCE
            AT_LEAST_ONCE -> AT_LEAST_ONCE
            UNKNOWN -> UNKNOWN
        }
        AT_LEAST_ONCE -> when (y) {
            ZERO -> UNKNOWN
            AT_MOST_ONCE -> UNKNOWN
            EXACTLY_ONCE -> AT_LEAST_ONCE
            AT_LEAST_ONCE -> AT_LEAST_ONCE
            UNKNOWN -> UNKNOWN
        }
        UNKNOWN -> UNKNOWN
    }

    private fun combine(x: InvocationKind, y: InvocationKind) = when (x) {
        ZERO -> when (y) {
            ZERO -> ZERO
            AT_MOST_ONCE -> AT_MOST_ONCE
            EXACTLY_ONCE -> EXACTLY_ONCE
            AT_LEAST_ONCE -> AT_LEAST_ONCE
            UNKNOWN -> UNKNOWN
        }
        AT_MOST_ONCE -> when (y) {
            ZERO -> AT_MOST_ONCE
            AT_MOST_ONCE -> UNKNOWN
            EXACTLY_ONCE -> AT_LEAST_ONCE
            AT_LEAST_ONCE -> AT_LEAST_ONCE
            UNKNOWN -> UNKNOWN
        }
        EXACTLY_ONCE -> when (y) {
            ZERO -> EXACTLY_ONCE
            else -> AT_LEAST_ONCE
        }
        AT_LEAST_ONCE -> AT_LEAST_ONCE
        UNKNOWN -> when (y) {
            EXACTLY_ONCE -> AT_LEAST_ONCE
            AT_LEAST_ONCE -> AT_LEAST_ONCE
            else -> UNKNOWN
        }
    }

    private fun updateWithCallKind(kind: InvocationKind, functionInvocationKind: InvocationKind) = when (functionInvocationKind) {
        AT_MOST_ONCE -> when (kind) {
            AT_MOST_ONCE -> AT_MOST_ONCE
            EXACTLY_ONCE -> AT_MOST_ONCE
            AT_LEAST_ONCE -> UNKNOWN
            ZERO -> ZERO
            UNKNOWN -> UNKNOWN
        }
        AT_LEAST_ONCE -> kind
        EXACTLY_ONCE -> kind
        else -> throw AssertionError()
    }
}