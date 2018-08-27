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
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.psi.KtElement

class CallFactFactory(
    val functionDescriptor: FunctionDescriptor,
    val receiverParameterDescriptor: ReceiverParameterDescriptor,
    owner: ESValue
) : ContextFactFactory(owner) {
    override fun createFact(calledElement: KtElement): ContextFact {
        return CallFact(
            FunctionReference(
                functionDescriptor,
                receiverParameterDescriptor
            ), calledElement
        )
    }
}

class CallCheckerFactory(
    val functionDescriptor: FunctionDescriptor,
    val receiverParameterDescriptor: ReceiverParameterDescriptor,
    val expectedKind: InvocationKind,
    owner: ESValue
) : ContextCheckerFactory(owner) {
    override fun createChecker(calledElement: KtElement): ContextChecker {
        return CallChecker(FunctionReference(functionDescriptor, receiverParameterDescriptor), expectedKind, calledElement)
    }
}