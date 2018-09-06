/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.transactions

import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.descriptors.ValueDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticSink

class TransactionContext(val openedTransactions: Set<ValueDescriptor> = setOf()) :
    Context {
    override val family = TransactionFamily

    override fun reportRemaining(sink: DiagnosticSink) {
        // TODO: what we do with uncommitted transactions?
    }
}