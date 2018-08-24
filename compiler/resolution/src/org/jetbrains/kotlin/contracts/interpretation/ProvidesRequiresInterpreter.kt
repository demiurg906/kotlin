/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.interpretation

import org.jetbrains.kotlin.contracts.description.*
import org.jetbrains.kotlin.contracts.model.ESEffect
import org.jetbrains.kotlin.contracts.model.ProvidesContextFactEffect
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

        val interpretedVariables = references.map { dispatcher.interpretVariable(it) }
        val interpretedFunction = dispatcher.interpretFunction(owner) ?: return null

        return ProvidesContextFactEffect(factory, interpretedVariables, interpretedFunction)
    }

    private fun interpretLambdaProvidesEffect(effectDeclaration: LambdaProvidesFactEffectDeclaration): ESEffect? {
        val (factory, references, owner) = effectDeclaration

        val interpretedVariables = references.map { dispatcher.interpretVariable(it) }
        val interpretedFunction = dispatcher.interpretVariable(owner) ?: return null

        return ProvidesContextFactEffect(factory, interpretedVariables, interpretedFunction)
    }

    private fun interpretRequiresContext(effectDeclaration: RequiresContextEffectDeclaration): ESEffect? {
        val (factory, references, owner) = effectDeclaration

        val interpretedVariables = references.map { dispatcher.interpretVariable(it) }
        val interpretedFunction = dispatcher.interpretFunction(owner) ?: return null

        return RequiresContextEffect(factory, interpretedVariables, interpretedFunction)
    }

    private fun interpretLambdaRequiresContext(effectDeclaration: LambdaRequiresContextEffectDeclaration): ESEffect? {
        val (factory, references, owner) = effectDeclaration

        val interpretedVariables = references.map { dispatcher.interpretVariable(it) }
        val interpretedFunction = dispatcher.interpretVariable(owner) ?: return null

        return RequiresContextEffect(factory, interpretedVariables, interpretedFunction)
    }
}