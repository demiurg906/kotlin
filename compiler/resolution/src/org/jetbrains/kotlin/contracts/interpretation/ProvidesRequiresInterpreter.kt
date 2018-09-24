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
import org.jetbrains.kotlin.contracts.model.ContextCleanerEffect
import org.jetbrains.kotlin.contracts.model.ContextProviderEffect
import org.jetbrains.kotlin.contracts.model.ContextVerifierEffect
import org.jetbrains.kotlin.contracts.model.ESEffect

internal class ProvidesRequiresInterpreter(private val dispatcher: ContractInterpretationDispatcher) : EffectDeclarationInterpreter {
    override fun tryInterpret(effectDeclaration: EffectDeclaration): ESEffect? = when (effectDeclaration) {
        is ContextProviderEffectDeclaration -> interpretContextProviderEffect(effectDeclaration)
        is LambdaContextProviderEffectDeclaration -> interpretLambdaContextProviderEffect(effectDeclaration)
        is ContextVerifierEffectDeclaration -> interpretContextVerifierEffect(effectDeclaration)
        is LambdaContextVerifierEffectDeclaration -> interpretLambdaContextVerifierEffect(effectDeclaration)
        is ContextCleanerEffectDeclaration -> interpretContextCleaner(effectDeclaration)
        is LambdaContextCleanerEffectDeclaration -> interpretLambdaContextCleaner(effectDeclaration)
        else -> null
    }

    private fun interpretContextProviderEffect(effectDeclaration: ContextProviderEffectDeclaration): ESEffect? {
        val (factory, references, owner) = effectDeclaration

        val interpretedReferences = interpretReferences(references)
        val interpretedFunction = dispatcher.interpretFunction(owner) ?: return null

        return ContextProviderEffect(factory, interpretedReferences, interpretedFunction)
    }

    private fun interpretLambdaContextProviderEffect(effectDeclaration: LambdaContextProviderEffectDeclaration): ESEffect? {
        val (factory, references, owner) = effectDeclaration

        val interpretedReferences = interpretReferences(references)
        val interpretedFunction = dispatcher.interpretVariable(owner) ?: return null

        return ContextProviderEffect(factory, interpretedReferences, interpretedFunction)
    }

    private fun interpretContextVerifierEffect(effectDeclaration: ContextVerifierEffectDeclaration): ESEffect? {
        val (factory, references, owner) = effectDeclaration

        val interpretedReferences = interpretReferences(references)
        val interpretedFunction = dispatcher.interpretFunction(owner) ?: return null

        return ContextVerifierEffect(factory, interpretedReferences, interpretedFunction)
    }

    private fun interpretLambdaContextVerifierEffect(effectDeclaration: LambdaContextVerifierEffectDeclaration): ESEffect? {
        val (factory, references, owner) = effectDeclaration

        val interpretedReferences = interpretReferences(references)
        val interpretedFunction = dispatcher.interpretVariable(owner) ?: return null

        return ContextVerifierEffect(factory, interpretedReferences, interpretedFunction)
    }

    private fun interpretContextCleaner(effectDeclaration: ContextCleanerEffectDeclaration): ESEffect? {
        val (factory, references, owner) = effectDeclaration

        val interpretedReferences = interpretReferences(references)
        val interpretedFunction = dispatcher.interpretFunction(owner) ?: return null

        return ContextCleanerEffect(factory, interpretedReferences, interpretedFunction)
    }

    private fun interpretLambdaContextCleaner(effectDeclaration: LambdaContextCleanerEffectDeclaration): ESEffect? {
        val (factory, references, owner) = effectDeclaration

        val interpretedReferences = interpretReferences(references)
        val interpretedFunction = dispatcher.interpretVariable(owner) ?: return null

        return ContextCleanerEffect(factory, interpretedReferences, interpretedFunction)
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