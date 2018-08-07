/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

class ContextualEffectFamily private constructor(
    val id: Int,
    val lattice: EffectLattice,
    val emptyHolder: () -> ContextualEffectsHolder
) {
    companion object {
//        val DUMMY_FAMILY = ContextualEffectFamily(
//            1,
//            ExceptionEffectLattice,
//            ::ExceptionEffectsHolder
//        )
//
//        val allFamilies = listOf(DUMMY_FAMILY)
    }
}