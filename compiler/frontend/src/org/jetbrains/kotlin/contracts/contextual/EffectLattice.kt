/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

// интерфейс для решетки над множествами эффектов
interface EffectLattice {
    val family: ContextualEffectFamily

    fun and(a: ContextualEffectsHolder, b: ContextualEffectsHolder): ContextualEffectsHolder
    fun or(a: ContextualEffectsHolder, b: ContextualEffectsHolder): ContextualEffectsHolder

    // TODO: maybe switch semantics of `top` and `bot`
    // bot `or` x = x
    fun bot(): ContextualEffectsHolder

    // top `and` x = x
    fun top(): ContextualEffectsHolder
}