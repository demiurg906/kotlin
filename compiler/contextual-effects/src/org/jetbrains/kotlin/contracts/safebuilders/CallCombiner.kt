/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.description.InvocationKind.*
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextCombiner

object CallCombiner : ContextCombiner() {
    override fun or(a: Context, b: Context): Context = performOperation(a, b, ::or)

    override fun combine(existedContext: Context, newContext: Context, depth: Int?): Context {
        if (depth != null) throw AssertionError()
        return performOperation(existedContext, newContext, ::combine)
    }

    private fun performOperation(a: Context, b: Context, operation: (InvocationKind, InvocationKind) -> InvocationKind): Context {
        if (a !is CallContext || b !is CallContext) throw AssertionError()

        val functions = a.calls.keys union b.calls.keys
        val updatedCalls = functions.mapNotNull { functionReference ->
            val aInfo = a.calls[functionReference]
            val bInfo = b.calls[functionReference]

            val aKind = aInfo?.kind ?: InvocationKind.ZERO
            val bKind = bInfo?.kind ?: InvocationKind.ZERO
            val resKind = operation(aKind, bKind)

            if (resKind == ZERO) {
                return@mapNotNull null
            }

            val sourceElement = aInfo?.sourceElement ?: bInfo?.sourceElement ?: throw AssertionError()

            functionReference to CallInfo(sourceElement, resKind)
        }.toMap()
        return CallContext(updatedCalls)
    }

    override fun cleanupContextAtBlockExit(context: Context, depth: Int): Context = context

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
}