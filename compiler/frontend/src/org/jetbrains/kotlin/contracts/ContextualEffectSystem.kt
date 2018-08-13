/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectSupplier
import org.jetbrains.kotlin.contracts.description.ContractDescription
import org.jetbrains.kotlin.contracts.description.ContractProviderKey
import org.jetbrains.kotlin.contracts.parsing.contextual.ContextualEffectParser
import org.jetbrains.kotlin.contracts.parsing.contextual.impl.ExceptionEffectParser
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.BindingContext

object ContextualEffectSystem {
    /**
     * list of all registered [ContextualEffectParser]s in compiler
     */
    val ALL_PARSERS: List<(BindingContext) -> ContextualEffectParser> = listOf(::ExceptionEffectParser)

    fun declaredSuppliers(declaration: FunctionDescriptor): List<ContextualEffectSupplier> {
        val contractDescription = declaration.contractDescription ?: return emptyList()
        return contractDescription.effects.mapNotNull { it as? ContextualEffectSupplier }
    }

    fun declaredConsumers(declaration: FunctionDescriptor): List<ContextualEffectConsumer> {
        val contractDescription = declaration.contractDescription ?: return emptyList()
        return contractDescription.effects.mapNotNull { it as? ContextualEffectConsumer }
    }

    private val FunctionDescriptor.contractDescription: ContractDescription?
        get() = getUserData(ContractProviderKey)?.getContractDescription()
}