/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.safebuilders

import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.facts.ContextFact
import org.jetbrains.kotlin.psi.KtElement

data class CallFact(
    val functionReference: FunctionReference,
    val calledElement: KtElement,
    val kind: InvocationKind = InvocationKind.EXACTLY_ONCE
) : ContextFact() {
    override val family = CallFamily
    override val isAllowedStayInContext = false

    override fun toString(): String = "${functionReference.functionDescriptor.name} was invoken $kind"
}