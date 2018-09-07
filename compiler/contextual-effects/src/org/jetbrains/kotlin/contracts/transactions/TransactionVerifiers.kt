/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.transactions

import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextVerifier
import org.jetbrains.kotlin.descriptors.ValueDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtElement

// checks that transaction opened
class OpenedTransactionVerifier(val requiredTransaction: ValueDescriptor, val sourceElement: KtElement) : ContextVerifier {
    override val family = TransactionFamily

    override fun verify(contexts: Collection<Context>, diagnosticSink: DiagnosticSink) {
        val openedTransactions = extractOpenedTransactions(contexts)
        if (requiredTransaction !in openedTransactions) {
            val message = "${requiredTransaction.name} is not opened"
            diagnosticSink.report(Errors.CONTEXTUAL_EFFECT_WARNING.on(sourceElement, message))
        }
    }
}

// checks that transaction isn't opened
class ClosedTransactionVerifier(val requiredTransaction: ValueDescriptor, val sourceElement: KtElement) : ContextVerifier {
    override val family = TransactionFamily

    override fun verify(contexts: Collection<Context>, diagnosticSink: DiagnosticSink) {
        val openedTransactions = extractOpenedTransactions(contexts)
        if (requiredTransaction in openedTransactions) {
            val message = "Transaction ${requiredTransaction.name} already started"
            diagnosticSink.report(Errors.CONTEXTUAL_EFFECT_WARNING.on(sourceElement, message))
        }
    }
}

private fun extractOpenedTransactions(contexts: Collection<Context>): Set<ValueDescriptor> {
    return contexts.mapNotNull { it as? TransactionContext }
        .map { it.openedTransactions }
        .firstOrNull() ?: setOf()
}