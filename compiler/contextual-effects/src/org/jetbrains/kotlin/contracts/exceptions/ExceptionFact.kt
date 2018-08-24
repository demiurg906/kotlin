/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.exceptions

import org.jetbrains.kotlin.contracts.facts.ContextFact
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.types.KotlinType

data class ExceptionFact(val exceptionType: KotlinType, val owner: KtElement) : ContextFact() {
    override val family = ExceptionFamily
    override val isAllowedStayInContext = true
}