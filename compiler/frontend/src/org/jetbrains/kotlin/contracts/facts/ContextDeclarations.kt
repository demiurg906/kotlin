/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.facts

import org.jetbrains.kotlin.contracts.description.expressions.ContractDescriptionValue
import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.BindingContext

interface ContextEntityDeclaration

interface ContextDeclaration : ContextDeclarationHackedInterface, ContextEntityDeclaration {
    val references: List<ContractDescriptionValue>

    fun bind(sourceElement: KtElement, references: List<ESValue?>, bindingContext: BindingContext): Context?
}

interface VerifierDeclaration : VerifierDeclarationHackedInterface, ContextEntityDeclaration {
    val references: List<ContractDescriptionValue>

    fun bind(sourceElement: KtElement, references: List<ESValue?>, bindingContext: BindingContext): ContextVerifier?
}

interface CleanerDeclaration : CleanerDeclarationHackedInterface, ContextEntityDeclaration {
    val references: List<ContractDescriptionValue>

    fun bind(sourceElement: KtElement, references: List<ESValue?>, bindingContext: BindingContext): ContextCleaner?
}