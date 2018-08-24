/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.old

import org.jetbrains.kotlin.contracts.description.InvocationKind

// интерфейс для решетки над множествами эффектов
interface ContextualEffectLattice {
//    val family: ContextualEffectFamily

    fun and(a: ContextualEffectsContext, b: ContextualEffectsContext): ContextualEffectsContext
    fun or(a: ContextualEffectsContext, b: ContextualEffectsContext): ContextualEffectsContext

    // bot `or` x = x
    fun bot(): ContextualEffectsContext

    // top `and` x = x
    fun top(): ContextualEffectsContext

    // ad hoc solution for current implementation of CFA of `call in place` lambdas
    fun updateContextWithInvocationKind(context: ContextualEffectsContext, functionInvocationKind: InvocationKind): ContextualEffectsContext
}