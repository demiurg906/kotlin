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

package org.jetbrains.kotlin.contracts

import com.intellij.util.containers.MultiMap
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextDeclaration
import org.jetbrains.kotlin.contracts.facts.ContextVerifier
import org.jetbrains.kotlin.contracts.facts.VerifierDeclaration
import org.jetbrains.kotlin.contracts.model.*
import org.jetbrains.kotlin.contracts.model.functors.EqualsFunctor
import org.jetbrains.kotlin.contracts.model.structure.*
import org.jetbrains.kotlin.contracts.model.visitors.InfoCollector
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.smartcasts.ConditionalDataFlowInfo
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory

class EffectSystem(val languageVersionSettings: LanguageVersionSettings, val dataFlowValueFactory: DataFlowValueFactory) {

    fun getDataFlowInfoForFinishedCall(
        resolvedCall: ResolvedCall<*>,
        bindingTrace: BindingTrace,
        moduleDescriptor: ModuleDescriptor
    ): DataFlowInfo {
        if (!languageVersionSettings.supportsFeature(LanguageFeature.UseReturnsEffect)) return DataFlowInfo.EMPTY

        // Prevent launch of effect system machinery on pointless cases (constants/enums/constructors/etc.)
        val callExpression = resolvedCall.call.callElement as? KtCallExpression ?: return DataFlowInfo.EMPTY
        if (callExpression is KtDeclaration) return DataFlowInfo.EMPTY

        val resultContextInfo = getContextInfoWhen(ESReturns(ESConstant.WILDCARD), callExpression, bindingTrace, moduleDescriptor)

        return resultContextInfo.toDataFlowInfo(languageVersionSettings)
    }

    fun getDataFlowInfoWhenEquals(
        leftExpression: KtExpression?,
        rightExpression: KtExpression?,
        bindingTrace: BindingTrace,
        moduleDescriptor: ModuleDescriptor
    ): ConditionalDataFlowInfo {
        if (!languageVersionSettings.supportsFeature(LanguageFeature.UseReturnsEffect)) return ConditionalDataFlowInfo.EMPTY
        if (leftExpression == null || rightExpression == null) return ConditionalDataFlowInfo.EMPTY

        val leftComputation =
            getNonTrivialComputation(leftExpression, bindingTrace, moduleDescriptor) ?: return ConditionalDataFlowInfo.EMPTY
        val rightComputation =
            getNonTrivialComputation(rightExpression, bindingTrace, moduleDescriptor) ?: return ConditionalDataFlowInfo.EMPTY

        val effects = EqualsFunctor(false).invokeWithArguments(leftComputation, rightComputation)

        val equalsContextInfo = InfoCollector(ESReturns(true.lift())).collectFromSchema(effects)
        val notEqualsContextInfo = InfoCollector(ESReturns(false.lift())).collectFromSchema(effects)

        return ConditionalDataFlowInfo(
            equalsContextInfo.toDataFlowInfo(languageVersionSettings),
            notEqualsContextInfo.toDataFlowInfo(languageVersionSettings)
        )
    }

    fun recordDefiniteInvocations(resolvedCall: ResolvedCall<*>, bindingTrace: BindingTrace, moduleDescriptor: ModuleDescriptor) {
        if (!languageVersionSettings.supportsFeature(LanguageFeature.UseCallsInPlaceEffect)) return

        // Prevent launch of effect system machinery on pointless cases (constants/enums/constructors/etc.)
        val callExpression = resolvedCall.call.callElement as? KtCallExpression ?: return
        if (callExpression is KtDeclaration) return

        val resultingContextInfo = getContextInfoWhen(ESReturns(ESConstant.WILDCARD), callExpression, bindingTrace, moduleDescriptor)

        val contexts = MultiMap.create<KtExpression, Context>()
        val verifiers = MultiMap.create<KtExpression, ContextVerifier>()

        loop@ for (effect in resultingContextInfo.firedEffects) {
            when (effect) {
                is ESCalls -> {
                    val lambdaExpression = (effect.callable as? ESLambda)?.lambda ?: continue@loop
                    bindingTrace.record(BindingContext.LAMBDA_INVOCATIONS, lambdaExpression, effect.kind)
                }

                is ProvidesContextEffect -> {
                    val (context, expression) = extractContext(effect, resolvedCall, bindingTrace.bindingContext)
                    contexts.putValue(expression, context)
                }

                is RequiresContextEffect -> {
                    val (verifier, expression) = extractVerifier(effect, resolvedCall, bindingTrace.bindingContext)
                    verifiers.putValue(expression, verifier)
                }
            }
        }

        // record contexts and verifiers to binding context
        val expressions = verifiers.keySet() union contexts.keySet()
        for (expression in expressions) {
            val factFactories = contexts[expression]
            val checkerFactories = verifiers[expression]
            bindingTrace.record(BindingContext.CONTEXT_FACTS, expression, FactsBindingInfo(factFactories, checkerFactories))
        }
    }

