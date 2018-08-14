/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.interpretation

import org.jetbrains.kotlin.contracts.description.BlockConsumesContextualEffectDeclaration
import org.jetbrains.kotlin.contracts.description.BlockSuppliesContextualEffectDeclaration
import org.jetbrains.kotlin.contracts.description.EffectDeclaration
import org.jetbrains.kotlin.contracts.model.ConsumesEffect
import org.jetbrains.kotlin.contracts.model.ESEffect
import org.jetbrains.kotlin.contracts.model.SuppliesEffect

internal class SuppliesEffectInterpreter(private val dispatcher: ContractInterpretationDispatcher) : EffectDeclarationInterpreter {
    override fun tryInterpret(effectDeclaration: EffectDeclaration): ESEffect? {
        if (effectDeclaration !is BlockSuppliesContextualEffectDeclaration) return null

        val variable = dispatcher.interpretVariable(effectDeclaration.variableReference) ?: return null
        val supplier = effectDeclaration.supplier

        return SuppliesEffect(variable, supplier)
    }
}

internal class ConsumesEffectInterpreter(private val dispatcher: ContractInterpretationDispatcher) : EffectDeclarationInterpreter {
    override fun tryInterpret(effectDeclaration: EffectDeclaration): ESEffect? {
        if (effectDeclaration !is BlockConsumesContextualEffectDeclaration) return null

        val variable = dispatcher.interpretVariable(effectDeclaration.variableReference) ?: return null
        val consumer = effectDeclaration.consumer

        return ConsumesEffect(variable, consumer)
    }
}

