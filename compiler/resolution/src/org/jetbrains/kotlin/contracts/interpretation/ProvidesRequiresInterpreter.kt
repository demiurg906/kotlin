/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.interpretation

import org.jetbrains.kotlin.contracts.description.*
import org.jetbrains.kotlin.contracts.description.expressions.ContractDescriptionValue
import org.jetbrains.kotlin.contracts.description.expressions.FunctionReference
import org.jetbrains.kotlin.contracts.description.expressions.ReceiverReference
import org.jetbrains.kotlin.contracts.description.expressions.VariableReference
import org.jetbrains.kotlin.contracts.model.ESEffect
import org.jetbrains.kotlin.contracts.model.ProvidesContextEffect
import org.jetbrains.kotlin.contracts.model.RequiresContextEffect

internal class ProvidesRequiresInterpreter(private val dispatcher: ContractInterpretationDispatcher) : EffectDeclarationInterpreter {
    override fun tryInterpret(effectDeclaration: EffectDeclaration): ESEffect? = when (effectDeclaration) {
        is ProvidesFactEffectDeclaration -> interpretProvidesEffect(effectDeclaration)
        is LambdaProvidesFactEffectDeclaration -> interpretLambdaProvidesEffect(effectDeclaration)
        is RequiresContextEffectDeclaration -> interpretRequiresContext(effectDeclaration)
        is LambdaRequiresContextEffectDeclaration -> interpretLambdaRequiresContext(effectDeclaration)
        else -> null
    }

    private fun interpretProvidesEffect(effectDeclaration: ProvidesFactEffectDeclaration): ESEffect? {
        val (factory, references, owner) = effectDeclaration

        val interpretedReferences = interpretReferences(references)
        val interpretedFunction = dispatcher.interpretFunction(owner) ?: return null

        return ProvidesContextEffect(factory, interpretedReferences, interpretedFunction)
    }

    private fun interpretLambdaProvidesEffect(effectDeclaration: LambdaProvidesFactEffectDeclaration): ESEffect? {
        val (factory, references, owner) = effectDeclaration

        val interpretedReferences = interpretReferences(references)
        val interpretedFunction = dispatcher.interpretVariable(owner) ?: return null

        return ProvidesContextEffect(factory, interpretedReferences, interpretedFunction)
    }

    private fun interpretRequiresContext(effectDeclaration: RequiresContextEffectDeclaration): ESEffect? {
        val (factory, references, owner) = effectDeclaration

        val interpretedReferences = interpretReferences(references)
        val interpretedFunction = dispatcher.interpretFunction(owner) ?: return null

        return RequiresContextEffect(factory, interpretedReferences, interpretedFunction)
    }

    private fun interpretLambdaRequiresContext(effectDeclaration: LambdaRequiresContextEffectDeclaration): ESEffect? {
        val (factory, references, owner) = effectDeclaration

        val interpretedReferences = interpretReferences(references)
        val interpretedFunction = dispatcher.interpretVariable(owner) ?: return null

        return RequiresContextEffect(factory, interpretedReferences, interpretedFunction)
    }

    private fun interpretReferences(references: List<ContractDescriptionValue>) = references.map {
        when (it) {
            is VariableReference -> dispatcher.interpretVariable(it)
            is FunctionReference -> dispatcher.interpretFunction(it)
            is ReceiverReference -> dispatcher.interpretReceiverReference(it)
            else -> throw AssertionError("Illegal type of ContractDescriptionValue type: ${it::class}")
        }
    }
}