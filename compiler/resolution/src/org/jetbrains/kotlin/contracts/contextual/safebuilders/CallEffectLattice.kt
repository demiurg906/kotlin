/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.safebuilders

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectLattice
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext
import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.description.InvocationKind.*
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

object CallEffectLattice : ContextualEffectLattice {
    override fun and(a: ContextualEffectsContext, b: ContextualEffectsContext): ContextualEffectsContext {
        if (a !is CallEffectsContext || b !is CallEffectsContext) throw AssertionError()

        val calls = processCalls(a.calls, b.calls, ::combine)
        return CallEffectsContext(calls)
    }

    override fun or(a: ContextualEffectsContext, b: ContextualEffectsContext): ContextualEffectsContext {
        if (a !is AbstractCallEffectsContext || b !is AbstractCallEffectsContext) throw AssertionError()

        if (a is BotCallEffectsContext) return b
        if (b is BotCallEffectsContext) return a

        if (a !is CallEffectsContext || b !is CallEffectsContext) throw AssertionError()

        val calls = processCalls(a.calls, b.calls, ::or)
        return CallEffectsContext(calls)
    }

    private fun processCalls(a: CallsMap, b: CallsMap, operation: (InvocationKind, InvocationKind) -> InvocationKind): CallsMap {
        val functions = a.keys union b.keys
        return functions.map { it to operation(a[it] ?: ZERO, b[it] ?: ZERO) }.toMap()
    }

    private fun processBadCalls(a: BadCallsMap, b: BadCallsMap): BadCallsMap {
        val (intersection, differenceA, differenceB) = splitSets(a.keys, b.keys)

        val res = mutableMapOf<FunctionDescriptor, List<CallAnalysisResult>>()
        res.putAll(differenceA.map { it to a[it]!! })
        res.putAll(differenceB.map { it to b[it]!! })
        res.putAll(intersection.map { it to (a[it]!! + b[it]!!) })
        return res
    }

    private fun <T> splitSets(a: Set<T>, b: Set<T>): Triple<Set<T>, Set<T>, Set<T>> {
        val intersection = a.intersect(b)
        val differenceA = a - b
        val differenceB = b - a
        return Triple(intersection, differenceA, differenceB)
    }

    private operator fun <T> List<T>.plus(other: List<T>): List<T> {
        val res = mutableListOf<T>()
        res.addAll(this)
        res.addAll(other)
        return res
    }

    override fun bot(): ContextualEffectsContext {
        return BotCallEffectsContext
    }

    override fun top(): ContextualEffectsContext {
        return CallEffectsContext()
    }

    override fun updateContextWithInvocationKind(
        context: ContextualEffectsContext,
        functionInvocationKind: InvocationKind
    ): ContextualEffectsContext {
        if (context !is CallEffectsContext) throw AssertionError()

        return CallEffectsContext(
            context.calls.mapValues { (_, kind) -> updateWithCallKind(kind, functionInvocationKind) }
        )
    }
    
    private fun updateWithCallKind(kind: InvocationKind, functionInvocationKind: InvocationKind) = when (functionInvocationKind) {
        AT_MOST_ONCE -> when (kind) {
            AT_MOST_ONCE -> AT_MOST_ONCE
            EXACTLY_ONCE -> AT_MOST_ONCE
            AT_LEAST_ONCE -> UNKNOWN
            ZERO -> ZERO
            UNKNOWN -> UNKNOWN
        }
        AT_LEAST_ONCE -> when (kind) {
            AT_MOST_ONCE -> UNKNOWN
            EXACTLY_ONCE -> AT_LEAST_ONCE
            AT_LEAST_ONCE -> AT_LEAST_ONCE
            ZERO -> ZERO
            UNKNOWN -> UNKNOWN
        }
        EXACTLY_ONCE -> kind
        else -> throw AssertionError()
    }
    
    internal fun or(x: InvocationKind, y: InvocationKind) = when (x) {
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

    internal fun combine(x: InvocationKind, y: InvocationKind) = when (x) {
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
            AT_MOST_ONCE -> AT_LEAST_ONCE
            EXACTLY_ONCE -> AT_LEAST_ONCE
            AT_LEAST_ONCE -> AT_LEAST_ONCE
            UNKNOWN -> UNKNOWN
        }
        AT_LEAST_ONCE -> if (y == UNKNOWN) UNKNOWN else AT_LEAST_ONCE
        UNKNOWN -> UNKNOWN
    }

    internal fun check(expected: InvocationKind, actual: InvocationKind): Boolean {
        if (expected == ZERO || expected == UNKNOWN) throw AssertionError()
        if (actual == UNKNOWN) return false

        if (actual == expected) return true
        if (expected == AT_MOST_ONCE && (actual == ZERO || actual == EXACTLY_ONCE)) return true
        if (expected == AT_LEAST_ONCE && actual == EXACTLY_ONCE) return true

        return false
    }
}