    private data class VerifierWithExpression(val verifier: ContextVerifier, val expression: KtExpression)
    private data class ContextWithExpression(val context: Context, val expression: KtExpression)

    private fun extractContext(
        effect: ProvidesContextEffect,
        resolvedCall: ResolvedCall<*>,
        bindingContext: BindingContext
    ): ContextWithExpression {
        // hack
        val contextDeclaration = effect.contextDeclaration as ContextDeclaration
        return when (effect.owner) {
            is ESFunction -> {
                val callExpression = resolvedCall.call.callElement as? KtCallExpression ?: throw AssertionError()
                val context = contextDeclaration.bind(callExpression, effect.references, bindingContext) ?: throw AssertionError()
                ContextWithExpression(context, callExpression)
            }
            is ESLambda -> {
                val lambda = (effect.owner as ESLambda).lambda.functionLiteral
                val context = contextDeclaration.bind(lambda, effect.references, bindingContext) ?: throw AssertionError()
                ContextWithExpression(context, lambda)
            }
            else -> throw AssertionError()
        }
    }

    private fun extractVerifier(
        effect: RequiresContextEffect,
        resolvedCall: ResolvedCall<*>,
        bindingContext: BindingContext
    ): VerifierWithExpression {
        // hack
        val verifierDeclaration = effect.verifiersDeclaration as VerifierDeclaration
        return when (effect.owner) {
            is ESFunction -> {
                val callExpression = resolvedCall.call.callElement as? KtCallExpression ?: throw AssertionError()
                val verifier = verifierDeclaration.bind(callExpression, effect.references, bindingContext) ?: throw AssertionError()
                VerifierWithExpression(verifier, callExpression)
            }
            is ESLambda -> {
                val lambda = (effect.owner as ESLambda).lambda.functionLiteral
                val verifier = verifierDeclaration.bind(lambda, effect.references, bindingContext) ?: throw AssertionError()
                VerifierWithExpression(verifier, lambda)
            }
            else -> throw AssertionError()
        }
    }

    fun extractDataFlowInfoFromCondition(
        condition: KtExpression?,
        value: Boolean,
        bindingTrace: BindingTrace,
        moduleDescriptor: ModuleDescriptor
    ): DataFlowInfo {
        if (!languageVersionSettings.supportsFeature(LanguageFeature.UseReturnsEffect)) return DataFlowInfo.EMPTY
        if (condition == null) return DataFlowInfo.EMPTY

        return getContextInfoWhen(ESReturns(value.lift()), condition, bindingTrace, moduleDescriptor)
            .toDataFlowInfo(languageVersionSettings)
    }

    private fun getContextInfoWhen(
        observedEffect: ESEffect,
        expression: KtExpression,
        bindingTrace: BindingTrace,
        moduleDescriptor: ModuleDescriptor
    ): MutableContextInfo {
        val computation = getNonTrivialComputation(expression, bindingTrace, moduleDescriptor) ?: return MutableContextInfo.EMPTY
        return InfoCollector(observedEffect).collectFromSchema(computation.effects)
    }

    private fun getNonTrivialComputation(expression: KtExpression, trace: BindingTrace, moduleDescriptor: ModuleDescriptor): Computation? {
        val computation = EffectsExtractingVisitor(trace, moduleDescriptor, dataFlowValueFactory).extractOrGetCached(expression)
        return if (computation == UNKNOWN_COMPUTATION) null else computation
    }
}