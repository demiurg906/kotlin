/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

import org.jetbrains.kotlin.contracts.description.InvocationKind

abstract class ContextFact {
    abstract val family: ContextFamily
    abstract val isAllowedStayInContext: Boolean
}

abstract class Context {
    abstract val facts: Collection<ContextFact>
    abstract fun addFact(fact: ContextFact): Context
}

abstract class FactsCombiner {
    abstract fun or(a: Context, b: Context): Context
    abstract fun combine(context: Context, fact: ContextFact): Context
    abstract fun updateWithInvocationKind(context: Context, invocationKind: InvocationKind): Context
}

abstract class ContextFamily {
    abstract val id: String
    abstract val combiner: FactsCombiner
    abstract val emptyContext: Context
}

// --------------------------------------------

interface ContextFactFactoryHackedInterface

interface ContextCheckerFactoryHackedInterface