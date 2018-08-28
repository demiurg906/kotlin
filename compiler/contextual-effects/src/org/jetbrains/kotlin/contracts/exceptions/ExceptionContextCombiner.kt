/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.exceptions

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextCombiner

object ExceptionContextCombiner : ContextCombiner() {
    override fun or(a: Context, b: Context): Context = union(a, b)

    override fun combine(a: Context, b: Context): Context = union(a, b)

    private fun union(a: Context, b: Context): Context {
        if (a !is ExceptionContext || b !is ExceptionContext) throw AssertionError()
        return ExceptionContext(a.cachedExceptions + b.cachedExceptions)
    }

    override fun updateWithInvocationKind(context: Context, invocationKind: InvocationKind): Context {
        if (context !is ExceptionContext) throw AssertionError()
        return context
    }
}