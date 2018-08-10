/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectSupplier
import org.jetbrains.kotlin.contracts.description.ConsumesContextualEffectDeclaration
import org.jetbrains.kotlin.contracts.description.ContractProviderKey
import org.jetbrains.kotlin.contracts.description.SuppliesContextualEffectDeclaration
import org.jetbrains.kotlin.contracts.parsing.contextual.ContextualEffectParser
import org.jetbrains.kotlin.contracts.parsing.contextual.impl.ExceptionEffectParser
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.BindingContext

object ContextualEffectSystem {
    // list of all registered ContextualEffectParsers in compiler
    val ALL_PARSERS: List<(BindingContext) -> ContextualEffectParser> = listOf(::ExceptionEffectParser)

    fun declaredSuppliers(declaration: FunctionDescriptor): Set<ContextualEffectSupplier> {
        val contractDescription = extractContractDescription(declaration) ?: return emptySet()
        return contractDescription.effects
            .filter { it is SuppliesContextualEffectDeclaration }
            .map { (it as SuppliesContextualEffectDeclaration).supplier }
            .toSet()
    }

    fun declaredConsumers(declaration: FunctionDescriptor): Set<ContextualEffectConsumer> {
        val contractDescription = extractContractDescription(declaration) ?: return emptySet()
        return contractDescription.effects
            .filter { it is ConsumesContextualEffectDeclaration }
            .map { (it as ConsumesContextualEffectDeclaration).consumer }
            .toSet()
    }

    private fun extractContractDescription(declaration: FunctionDescriptor) =
        declaration.getUserData(ContractProviderKey)?.getContractDescription()
}