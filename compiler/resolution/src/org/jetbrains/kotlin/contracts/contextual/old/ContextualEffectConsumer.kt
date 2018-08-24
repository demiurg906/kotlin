/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.old

import org.jetbrains.kotlin.contracts.description.ContractDescriptionVisitor
import org.jetbrains.kotlin.contracts.description.EffectDeclaration

/**
 * Effect which specifies, that subroutine consumes contextual effect
 */
abstract class ContextualEffectConsumer : EffectDeclaration {
    abstract val family: ContextualEffectFamily

    abstract fun consume(context: ContextualEffectsContext): Pair<ContextualEffectsContext, String?>

    override fun <R, D> accept(contractDescriptionVisitor: ContractDescriptionVisitor<R, D>, data: D): R =
        contractDescriptionVisitor.visitConsumesContextualEffectDeclaration(this, data)
}