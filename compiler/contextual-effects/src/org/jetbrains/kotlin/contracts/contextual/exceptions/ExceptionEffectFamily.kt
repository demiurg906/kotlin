/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.exceptions

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily

object ExceptionEffectFamily : ContextualEffectFamily() {
    override val id: String = "Checked exception effects"

    override val lattice = ExceptionEffectLattice

    override val emptyContext = ExceptionEffectsContext()
}