/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

interface ContextualEffectHolderChecker {
    val family: ContextualEffectFamily

    /* looks at context and generates warning messages if needed */
    fun generateDiagnostics(context: ContextualEffectsHolder): List<String>
}