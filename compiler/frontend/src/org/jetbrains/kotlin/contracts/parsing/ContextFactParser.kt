/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing

import org.jetbrains.kotlin.contracts.description.expressions.VariableReference
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext

abstract class ContextFactParser(val context: BindingContext, val dispatcher: PsiContractParserDispatcher) {
    abstract fun parseDeclarationForFactFactory(declaration: KtExpression): Pair<ContextFactFactoryDeclaration, List<VariableReference>>?
    abstract fun parseDeclarationForCheckerFactory(declaration: KtExpression): Pair<ContextCheckerFactoryDeclaration, List<VariableReference>>?

    protected fun extractConstructorName(descriptor: CallableDescriptor) =
        (descriptor as? ClassConstructorDescriptor)?.constructedClass?.name?.asString()
}