/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.exceptions

import org.jetbrains.kotlin.contracts.contextual.Context
import org.jetbrains.kotlin.contracts.parsing.ContextChecker
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isSubtypeOf

class ExceptionChecker(val exceptionType: KotlinType, val calledElement: KtElement) : ContextChecker() {
    override val family = ExceptionFamily

    override fun verifyContext(context: Context, trace: BindingTrace, shouldReport: Boolean): Context {
        if (context !is ExceptionContext) throw AssertionError()
        val isOk = context.facts.asSequence().any {
            exceptionType.isSubtypeOf(it.exceptionType) && calledElement.parents.contains(it.owner)
        }
        if (!isOk && shouldReport) {
            trace.report(Errors.CONTEXTUAL_EFFECT_WARNING.on(calledElement, "Unchecked exception: $exceptionType"))
        }
        return context
    }
}