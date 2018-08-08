/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.impl

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectHolderChecker
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsHolder
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors.CONTEXTUAL_EFFECT_WARNING

class ExceptionEffectHolderChecker : ContextualEffectHolderChecker {
    override val family = ContextualEffectFamily.EXCEPTION

    override fun generateDiagnostics(element: PsiElement, context: ContextualEffectsHolder): List<Diagnostic> {
        if (context !is ExceptionEffectsHolder) {
            throw IllegalArgumentException()
        }
        return context.effects.map { CONTEXTUAL_EFFECT_WARNING.on(element, "unchecked exception ${it.exception}") }
    }

    override fun checkContext(context: ContextualEffectsHolder) {
//        if (context !is ExceptionEffectsHolder) {
//            throw AssertionError()
//        }
//        return context.effects.isEmpty()
    }
}