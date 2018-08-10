/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily


class ExceptionEffect(val exception: String) {
    val family = ContextualEffectFamily.EXCEPTION

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExceptionEffect

        if (exception != other.exception) return false
        if (family != other.family) return false

        return true
    }

    override fun hashCode(): Int {
        return exception.hashCode()
    }
}