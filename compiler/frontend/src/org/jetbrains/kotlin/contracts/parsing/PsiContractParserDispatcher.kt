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

package org.jetbrains.kotlin.contracts.parsing

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.contracts.description.BooleanExpression
import org.jetbrains.kotlin.contracts.description.ContractDescription
import org.jetbrains.kotlin.contracts.description.EffectDeclaration
import org.jetbrains.kotlin.contracts.description.expressions.*
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.BLOCK_PROVIDES_FACT
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.BLOCK_REQUIRES_CONTEXT
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.BLOCK_REQUIRES_NOT_CONTEXT
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.CALLS_IN_PLACE_EFFECT
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.CONDITIONAL_EFFECT
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.PROVIDES_FACT
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.RECEIVER_OF
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.REQUIRES_CONTEXT
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.REQUIRES_NOT_CONTEXT
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.RETURNS_EFFECT
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames.RETURNS_NOT_NULL_EFFECT
import org.jetbrains.kotlin.contracts.parsing.effects.*
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.callUtil.getType

class PsiContractParserDispatcher(
    private val collector: ContractParsingDiagnosticsCollector,
    private val callContext: ContractCallContext
) {
    private val conditionParser = PsiConditionParser(collector, callContext, this)
    private val constantParser = PsiConstantParser(callContext)
    private val effectsParsers: Map<Name, PsiEffectParser> = mapOf(
        RETURNS_EFFECT to PsiReturnsEffectParser(collector, callContext, this),
        RETURNS_NOT_NULL_EFFECT to PsiReturnsEffectParser(collector, callContext, this),
        CALLS_IN_PLACE_EFFECT to PsiCallsEffectParser(collector, callContext, this),
        CONDITIONAL_EFFECT to PsiConditionalEffectParser(collector, callContext, this),

        PROVIDES_FACT to PsiFactParser(collector, callContext, this),
        REQUIRES_CONTEXT to PsiFactParser(collector, callContext, this),
        REQUIRES_NOT_CONTEXT to PsiFactParser(collector, callContext, this),
        BLOCK_PROVIDES_FACT to PsiLambdaFactParser(collector, callContext, this),
        BLOCK_REQUIRES_CONTEXT to PsiLambdaFactParser(collector, callContext, this),
        BLOCK_REQUIRES_NOT_CONTEXT to PsiLambdaFactParser(collector, callContext, this)
    )

    fun parseContract(): ContractDescription? {
        // Must be non-null because of checks in 'checkContractAndRecordIfPresent', but actually is not, see EA-124365
        val resolvedCall = callContext.contractCallExpression.getResolvedCall(callContext.bindingContext) ?: return null

        val firstArgumentExpression = resolvedCall.firstArgumentAsExpressionOrNull()
        val lambda = if (firstArgumentExpression is KtLambdaExpression) {
            firstArgumentExpression
        } else {
            val reportOn = firstArgumentExpression ?: callContext.contractCallExpression
            collector.badDescription("first argument of 'contract'-call should be a lambda expression", reportOn)
            return null
        }

        val effects = lambda.bodyExpression?.statements?.mapNotNull { parseEffect(it) } ?: return null

        if (effects.isEmpty()) return null

        return ContractDescription(effects, callContext.functionDescriptor)
    }

    fun parseCondition(expression: KtExpression?): BooleanExpression? = expression?.accept(conditionParser, Unit)

    fun parseEffect(expression: KtExpression?): EffectDeclaration? {
        if (expression == null) return null
        if (!isValidEffectDeclaration(expression)) return null

        val returnType = expression.getType(callContext.bindingContext) ?: return null
        val parser = effectsParsers[returnType.constructor.declarationDescriptor?.name]
        if (parser == null) {
            collector.badDescription("unrecognized effect", expression)
            return null
        }

        return parser.tryParseEffect(expression)
    }

    private fun isValidEffectDeclaration(expression: KtExpression): Boolean {
        if (expression !is KtCallExpression && expression !is KtBinaryExpression) {
            collector.badDescription("unexpected construction in contract description", expression)
            return false
        }

        val resultingDescriptor = expression.getResolvedCall(callContext.bindingContext)?.resultingDescriptor ?: return false
        if (!resultingDescriptor.isFromContractDsl()) {
            collector.badDescription("effects can be produced only by direct calls to ContractsDSL", expression)
            return false
        }

        return true
    }

    fun parseConstant(expression: KtExpression?): ConstantReference? {
        if (expression == null) return null
        return expression.accept(constantParser, Unit)
    }

    fun parseVariable(expression: KtExpression?): VariableReference? {
        if (expression == null) return null
        val descriptor = expression.getResolvedCall(callContext.bindingContext)?.resultingDescriptor ?: return null
        if (descriptor !is ParameterDescriptor) {
            collector.badDescription("only references to parameters are allowed in contract description", expression)
            return null
        }

        if (descriptor is ReceiverParameterDescriptor && descriptor.type.constructor.declarationDescriptor?.isFromContractDsl() == true) {
            collector.badDescription("only references to parameters are allowed. Did you miss label on <this>?", expression)
            return null
        }

        return if (KotlinBuiltIns.isBoolean(descriptor.type))
            BooleanVariableReference(descriptor)
        else
            VariableReference(descriptor)
    }

    fun parseReceiver(expression: KtExpression?): ReceiverReference? {
        if (expression == null) return null
        val resolvedCall = expression.getResolvedCall(callContext.bindingContext) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        // TODO: why isReceiverOf not working?
        if (descriptor.returnType?.toString() != RECEIVER_OF.toString()) return null

        val argument = resolvedCall.firstArgumentAsExpressionOrNull() ?: return null
        val variable = parseVariable(argument) ?: return null

        return ReceiverReference(variable)
    }

    fun parseFunction(expression: KtExpression?): FunctionReference? {
        if (expression == null) return null
        val reference = expression as? KtCallableReferenceExpression ?: return null
        val descriptor =
            callContext.bindingContext[BindingContext.REFERENCE_TARGET, reference.callableReference] as? FunctionDescriptor ?: return null
        return FunctionReference(descriptor)
    }

    fun parseValue(expression: KtExpression?): ContractDescriptionValue? {
        val variable = parseVariable(expression)
        if (variable != null) return variable

        return parseConstant(expression)
    }
}