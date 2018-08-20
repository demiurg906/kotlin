/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.safebuilders

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectContextChecker
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext

object CallEffectContextChecker : ContextualEffectContextChecker {
    override fun generateDiagnostics(context: ContextualEffectsContext): List<String> {
        if (context !is CallEffectsContext) throw AssertionError()

        return context.badCalls.flatMap { (function, results) ->
            results.map { result ->
                "${function.name} call mismatch: expected ${result.expected}, actual ${result.actual}"
            }.sorted()
        }
    }
}