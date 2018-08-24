/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.parsing

import org.jetbrains.kotlin.contracts.contextual.*
import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.BindingTrace

interface ContextEntityFactory

abstract class ContextChecker {
    abstract val family: ContextFamily
    abstract fun verifyContext(context: Context, trace: BindingTrace, shouldReport: Boolean): Context
}

abstract class ContextFactFactoryDeclaration : ContextFactFactoryHackedInterface, ContextEntityFactory {
    abstract fun resolveFactory(owner: ESValue, references: List<ESValue?>): ContextFactFactory
}

abstract class ContextFactFactory(val owner: ESValue) {
    abstract fun createFact(calledElement: KtElement): ContextFact

//    abstract fun createFact(descriptor: FunctionDescriptor): ContextChecker
//    abstract fun createFact(expression: KtLambdaExpression, context: BindingContext): ContextChecker
}

abstract class ContextCheckerFactoryDeclaration : ContextCheckerFactoryHackedInterface, ContextEntityFactory {
    abstract fun resolveFactory(owner: ESValue, references: List<ESValue?>): ContextCheckerFactory
}

abstract class ContextCheckerFactory(val owner: ESValue) {
    abstract fun createChecker(calledElement: KtElement): ContextChecker
}