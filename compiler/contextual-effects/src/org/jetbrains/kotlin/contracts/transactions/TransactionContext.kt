/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.transactions

import org.jetbrains.kotlin.cfg.ContextContracts
import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.descriptors.ValueDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticSink

internal data class TransactionContext(val openedTransactions: Map<ValueDescriptor, InvocationKind> = mapOf()) : Context {
    override val family = TransactionFamily

    override fun reportRemaining(sink: DiagnosticSink, declaredContracts: ContextContracts) {
        // TODO: what we do with uncommitted transactions?
    }
}