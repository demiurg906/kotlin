/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectContextChecker
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext

object ExceptionEffectContextChecker : ContextualEffectContextChecker {
    override fun generateDiagnostics(context: ContextualEffectsContext): List<String> {
        if (context !is ExceptionEffectsContext) {
            throw IllegalArgumentException()
        }
        return context.exceptions.map { "Unchecked exception: $it" }.sorted()
    }
}