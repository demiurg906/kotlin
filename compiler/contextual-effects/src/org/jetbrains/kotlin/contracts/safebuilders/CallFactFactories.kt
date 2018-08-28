/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.facts.ContextChecker
import org.jetbrains.kotlin.contracts.facts.ContextCheckerFactory
import org.jetbrains.kotlin.contracts.facts.ContextFact
import org.jetbrains.kotlin.contracts.facts.ContextFactFactory
import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue

class CallFactFactory(
    val functionDescriptor: FunctionDescriptor,
    val receiverValue: ReceiverValue,
    owner: ESValue
) : ContextFactFactory(owner) {
    override fun createFact(calledElement: KtElement): ContextFact =
        CallFact(FunctionReference(functionDescriptor, receiverValue), calledElement)
}

class CallCheckerFactory(
    val functionDescriptor: FunctionDescriptor,
    val receiverParameterDescriptor: ReceiverValue,
    val expectedKind: InvocationKind,
    owner: ESValue
) : ContextCheckerFactory(owner) {
    override fun createChecker(calledElement: KtElement): ContextChecker {
        return CallChecker(FunctionReference(functionDescriptor, receiverParameterDescriptor), expectedKind, calledElement)
    }
}