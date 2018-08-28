/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.facts.ContextCheckerFactory
import org.jetbrains.kotlin.contracts.facts.ContextCheckerFactoryDeclaration
import org.jetbrains.kotlin.contracts.facts.ContextFactFactory
import org.jetbrains.kotlin.contracts.facts.ContextFactFactoryDeclaration
import org.jetbrains.kotlin.contracts.model.ESFunction
import org.jetbrains.kotlin.contracts.model.ESReceiver
import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.resolve.BindingContext

class CallFactFactoryDeclaration : ContextFactFactoryDeclaration {
    // TODO: rename to bind
    override fun resolveFactory(owner: ESValue, references: List<ESValue?>, bindingContext: BindingContext): ContextFactFactory? {
        val functionDescriptor = (references[0] as? ESFunction)?.descriptor ?: return null
        val receiver = references[1] as? ESReceiver ?: return null
        val receiverValue = receiver.receiver
        return CallFactFactory(functionDescriptor, receiverValue, owner)
    }

    override fun toString(): String = "func called EXACTLY_ONCE"
}

class CallCheckerFactoryDeclaration(val kind: InvocationKind) : ContextCheckerFactoryDeclaration {
    override fun resolveFactory(owner: ESValue, references: List<ESValue?>, bindingContext: BindingContext): ContextCheckerFactory? {
        val function = references[0] as? ESFunction ?: return null
        val functionDescriptor = function.descriptor

        val receiver = references[1] as? ESReceiver ?: return null
        val receiverParameter = receiver.receiver

        return CallCheckerFactory(functionDescriptor, receiverParameter, kind, owner)
    }

    override fun toString(): String = "func needs to be called $kind"
}
