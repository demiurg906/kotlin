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
    override fun parseDeclarationForSupplier(declaration: FunctionDescriptor): EffectSupplier? {
        if (declaration.name.asString() == "bar") {
            return ExceptionEffectSupplier("aaa")
        }
        return null
    }

    override fun parseDeclarationForConsumer(declaration: FunctionDescriptor): EffectConsumer? {
        if (declaration.name.asString() == "foo") {
            return ExceptionEffectConsumer("aaa")
        }
        return null
    }
}