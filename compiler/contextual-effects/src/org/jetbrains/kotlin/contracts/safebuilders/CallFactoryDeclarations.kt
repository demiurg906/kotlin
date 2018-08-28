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
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.BindingContext

class CallDeclaration : ContextDeclaration {
    override fun bind(sourceElement: KtElement, references: List<ESValue?>, bindingContext: BindingContext): Context? {
        val function = references[0] as? ESFunction ?: return null
        val functionDescriptor = function.descriptor

        val receiver = references[1] ?: return null
        val receiverValue = receiver.extractReceiverValue() ?: return null

        return CallContext(FunctionReference(functionDescriptor, receiverValue), sourceElement)
    }

    override fun toString(): String = "func called EXACTLY_ONCE"
}

class CallVerifierDeclaration(private val kind: InvocationKind) : VerifierDeclaration {
    override fun bind(sourceElement: KtElement, references: List<ESValue?>, bindingContext: BindingContext): ContextVerifier? {
        val function = references[0] as? ESFunction ?: return null
        val functionDescriptor = function.descriptor

        val receiver = references[1] ?: return null
        val receiverValue = receiver.extractReceiverValue() ?: return null

        return CallVerifier(FunctionReference(functionDescriptor, receiverValue), kind, sourceElement)
    }

    override fun toString(): String = "func needs to be called $kind"
}
