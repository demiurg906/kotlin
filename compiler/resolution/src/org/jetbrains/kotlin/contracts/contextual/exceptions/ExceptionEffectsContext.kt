/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.exceptions

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext
import org.jetbrains.kotlin.types.KotlinType

data class ExceptionEffectsContext(val exceptions: Set<KotlinType> = setOf()) : ContextualEffectsContext {
    override val family = ExceptionEffectFamily
    // TODO: check equality with javaClass
}