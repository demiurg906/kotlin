/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts

import org.jetbrains.kotlin.contracts.contextual.EffectConsumer
import org.jetbrains.kotlin.contracts.contextual.EffectSupplier
import org.jetbrains.kotlin.contracts.description.ConsumesContextualEffectDeclaration
import org.jetbrains.kotlin.contracts.description.ContractProviderKey
import org.jetbrains.kotlin.contracts.description.SuppliesContextualEffectDeclaration
import org.jetbrains.kotlin.contracts.parsing.contextual.ContextualEffectParser
import org.jetbrains.kotlin.contracts.parsing.contextual.impl.ExceptionEffectParser
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.BindingContext

object ContextualEffectSystem {
    fun declaredSuppliers(declaration: FunctionDescriptor): Set<EffectSupplier> {
        val contractDescription = declaration.getUserData(ContractProviderKey)?.getContractDescription() ?: return emptySet()
        return contractDescription.effects
            .filter { it is SuppliesContextualEffectDeclaration }
            .map { (it as SuppliesContextualEffectDeclaration).supplier }
            .toSet()
    }

    fun declaredConsumers(declaration: FunctionDescriptor): Set<EffectConsumer> {
        val contractDescription = declaration.getUserData(ContractProviderKey)?.getContractDescription() ?: return emptySet()
        return contractDescription.effects
            .filter { it is ConsumesContextualEffectDeclaration }
            .map { (it as ConsumesContextualEffectDeclaration).consumer }
            .toSet()
    }

    val ALL_PARSERS: List<(BindingContext) -> ContextualEffectParser> =
        listOf({ context -> ExceptionEffectParser(context) })

//    private val parsers = ContextualEffectFamily.ALL_FAMILIES.map { it.newParser() }

//    private fun dirtyMockDeclaredSuppliers(declaration: FunctionDescriptor): Set<EffectSupplier> {
//        return parsers.flatMap { it.parseDeclarationForSupplier(declaration) }.toSet()
//    }
//
//    private fun dirtyMockDeclaredConsumers(declaration: FunctionDescriptor): Set<EffectConsumer> {
//        return parsers.flatMap { it.parseDeclarationForConsumer(declaration) }.toSet()
//    }
}