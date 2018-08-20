/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

// some abstract collection (not Collection interface) that holds effects that belongs to one family
// !!! ContextualEffectsContext is immutable data structure !!!
interface ContextualEffectsContext {
    val family: ContextualEffectFamily

    fun unhandledEffects(): List<String>
}