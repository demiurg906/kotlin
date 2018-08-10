/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

import org.jetbrains.kotlin.contracts.contextual.impl.ExceptionEffectContextChecker
import org.jetbrains.kotlin.contracts.contextual.impl.ExceptionEffectLattice
import org.jetbrains.kotlin.contracts.contextual.impl.ExceptionEffectsContext

class ContextualEffectFamily(
    val id: Int,
    val lattice: ContextualEffectLattice,
    val contextChecker: () -> ContextualEffectContextChecker,
    val emptyContext: () -> ContextualEffectsContext
) {
    companion object {
        val EXCEPTION = ContextualEffectFamily(
            1,
            ExceptionEffectLattice,
            { ExceptionEffectContextChecker() },
            { ExceptionEffectsContext() }
        )

        val ALL_FAMILIES = listOf(EXCEPTION)
    }
}