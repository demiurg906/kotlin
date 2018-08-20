/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

import org.jetbrains.kotlin.contracts.contextual.exceptions.ExceptionEffectFamily
import org.jetbrains.kotlin.contracts.contextual.safebuilders.CallEffectFamily

abstract class ContextualEffectFamily {
    abstract val id: String
    abstract val lattice: ContextualEffectLattice
    abstract val emptyContext: ContextualEffectsContext

    companion object {
        val ALL_FAMILIES: List<ContextualEffectFamily> by lazy { listOf(ExceptionEffectFamily, CallEffectFamily) }
    }
}