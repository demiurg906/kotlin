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

import org.jetbrains.kotlin.contracts.description.expressions.ConstantReference
import org.jetbrains.kotlin.contracts.description.expressions.ContractDescriptionValue
import org.jetbrains.kotlin.contracts.description.expressions.FunctionReference
import org.jetbrains.kotlin.contracts.description.expressions.VariableReference
import org.jetbrains.kotlin.contracts.facts.CleanerDeclarationHackedInterface
import org.jetbrains.kotlin.contracts.facts.ProviderDeclarationHackedInterface
import org.jetbrains.kotlin.contracts.facts.VerifierDeclarationHackedInterface

/**
 * Effect with condition attached to it.
 *
 * [condition] is some expression, which result-type is Boolean, and clause should
 * be interpreted as: "if [effect] took place then [condition]-expression is
 * guaranteed to be true"
 *
 * NB. [effect] and [condition] connected with implication in math logic sense:
 * [effect] => [condition]. In particular this means that:
 *  - there can be multiple ways how [effect] can be produced, but for any of them
 *    [condition] holds.
 *  - if [effect] wasn't observed, we *can't* reason that [condition] is false
 *  - if [condition] is true, we *can't* reason that [effect] will be observed.
 */
class ConditionalEffectDeclaration(val effect: EffectDeclaration, val condition: BooleanExpression) : EffectDeclaration {
    override fun <R, D> accept(contractDescriptionVisitor: ContractDescriptionVisitor<R, D>, data: D): R =
        contractDescriptionVisitor.visitConditionalEffectDeclaration(this, data)
}


/**
 * Effect which specifies that subroutine returns some particular value
 */
class ReturnsEffectDeclaration(val value: ConstantReference) : EffectDeclaration {
    override fun <R, D> accept(contractDescriptionVisitor: ContractDescriptionVisitor<R, D>, data: D): R =
        contractDescriptionVisitor.visitReturnsEffectDeclaration(this, data)

}


/**
 * Effect which specifies, that during execution of subroutine, callable [variableReference] will be invoked
 * [kind] amount of times, and will never be invoked after subroutine call is finished.
 */
class CallsEffectDeclaration(val variableReference: VariableReference, val kind: InvocationKind) : EffectDeclaration {
    override fun <R, D> accept(contractDescriptionVisitor: ContractDescriptionVisitor<R, D>, data: D): R =
        contractDescriptionVisitor.visitCallsEffectDeclaration(this, data)
}

enum class InvocationKind {
    AT_MOST_ONCE,
    EXACTLY_ONCE,
    AT_LEAST_ONCE,
    ZERO,
    UNKNOWN
}

fun InvocationKind.isDefinitelyVisited(): Boolean = this == InvocationKind.EXACTLY_ONCE || this == InvocationKind.AT_LEAST_ONCE
fun InvocationKind.canBeRevisited(): Boolean = this == InvocationKind.UNKNOWN || this == InvocationKind.AT_LEAST_ONCE

// -----------------------------------------------------------------

data class ContextProviderEffectDeclaration(
    val factory: ProviderDeclarationHackedInterface,
    val references: List<ContractDescriptionValue>,
    val owner: FunctionReference
) : EffectDeclaration {
    override fun <R, D> accept(contractDescriptionVisitor: ContractDescriptionVisitor<R, D>, data: D): R {
        return contractDescriptionVisitor.visitContextProviderEffectDeclaration(this, data)
    }
}

data class LambdaContextProviderEffectDeclaration(
    val factory: ProviderDeclarationHackedInterface,
    val references: List<ContractDescriptionValue>,
    val owner: VariableReference
) : EffectDeclaration {
    override fun <R, D> accept(contractDescriptionVisitor: ContractDescriptionVisitor<R, D>, data: D): R {
        return contractDescriptionVisitor.visitLambdaContextProviderEffectDeclaration(this, data)
    }
}

data class ContextVerifierEffectDeclaration(
    val factory: VerifierDeclarationHackedInterface,
    val references: List<ContractDescriptionValue>,
    val owner: FunctionReference
) : EffectDeclaration {
    override fun <R, D> accept(contractDescriptionVisitor: ContractDescriptionVisitor<R, D>, data: D): R {
        return contractDescriptionVisitor.visitContextVerifierEffectDeclaration(this, data)
    }
}

data class LambdaContextVerifierEffectDeclaration(
    val factory: VerifierDeclarationHackedInterface,
    val references: List<ContractDescriptionValue>,
    val owner: VariableReference
) : EffectDeclaration {
    override fun <R, D> accept(contractDescriptionVisitor: ContractDescriptionVisitor<R, D>, data: D): R {
        return contractDescriptionVisitor.visitLambdaContextVerifierEffectDeclaration(this, data)
    }
}

data class ContextCleanerEffectDeclaration(
    val factory: CleanerDeclarationHackedInterface,
    val references: List<ContractDescriptionValue>,
    val owner: FunctionReference
) : EffectDeclaration {
    override fun <R, D> accept(contractDescriptionVisitor: ContractDescriptionVisitor<R, D>, data: D): R {
        return contractDescriptionVisitor.visitContextCleanerEffectDeclaration(this, data)
    }
}

data class LambdaContextCleanerEffectDeclaration(
    val factory: CleanerDeclarationHackedInterface,
    val references: List<ContractDescriptionValue>,
    val owner: VariableReference
) : EffectDeclaration {
    override fun <R, D> accept(contractDescriptionVisitor: ContractDescriptionVisitor<R, D>, data: D): R {
        return contractDescriptionVisitor.visitLambdaContextCleanerEffectDeclaration(this, data)
    }
}