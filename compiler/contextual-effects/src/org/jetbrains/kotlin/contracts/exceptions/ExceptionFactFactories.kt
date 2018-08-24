/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.exceptions

import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.contracts.parsing.ContextCheckerFactory
import org.jetbrains.kotlin.contracts.parsing.ContextFactFactory
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.types.KotlinType

class ExceptionFactFactory(owner: ESValue, private val exceptionType: KotlinType) : ContextFactFactory(owner) {
    override fun createFact(calledElement: KtElement) =
        ExceptionFact(exceptionType, calledElement)

}

class ExceptionCheckerFactory(owner: ESValue, private val exceptionType: KotlinType) : ContextCheckerFactory(owner) {
    override fun createChecker(calledElement: KtElement) =
        ExceptionChecker(exceptionType, calledElement)
}

