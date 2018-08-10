/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsHolder
import org.jetbrains.kotlin.contracts.contextual.EffectSupplier
import org.jetbrains.kotlin.types.KotlinType

class ExceptionEffectSupplier(private val exceptionType: KotlinType) : EffectSupplier {
    override val family = ContextualEffectFamily.EXCEPTION

    override fun supply(context: ContextualEffectsHolder): ContextualEffectsHolder {
        if (context !is ExceptionEffectsHolder) {
            throw AssertionError()
        }
        return if (exceptionType in context.exceptions) {
            context
        } else {
            ExceptionEffectsHolder(context.exceptions + exceptionType)
        }
    }

    override fun toString(): String {
        return "Supplier of $exceptionType"
    }
}