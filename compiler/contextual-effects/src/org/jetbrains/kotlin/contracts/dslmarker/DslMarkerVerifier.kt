/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.dslmarker

import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextVerifier
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue

class DslMarkerVerifier(val receiver: ReceiverValue, val sourceElement: KtElement) :
    ContextVerifier {
    override val family = DslMarkerFamily

    override fun verify(noLevelContext: Context, blockContexts: List<Context>, diagnosticSink: DiagnosticSink) {
        val lastContext = blockContexts.lastOrNull()
        if (lastContext == null) {
            val message = "No opened context"
            diagnosticSink.report(Errors.CONTEXTUAL_EFFECT_WARNING.on(sourceElement, message))
            return
        }

        if (lastContext !is DslMarkerContext) throw AssertionError()

        if (receiver !in lastContext.receivers) {
            val message = "Call function in wrong scope"
            diagnosticSink.report(Errors.CONTEXTUAL_EFFECT_WARNING.on(sourceElement, message))
        }
    }
}