/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.facts.Context

data class CallContext(val calls: Map<FunctionReference, CallFact> = mapOf()) : Context() {
    override val facts: Collection<CallFact>
        get() = calls.values
}