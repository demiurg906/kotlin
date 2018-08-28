/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.extractReceiverValue
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextDeclaration
import org.jetbrains.kotlin.contracts.facts.ContextVerifier
import org.jetbrains.kotlin.contracts.facts.VerifierDeclaration
import org.jetbrains.kotlin.contracts.model.ESFunction
import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue

class CallDeclaration : ContextDeclaration {
    override fun bind(sourceElement: KtElement, references: List<ESValue?>, bindingContext: BindingContext): Context? {
        val (functionDescriptor, receiverValue) = extractFunctionAndReceiver(references) ?: return null
        return CallContext(FunctionReference(functionDescriptor, receiverValue), sourceElement)
    }

    override fun toString(): String = "func called EXACTLY_ONCE"
}

class CallVerifierDeclaration(private val kind: InvocationKind) : VerifierDeclaration {
    override fun bind(sourceElement: KtElement, references: List<ESValue?>, bindingContext: BindingContext): ContextVerifier? {
        val (functionDescriptor, receiverValue) = extractFunctionAndReceiver(references) ?: return null
        return CallVerifier(FunctionReference(functionDescriptor, receiverValue), kind, sourceElement)
    }

    override fun toString(): String = "func needs to be called $kind"
}

fun extractFunctionAndReceiver(references: List<ESValue?>): Pair<FunctionDescriptor, ReceiverValue>? {
    val functionDescriptor = (references[0] as? ESFunction)?.descriptor ?: return null
    val receiverValue = references[1]?.extractReceiverValue() ?: return null
    return functionDescriptor to receiverValue
}