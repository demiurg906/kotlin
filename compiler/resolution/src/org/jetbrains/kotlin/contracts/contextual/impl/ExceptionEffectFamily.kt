/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext

class ExceptionEffectFamily : ContextualEffectFamily() {
    override val id: String = "Checked exception effects"

    override val lattice = ExceptionEffectLattice

    override val contextChecker = ExceptionEffectContextChecker

    override val emptyContext: ContextualEffectsContext
        get() = ExceptionEffectsContext()

    // TODO: remove equals after object bug
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExceptionEffectFamily

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}