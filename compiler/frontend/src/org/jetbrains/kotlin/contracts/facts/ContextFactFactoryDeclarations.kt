/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.facts

import org.jetbrains.kotlin.contracts.model.ESValue

interface ContextEntityFactory

abstract class ContextFactFactoryDeclaration : ContextFactFactoryDeclarationInterface, ContextEntityFactory {
    abstract fun resolveFactory(owner: ESValue, references: List<ESValue?>): ContextFactFactory
}

abstract class ContextCheckerFactoryDeclaration : ContextCheckerFactoryDeclarationInterface, ContextEntityFactory {
    abstract fun resolveFactory(owner: ESValue, references: List<ESValue?>): ContextCheckerFactory
}