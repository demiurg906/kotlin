/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

// интерфейс для решетки над множествами эффектов
interface EffectLattice<T : ContextualEffect> {
    fun and(a: Set<T>, b: Set<T>): Set<T>
    fun or(a: Set<T>, b: Set<T>): Set<T>
}