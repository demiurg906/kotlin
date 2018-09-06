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

class TransactionVerifier(val requiredTransaction: ValueDescriptor, val sourceElement: KtElement) :
    ContextVerifier {
    override val family = TransactionFamily

    override fun verify(contexts: Collection<Context>, diagnosticSink: DiagnosticSink) {
        val openedTransactions = contexts.mapNotNull { it as? TransactionContext }
            .map { it.openedTransactions }
            .firstOrNull() ?: setOf()
        if (requiredTransaction !in openedTransactions) {
            val message = "${requiredTransaction.name} is not opened"
            diagnosticSink.report(Errors.CONTEXTUAL_EFFECT_WARNING.on(sourceElement, message))
        }
    }
}