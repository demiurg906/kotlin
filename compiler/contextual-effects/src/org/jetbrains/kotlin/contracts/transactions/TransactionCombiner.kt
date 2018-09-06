/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.transactions

import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextCombiner
import org.jetbrains.kotlin.contracts.facts.ContextProvider

object TransactionCombiner : ContextCombiner {
    override fun or(a: Context, b: Context): Context {
        if (a !is TransactionContext || b !is TransactionContext) throw AssertionError()

        val openedTransactions = a.openedTransactions intersect b.openedTransactions
        return TransactionContext(openedTransactions)
    }

    override fun combine(context: Context, provider: ContextProvider): Context {
        if (context !is TransactionContext || provider !is TransactionProvider) throw AssertionError()

        val openedTransactions = context.openedTransactions + provider.openedTransaction
        return TransactionContext(openedTransactions)
    }
}