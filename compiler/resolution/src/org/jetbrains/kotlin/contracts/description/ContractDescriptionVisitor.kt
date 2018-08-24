/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.contracts.description

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectSupplier
import org.jetbrains.kotlin.contracts.description.expressions.*

interface ContractDescriptionVisitor<out R, in D> {
    fun visitContractDescriptionElement(contractDescriptionElement: ContractDescriptionElement, data: D): R {
        throw IllegalStateException("Top of hierarchy reached, no overloads were found for element: $contractDescriptionElement")
    }

    // Effects
    fun visitEffectDeclaration(effectDeclaration: EffectDeclaration, data: D): R = visitContractDescriptionElement(effectDeclaration, data)

    fun visitConditionalEffectDeclaration(conditionalEffect: ConditionalEffectDeclaration, data: D): R =
        visitEffectDeclaration(conditionalEffect, data)

    fun visitReturnsEffectDeclaration(returnsEffect: ReturnsEffectDeclaration, data: D): R = visitEffectDeclaration(returnsEffect, data)
    fun visitCallsEffectDeclaration(callsEffect: CallsEffectDeclaration, data: D): R = visitEffectDeclaration(callsEffect, data)

    @Deprecated("")
    fun visitSuppliesContextualEffectDeclaration(suppliesEffect: ContextualEffectSupplier, data: D): R =
        visitEffectDeclaration(suppliesEffect, data)

    @Deprecated("")
    fun visitConsumesContextualEffectDeclaration(consumesEffect: ContextualEffectConsumer, data: D): R =
        visitEffectDeclaration(consumesEffect, data)

    @Deprecated("")
    fun visitBlockSuppliesContextualEffectDeclaration(suppliesEffect: BlockSuppliesContextualEffectDeclaration, data: D): R =
        visitEffectDeclaration(suppliesEffect, data)

    @Deprecated("")
    fun visitBlockConsumesContextualEffectDeclaration(consumesEffect: BlockConsumesContextualEffectDeclaration, data: D): R =
        visitEffectDeclaration(consumesEffect, data)

    // Expressions
    fun visitBooleanExpression(booleanExpression: BooleanExpression, data: D): R = visitContractDescriptionElement(booleanExpression, data)

    fun visitLogicalOr(logicalOr: LogicalOr, data: D): R = visitBooleanExpression(logicalOr, data)
    fun visitLogicalAnd(logicalAnd: LogicalAnd, data: D): R = visitBooleanExpression(logicalAnd, data)
    fun visitLogicalNot(logicalNot: LogicalNot, data: D): R = visitBooleanExpression(logicalNot, data)

    fun visitIsInstancePredicate(isInstancePredicate: IsInstancePredicate, data: D): R = visitBooleanExpression(isInstancePredicate, data)
    fun visitIsNullPredicate(isNullPredicate: IsNullPredicate, data: D): R = visitBooleanExpression(isNullPredicate, data)

    // Values
    fun visitValue(value: ContractDescriptionValue, data: D): R = visitContractDescriptionElement(value, data)

    fun visitConstantDescriptor(constantReference: ConstantReference, data: D): R = visitValue(constantReference, data)
    fun visitBooleanConstantDescriptor(booleanConstantDescriptor: BooleanConstantReference, data: D): R =
        visitConstantDescriptor(booleanConstantDescriptor, data)

    fun visitVariableReference(variableReference: VariableReference, data: D): R = visitValue(variableReference, data)
    fun visitBooleanVariableReference(booleanVariableReference: BooleanVariableReference, data: D): R =
        visitVariableReference(booleanVariableReference, data)

    // Facts
    fun visitProvidesFactEffectDeclaration(effectDeclaration: ProvidesFactEffectDeclaration, data: D): R =
        visitEffectDeclaration(effectDeclaration, data)

    fun visitLambdaProvidesFactEffectDeclaration(effectDeclaration: LambdaProvidesFactEffectDeclaration, data: D): R =
        visitEffectDeclaration(effectDeclaration, data)

    fun visitRequiresContextEffectDeclaration(effectDeclaration: RequiresContextEffectDeclaration, data: D): R =
        visitEffectDeclaration(effectDeclaration, data)

    fun visitLambdaRequiresContextEffectDeclaration(effectDeclaration: LambdaRequiresContextEffectDeclaration, data: D): R =
        visitEffectDeclaration(effectDeclaration, data)
}