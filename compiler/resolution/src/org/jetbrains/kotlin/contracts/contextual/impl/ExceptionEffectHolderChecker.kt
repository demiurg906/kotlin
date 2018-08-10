/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectHolderChecker
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsHolder

class ExceptionEffectHolderChecker : ContextualEffectHolderChecker {
    override val family = ContextualEffectFamily.EXCEPTION

    override fun generateDiagnostics(context: ContextualEffectsHolder): List<String> {
        if (context !is ExceptionEffectsHolder) {
            throw IllegalArgumentException()
        }
        return context.exceptions.map { "Unchecked exception: $it" }
    }
}