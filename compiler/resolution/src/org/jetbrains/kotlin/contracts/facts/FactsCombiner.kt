/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.facts

import org.jetbrains.kotlin.contracts.description.InvocationKind

abstract class FactsCombiner {
    abstract fun or(a: Context, b: Context): Context
    abstract fun combine(context: Context, fact: ContextFact): Context
    abstract fun updateWithInvocationKind(context: Context, invocationKind: InvocationKind): Context
}