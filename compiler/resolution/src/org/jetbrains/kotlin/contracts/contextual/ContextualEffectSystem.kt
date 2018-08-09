/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

import org.jetbrains.kotlin.descriptors.FunctionDescriptor

object ContextualEffectSystem {
    fun declaredSuppliers(declaration: FunctionDescriptor): Set<EffectSupplier> =
        dirtyMockDeclaredSuppliers(declaration)
    fun declaredConsumers(declaration: FunctionDescriptor): Set<EffectConsumer> =
        dirtyMockDeclaredConsumers(declaration)

    private val parsers = ContextualEffectFamily.ALL_FAMILIES.map { it.newParser() }

    private fun dirtyMockDeclaredSuppliers(declaration: FunctionDescriptor): Set<EffectSupplier> {
        return parsers.flatMap { it.parseDeclarationForSupplier(declaration) }.toSet()
    }

    private fun dirtyMockDeclaredConsumers(declaration: FunctionDescriptor): Set<EffectConsumer> {
        return parsers.flatMap { it.parseDeclarationForConsumer(declaration) }.toSet()
    }
}