/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.exceptions

import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextCombiner

object ExceptionContextCombiner : ContextCombiner() {
    override fun or(a: Context, b: Context): Context {
        if (a !is ExceptionContext || b !is ExceptionContext) throw AssertionError()
        return ExceptionContext(a.cachedExceptions + b.cachedExceptions)
    }

    override fun combine(existedContext: Context, newContext: Context): Context {
        if (existedContext !is ExceptionContext || newContext !is ExceptionContext) throw AssertionError()
        return ExceptionContext(existedContext.cachedExceptions + newContext.cachedExceptions)
    }

    override fun cleanupContextAtBlockExit(context: Context, depth: Int): Context {
//        if (context !is ExceptionContext) throw AssertionError()
//        val depths = MultiMap(context.depths)
//        val exceptions = depths.keySet().toList()
//        for (exception in exceptions) {
//            depths.remove(exception, depth)
//        }
//        return ExceptionContext(depths)
        TODO()
    }
}