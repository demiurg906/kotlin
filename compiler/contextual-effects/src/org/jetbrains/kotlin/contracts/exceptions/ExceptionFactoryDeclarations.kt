/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.exceptions

import org.jetbrains.kotlin.contracts.facts.ContextCheckerFactoryDeclaration
import org.jetbrains.kotlin.contracts.facts.ContextFactFactoryDeclaration
import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.types.KotlinType

class ExceptionFactFactoryDeclaration(private val exceptionType: KotlinType) : ContextFactFactoryDeclaration {
    override fun resolveFactory(owner: ESValue, references: List<ESValue?>, bindingContext: BindingContext) =
        ExceptionFactFactory(owner, exceptionType)

    override fun toString(): String {
        return "Catches $exceptionType"
    }
}

class ExceptionCheckerFactoryDeclaration(private val exceptionType: KotlinType) : ContextCheckerFactoryDeclaration {
    override fun resolveFactory(owner: ESValue, references: List<ESValue?>, bindingContext: BindingContext) =
        ExceptionCheckerFactory(owner, exceptionType)

    override fun toString(): String {
        return "Catches $exceptionType"
    }
}