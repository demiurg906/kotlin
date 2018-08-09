/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectParser
import org.jetbrains.kotlin.contracts.contextual.EffectConsumer
import org.jetbrains.kotlin.contracts.contextual.EffectSupplier
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

class ExceptionEffectParser : ContextualEffectParser {
    companion object {
        private const val SUPPLIER = "supplier"
        private const val CONSUMER = "consumer"
        private const val EXCEPTION = "Exception"
    }

    override fun parseDeclarationForSupplier(declaration: FunctionDescriptor): List<EffectSupplier> {
        val name = declaration.name.asString()
        if (name.startsWith(SUPPLIER)) {
            val exceptions = name.split("_").drop(1).toMutableList()
            if (exceptions.isEmpty()) {
                exceptions += EXCEPTION
            }
            return exceptions.map(::ExceptionEffectSupplier)
        }
        return listOf()
    }

    override fun parseDeclarationForConsumer(declaration: FunctionDescriptor): List<EffectConsumer> {
        val name = declaration.name.asString()
        if (name.startsWith(CONSUMER)) {
            val exceptions = name.split("_").drop(1).toMutableList()
            if (exceptions.isEmpty()) {
                exceptions += EXCEPTION
            }
            return exceptions.map(::ExceptionEffectConsumer)
        }
        return listOf()
    }
}