/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.transactions

import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextCleaner
import org.jetbrains.kotlin.descriptors.ValueDescriptor

class TransactionCleaner(val openedTransaction: ValueDescriptor) :
    ContextCleaner {
    override val family = TransactionFamily

    override fun cleanupProcessed(context: Context): Context {
        if (context !is TransactionContext) throw AssertionError()

        val openedTransaction = context.openedTransactions - openedTransaction
        return TransactionContext(openedTransaction)
    }
}