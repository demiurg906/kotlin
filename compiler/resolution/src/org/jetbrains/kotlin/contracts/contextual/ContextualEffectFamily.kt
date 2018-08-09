/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

import org.jetbrains.kotlin.contracts.contextual.impl.ExceptionEffectHolderChecker
import org.jetbrains.kotlin.contracts.contextual.impl.ExceptionEffectLattice
import org.jetbrains.kotlin.contracts.contextual.impl.ExceptionEffectParser
import org.jetbrains.kotlin.contracts.contextual.impl.ExceptionEffectsHolder

class ContextualEffectFamily(
    val id: Int,
    val lattice: EffectLattice,
    val contextChecker: () -> ContextualEffectHolderChecker,
    val emptyHolder: () -> ContextualEffectsHolder,
    // dirty, used only for prototyping
    val newParser: () -> ContextualEffectParser
) {
    companion object {
        val EXCEPTION = ContextualEffectFamily(
            1,
            ExceptionEffectLattice,
            { ExceptionEffectHolderChecker() },
            { ExceptionEffectsHolder() },
            ::ExceptionEffectParser
        )

        val ALL_FAMILIES = listOf(EXCEPTION)
    }
}