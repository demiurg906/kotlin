/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.safebuilders

import org.jetbrains.kotlin.contracts.contextual.model.Context
import org.jetbrains.kotlin.contracts.contextual.model.ContextCleaner

internal class CallCleaner(val functionReference: FunctionReference) : ContextCleaner {
    override val family = CallFamily

    override fun cleanupProcessed(context: Context): Context {
        if (context !is CallContext) throw AssertionError()

        val calls = context.calls.toMutableMap()
        calls.remove(functionReference)
        return CallContext(calls)
    }
}