/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.facts

import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.psi.KtElement

// TODO: source element
// rename to
abstract class ContextFactFactory(val owner: ESValue) {
    abstract fun createFact(calledElement: KtElement): ContextFact
}

abstract class ContextCheckerFactory(val owner: ESValue) {
    abstract fun createChecker(calledElement: KtElement): ContextChecker
}