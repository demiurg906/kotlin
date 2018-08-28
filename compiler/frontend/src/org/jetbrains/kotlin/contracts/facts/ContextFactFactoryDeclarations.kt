/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.facts

import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.resolve.BindingContext

interface ContextEntityFactory

interface ContextFactFactoryDeclaration : ContextFactFactoryDeclarationInterface, ContextEntityFactory {
    fun resolveFactory(owner: ESValue, references: List<ESValue?>, bindingContext: BindingContext): ContextFactFactory?
}

interface ContextCheckerFactoryDeclaration : ContextCheckerFactoryDeclarationInterface, ContextEntityFactory {
    fun resolveFactory(owner: ESValue, references: List<ESValue?>, bindingContext: BindingContext): ContextCheckerFactory?
}