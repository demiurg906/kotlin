/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.transactions

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextCombiner
import org.jetbrains.kotlin.contracts.facts.ContextProvider
import org.jetbrains.kotlin.descriptors.ValueDescriptor

internal object TransactionCombiner : ContextCombiner {
    override fun or(a: Context, b: Context): Context {
        if (a !is TransactionContext || b !is TransactionContext) throw AssertionError()

        val openedTransactions = a.openedTransactions.keys union b.openedTransactions.keys

        val result = mutableMapOf<ValueDescriptor, InvocationKind>()
        for (transaction in openedTransactions) {
            val aKind = a.openedTransactions[transaction] ?: InvocationKind.ZERO
            val bKind = b.openedTransactions[transaction] ?: InvocationKind.ZERO
            result[transaction] = InvocationKind.or(aKind, bKind)
        }
        return TransactionContext(result)
    }

    override fun combine(context: Context, provider: ContextProvider): Context {
        if (context !is TransactionContext || provider !is TransactionProvider) throw AssertionError()

        val openedTransactions = context.openedTransactions.toMutableMap()
        val currentKind = openedTransactions[provider.openedTransaction] ?: InvocationKind.ZERO
        openedTransactions[provider.openedTransaction] = InvocationKind.combine(currentKind, InvocationKind.EXACTLY_ONCE)
        return TransactionContext(openedTransactions)
    }
}