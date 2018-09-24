/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.dslmarker

import org.jetbrains.kotlin.contracts.description.expressions.ContractDescriptionValue
import org.jetbrains.kotlin.contracts.extractReceiverValue
import org.jetbrains.kotlin.contracts.facts.ContextProvider
import org.jetbrains.kotlin.contracts.facts.ContextVerifier
import org.jetbrains.kotlin.contracts.facts.ProviderDeclaration
import org.jetbrains.kotlin.contracts.facts.VerifierDeclaration
import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.BindingContext

internal class DslMarkerProviderDeclaration(override val references: List<ContractDescriptionValue>) : ProviderDeclaration {
    override val family = DslMarkerFamily

    override fun bind(sourceElement: KtElement, references: List<ESValue?>, bindingContext: BindingContext): ContextProvider? {
        val receiver = extractReceiverValue(references) ?: return null
        return DslMarkerProvider(receiver)
    }

    override fun toString(): String = "DSLMarker"
}

internal class DslMarkerVerifierDeclaration(override val references: List<ContractDescriptionValue>) : VerifierDeclaration {
    override val family = DslMarkerFamily

    override fun bind(sourceElement: KtElement, references: List<ESValue?>, bindingContext: BindingContext): ContextVerifier? {
        val receiver = extractReceiverValue(references) ?: return null
        return DslMarkerVerifier(receiver, sourceElement)
    }

    override fun toString(): String = "DSLMarker"
}

private fun extractReceiverValue(references: List<ESValue?>) = references.first()?.extractReceiverValue()
