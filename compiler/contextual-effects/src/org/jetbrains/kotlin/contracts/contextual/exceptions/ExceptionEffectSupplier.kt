/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.exceptions

import org.jetbrains.kotlin.contracts.contextual.old.ContextualEffectSupplier
import org.jetbrains.kotlin.contracts.contextual.old.ContextualEffectsContext
import org.jetbrains.kotlin.types.KotlinType

class ExceptionEffectSupplier(private val exceptionType: KotlinType) : ContextualEffectSupplier() {
    override val family = ExceptionEffectFamily

    override fun supply(context: ContextualEffectsContext): ContextualEffectsContext {
        if (context !is ExceptionEffectsContext) {
            throw AssertionError()
        }
        return if (exceptionType in context.exceptions) {
            context
        } else {
            ExceptionEffectsContext(context.exceptions + exceptionType)
        }
    }

    override fun toString(): String {
        return "Supplier of $exceptionType"
    }
}