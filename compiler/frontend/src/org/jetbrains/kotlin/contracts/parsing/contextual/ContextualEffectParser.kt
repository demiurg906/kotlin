/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing.contextual

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectSupplier
import org.jetbrains.kotlin.psi.KtExpression

interface ContextualEffectParser {
    fun parseDeclarationForSupplier(declaration: KtExpression): ContextualEffectSupplier?
    fun parseDeclarationForConsumer(declaration: KtExpression): ContextualEffectConsumer?
}
