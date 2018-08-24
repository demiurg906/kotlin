/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.exceptions

import org.jetbrains.kotlin.contracts.contextual.Context
import org.jetbrains.kotlin.contracts.contextual.ContextFact

data class ExceptionContext(override val facts: Set<ExceptionFact> = setOf()) : Context() {
    override fun addFact(fact: ContextFact): Context {
        if (fact !is ExceptionFact) throw AssertionError()
        return ExceptionContext(facts + fact)
    }
}