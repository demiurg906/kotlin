/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.safebuilders

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectLattice
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext
import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

object CallEffectLattice : ContextualEffectLattice {
    override fun and(a: ContextualEffectsContext, b: ContextualEffectsContext): ContextualEffectsContext {
        if (a !is CallEffectsContext || b !is CallEffectsContext) throw AssertionError()

        // TODO: remove after debug
        debugAssert(a.calls.values)
        debugAssert(b.calls.values)

        val calls = processAndCalls(a.calls, b.calls)
        val badCalls = processBadCalls(a.badCalls, b.badCalls)
        return CallEffectsContext(calls, badCalls)
    }

    override fun or(a: ContextualEffectsContext, b: ContextualEffectsContext): ContextualEffectsContext {
        if (a !is AbstractCallEffectsContext || b !is AbstractCallEffectsContext) throw AssertionError()

        if (a is BotCallEffectsContext) return b
        if (b is BotCallEffectsContext) return a

        if (a !is CallEffectsContext || b !is CallEffectsContext) throw AssertionError()

        // TODO: remove after debug
        debugAssert(a.calls.values)
        debugAssert(b.calls.values)

        val calls = processOrCalls(a.calls, b.calls)
        val badCalls = processBadCalls(a.badCalls, b.badCalls)
        return CallEffectsContext(calls, badCalls)
    }

    private fun debugAssert(set: Collection<CallKind>) {
        if (CallKind.ZERO in set || CallKind.AT_MOST_ONCE in set) {
            throw AssertionError()
        }
    }

    private fun processAndCalls(a: CallsMap, b: CallsMap): CallsMap {
        val (intersection, differenceA, differenceB) = splitSets(a.keys, b.keys)

        val res = mutableMapOf<FunctionDescriptor, CallKind>()
        res.putAll(differenceA.map { it to a[it]!! })
        res.putAll(differenceB.map { it to b[it]!! })
        res.putAll(intersection.map { it to (a[it]!! + b[it]!!) })
        return res
    }

    private fun processOrCalls(a: CallsMap, b: CallsMap): CallsMap {
        val intersection = a.keys intersect b.keys
        return intersection.map { it to min(a[it]!!, b[it]!!) }.toMap()
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
    
    private fun <T : Comparable<T>> min(a: T, b: T) = if (a < b) a else b

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
        invocationKind: InvocationKind
    ): ContextualEffectsContext {
        if (context !is CallEffectsContext) throw AssertionError()

        return when (invocationKind) {
            InvocationKind.AT_MOST_ONCE -> CallEffectsContext(badCalls = context.badCalls)
            InvocationKind.EXACTLY_ONCE -> context
            InvocationKind.AT_LEAST_ONCE -> CallEffectsContext(
                context.calls.mapValues { (_, _) -> CallKind.AT_LEAST_ONCE },
                context.badCalls
            )
            InvocationKind.UNKNOWN -> TODO()
        }
    }
}