/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue


data class FunctionReference(val functionDescriptor: FunctionDescriptor, val receiverValue: ReceiverValue)

//class FunctionReference(val functionDescriptor: FunctionDescriptor, val receiverParameterDescriptor: ReceiverParameterDescriptor) {
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as FunctionReference
//
//        if (functionDescriptor != other.functionDescriptor) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        return functionDescriptor.hashCode()
//    }
//}
