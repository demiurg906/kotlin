/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.ESLambda
import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.facts.ContextCheckerFactory
import org.jetbrains.kotlin.contracts.facts.ContextCheckerFactoryDeclaration
import org.jetbrains.kotlin.contracts.facts.ContextFactFactory
import org.jetbrains.kotlin.contracts.facts.ContextFactFactoryDeclaration
import org.jetbrains.kotlin.contracts.model.ESFunction
import org.jetbrains.kotlin.contracts.model.ESReceiver
import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.contracts.model.structure.ESVariable
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.resolve.BindingContext

class CallFactFactoryDeclaration : ContextFactFactoryDeclaration() {
    override fun resolveFactory(owner: ESValue, references: List<ESValue?>, bindingContext: BindingContext): ContextFactFactory? {
        val functionDescriptor = (references[0] as? ESFunction)?.descriptor ?: return null
        val receiverParameterDescriptor = (references[1] as? ESVariable)?.descriptor as? ReceiverParameterDescriptor
            ?: return null

        return CallFactFactory(functionDescriptor, receiverParameterDescriptor, owner)
    }

    override fun toString(): String = "func called EXACTLY_ONCE"
}

class CallCheckerFactoryDeclaration(val kind: InvocationKind) : ContextCheckerFactoryDeclaration() {
    override fun resolveFactory(owner: ESValue, references: List<ESValue?>, bindingContext: BindingContext): ContextCheckerFactory? {
        val function = references[0] as? ESFunction ?: return null

        // TODO: do something with receiver
        val receiver = references[1] as? ESReceiver ?: return null

        val literal = bindingContext[BindingContext.FUNCTION, (owner as ESLambda).lambda.functionLiteral] ?: return null
        val receiverParameter = literal.extensionReceiverParameter ?: return null

        val functionDescriptor = function.descriptor

        return CallCheckerFactory(functionDescriptor, receiverParameter, kind, owner)
    }

    override fun toString(): String = "func needs to be called $kind"
}
