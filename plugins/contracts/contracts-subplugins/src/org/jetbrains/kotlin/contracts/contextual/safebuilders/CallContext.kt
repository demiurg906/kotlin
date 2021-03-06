/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.safebuilders

import org.jetbrains.kotlin.contracts.contextual.cfg.ContextContracts
import org.jetbrains.kotlin.contracts.contextual.model.Context
import org.jetbrains.kotlin.contracts.contextual.model.ContextProvider
import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue

internal data class FunctionReference(val functionDescriptor: FunctionDescriptor, val receiverValue: ReceiverValue) {
    override fun toString(): String = functionDescriptor.name.toString()
}

internal data class CallInfo(val sourceElement: KtElement, val kind: InvocationKind) {
    override fun toString(): String = kind.toString()
}

internal data class CallContext(val calls: Map<FunctionReference, CallInfo> = mapOf()) : Context {
    override val family = CallFamily

    override fun reportRemaining(sink: DiagnosticSink, declaredContracts: ContextContracts) {
        for ((functionReference, info) in calls) {
            val (sourceElement, kind) = info
            val message = "${functionReference.functionDescriptor.name} had invoked $kind"
            sink.report(Errors.CONTEXTUAL_EFFECT_WARNING.on(sourceElement, message))
        }
    }
}

internal data class CallContextProvider(val functionReference: FunctionReference, val sourceElement: KtElement) : ContextProvider {
    override val family = CallFamily
}