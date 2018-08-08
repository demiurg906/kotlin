/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

// checks that holder left after all consumers are done is good
// TODO: that class should be refactored
interface ContextualEffectHolderChecker {
    fun checkContext(context: ContextualEffectsHolder): Boolean

    // to refactor
    // single place where from other ContextualEffect* classes take family
    // should be refactored after prototyping CEsystem will be completed
    val family: ContextualEffectFamily
}