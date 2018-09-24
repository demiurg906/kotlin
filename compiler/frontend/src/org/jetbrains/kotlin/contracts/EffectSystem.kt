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
import org.jetbrains.kotlin.contracts.facts.*
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

        val contextsGroupedByExpression = MultiMap.create<KtExpression, ContextProvider>()
        val verifiersGroupedByExpression = MultiMap.create<KtExpression, ContextVerifier>()
        val cleanersGroupedByExpression = MultiMap.create<KtExpression, ContextCleaner>()

        loop@ for (effect in resultingContextInfo.firedEffects) {
            when (effect) {
                is ESCalls -> {
                    val lambdaExpression = (effect.callable as? ESLambda)?.lambda ?: continue@loop
                    bindingTrace.record(BindingContext.LAMBDA_INVOCATIONS, lambdaExpression, effect.kind)
                }

                is ContextProviderEffect -> {
                    val (context, expression) = extractProvider(effect, resolvedCall, bindingTrace.bindingContext)
                    contextsGroupedByExpression.putValue(expression, context)
                }

                is ContextVerifierEffect -> {
                    val (verifier, expression) = extractVerifier(effect, resolvedCall, bindingTrace.bindingContext)
                    verifiersGroupedByExpression.putValue(expression, verifier)
                }

                is ContextCleanerEffect -> {
                    val (cleaner, expression) = extractCleaner(effect, resolvedCall, bindingTrace.bindingContext)
                    cleanersGroupedByExpression.putValue(expression, cleaner)
                }
            }
        }

        // record contexts and verifiers to binding context
        val expressions = verifiersGroupedByExpression.keySet() union contextsGroupedByExpression.keySet() union cleanersGroupedByExpression.keySet()
        for (expression in expressions) {
            val providers = contextsGroupedByExpression[expression]
            val verifiers = verifiersGroupedByExpression[expression]
            val cleaners = cleanersGroupedByExpression[expression]
            bindingTrace.record(BindingContext.CONTEXT_FACTS, expression, FactsBindingInfo(providers, verifiers, cleaners))
        }
    }

    private data class ProviderWithExpression(val provider: ContextProvider, val expression: KtExpression)
    private data class VerifierWithExpression(val verifier: ContextVerifier, val expression: KtExpression)
    private data class CleanerWithExpression(val cleaner: ContextCleaner, val expression: KtExpression)

    private fun extractProvider(
        effect: ContextProviderEffect,
        resolvedCall: ResolvedCall<*>,
        bindingContext: BindingContext
    ): ProviderWithExpression {
        // hack
        val providerDeclaration = effect.providerDeclaration as ProviderDeclaration
        return when (effect.owner) {
            is ESFunction -> {
                val callExpression = resolvedCall.call.callElement as? KtCallExpression ?: throw AssertionError()
                val provider = providerDeclaration.bind(callExpression, effect.references, bindingContext) ?: throw AssertionError()
                ProviderWithExpression(provider, callExpression)
            }
            is ESLambda -> {
                val lambda = (effect.owner as ESLambda).lambda.functionLiteral
                val provider = providerDeclaration.bind(lambda, effect.references, bindingContext) ?: throw AssertionError()
                ProviderWithExpression(provider, lambda)
            }
            else -> throw AssertionError()
        }
    }

    private fun extractVerifier(
        effect: ContextVerifierEffect,
        resolvedCall: ResolvedCall<*>,
        bindingContext: BindingContext
    ): VerifierWithExpression {
        // hack
        val verifierDeclaration = effect.verifierDeclaration as VerifierDeclaration
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

    private fun extractCleaner(
        effect: ContextCleanerEffect,
        resolvedCall: ResolvedCall<*>,
        bindingContext: BindingContext
    ): CleanerWithExpression {
        // hack
        val cleanerDeclaration = effect.cleanerDeclaration as CleanerDeclaration
        return when (effect.owner) {
            is ESFunction -> {
                val callExpression = resolvedCall.call.callElement as? KtCallExpression ?: throw AssertionError()
                val cleaner = cleanerDeclaration.bind(callExpression, effect.references, bindingContext) ?: throw AssertionError()
                CleanerWithExpression(cleaner, callExpression)
            }
            is ESLambda -> {
                val lambda = (effect.owner as ESLambda).lambda.functionLiteral
                val cleaner = cleanerDeclaration.bind(lambda, effect.references, bindingContext) ?: throw AssertionError()
                CleanerWithExpression(cleaner, lambda)
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