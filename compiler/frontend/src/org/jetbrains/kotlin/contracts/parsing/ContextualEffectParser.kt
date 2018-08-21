/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectSupplier
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext

abstract class ContextualEffectParser(val context: BindingContext) {
    abstract fun parseDeclarationForSupplier(declaration: KtExpression): ContextualEffectSupplier?
    abstract fun parseDeclarationForConsumer(declaration: KtExpression): ContextualEffectConsumer?

    protected fun extractConstructorName(descriptor: CallableDescriptor) =
        (descriptor as? ClassConstructorDescriptor)?.constructedClass?.name?.asString()
}
