/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsHolder
import org.jetbrains.kotlin.types.KotlinType

class ExceptionEffectsHolder(val exceptions: Set<KotlinType> = setOf()) : ContextualEffectsHolder {
    override val family = ContextualEffectFamily.EXCEPTION

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExceptionEffectsHolder

        if (exceptions != other.exceptions) return false
        if (family != other.family) return false

        return true
    }

    override fun hashCode(): Int {
        var result = exceptions.hashCode()
        result = 31 * result + family.hashCode()
        return result
    }
}