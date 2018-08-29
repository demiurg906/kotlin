/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.exceptions

import org.jetbrains.kotlin.contracts.description.expressions.ContractDescriptionValue
import org.jetbrains.kotlin.contracts.facts.ContextDeclaration
import org.jetbrains.kotlin.contracts.facts.VerifierDeclaration
import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.types.KotlinType

class ExceptionContextDeclaration(private val exceptionType: KotlinType) : ContextDeclaration {
    override val references: List<ContractDescriptionValue> = emptyList()

    override fun bind(sourceElement: KtElement, references: List<ESValue?>, bindingContext: BindingContext): ExceptionContext =
        ExceptionContext(exceptionType)

    override fun toString(): String {
        return "Catches $exceptionType"
    }
}

class ExceptionVerifierDeclaration(private val exceptionType: KotlinType) : VerifierDeclaration {
    override val references: List<ContractDescriptionValue> = emptyList()

    override fun bind(sourceElement: KtElement, references: List<ESValue?>, bindingContext: BindingContext): ExceptionVerifier =
        ExceptionVerifier(exceptionType, sourceElement)

    override fun toString(): String {
        return "Catches $exceptionType"
    }
}