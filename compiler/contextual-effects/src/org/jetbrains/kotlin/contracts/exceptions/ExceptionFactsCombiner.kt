/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.exceptions

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextFact
import org.jetbrains.kotlin.contracts.facts.FactsCombiner

object ExceptionFactsCombiner : FactsCombiner() {
    override fun or(a: Context, b: Context): Context {
        if (a !is ExceptionContext || b !is ExceptionContext) throw AssertionError()
        return ExceptionContext(a.facts + b.facts)
    }

    override fun combine(context: Context, fact: ContextFact): Context {
        if (context !is ExceptionContext || fact !is ExceptionFact) throw AssertionError()
        return context.addFact(fact)
    }

    override fun updateWithInvocationKind(context: Context, invocationKind: InvocationKind): Context {
        if (context !is ExceptionContext) throw AssertionError()
        return context
    }
}