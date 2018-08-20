/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.safebuilders

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily

object CallEffectFamily : ContextualEffectFamily() {
    override val id: String = "Call (safe builders) effects"

    override val lattice = CallEffectLattice

    override val emptyContext = CallEffectsContext()
}