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
import org.jetbrains.kotlin.contracts.facts.ContextCheckerFactory
import org.jetbrains.kotlin.contracts.facts.ContextCheckerFactoryDeclaration
import org.jetbrains.kotlin.contracts.facts.ContextFactFactory
import org.jetbrains.kotlin.contracts.facts.ContextFactFactoryDeclaration
import org.jetbrains.kotlin.contracts.model.*
import org.jetbrains.kotlin.contracts.model.functors.EqualsFunctor
import org.jetbrains.kotlin.contracts.model.structure.*
import org.jetbrains.kotlin.contracts.model.visitors.InfoCollector
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
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

        loop@ for (effect in resultingContextInfo.firedEffects) {
            when (effect) {
                is ESCalls -> {
                    val lambdaExpression = (effect.callable as? ESLambda)?.lambda ?: continue@loop
                    bindingTrace.record(BindingContext.LAMBDA_INVOCATIONS, lambdaExpression, effect.kind)
                }
            }
        }

        recordContextFactories(resolvedCall, resultingContextInfo, bindingTrace)
    }

    private fun recordContextFactories(
        resolvedCall: ResolvedCall<*>,
        resultingContextInfo: MutableContextInfo,
        bindingTrace: BindingTrace
    ) {
        // collect all factories for lambdas and functions
        val lambdaFactFactories = MultiMap.create<KtLambdaExpression, ContextFactFactory>()
        val lambdaCheckerFactories = MultiMap.create<KtLambdaExpression, ContextCheckerFactory>()
        val callFactFactories = MultiMap.create<KtCallExpression, ContextFactFactory>()
        val callCheckerFactories = MultiMap.create<KtCallExpression, ContextCheckerFactory>()

        loop@ for (effect in resultingContextInfo.firedEffects) {
            when (effect) {
                is ProvidesContextFactEffect -> {
                    // hack
                    val factory = effect.factory as ContextFactFactoryDeclaration

                    // TODO: maybe report diagnostic?
                    val resolvedFactory =
                        factory.resolveFactory(effect.owner, effect.references, bindingTrace.bindingContext) ?: continue@loop
                    when (effect.owner) {
                        is ESFunction -> {
                            val callExpression = resolvedCall.call.callElement as? KtCallExpression ?: throw AssertionError()
                            callFactFactories.putValue(callExpression, resolvedFactory)
                        }
                        is ESLambda -> {
                            val lambda = (effect.owner as ESLambda).lambda
                            lambdaFactFactories.putValue(lambda, resolvedFactory)
                        }
                        else -> throw AssertionError()
                    }
                }

                is RequiresContextEffect -> {
                    // hack
                    val factory = effect.factory as ContextCheckerFactoryDeclaration
                    // TODO: maybe report diagnostic?
                    val resolvedFactory =
                        factory.resolveFactory(effect.owner, effect.references, bindingTrace.bindingContext) ?: continue@loop
                    when (effect.owner) {
                        is ESFunction -> {
                            val callExpression = resolvedCall.call.callElement as? KtCallExpression ?: throw AssertionError()
                            callCheckerFactories.putValue(callExpression, resolvedFactory)
                        }
                        is ESLambda -> {
                            val lambda = (effect.owner as ESLambda).lambda
                            lambdaCheckerFactories.putValue(lambda, resolvedFactory)
                        }
                        else -> throw AssertionError()
                    }
                }
            }
        }

        // record factories to binding context
        val allLambdas = lambdaCheckerFactories.keySet() union lambdaFactFactories.keySet()
        for (lambda in allLambdas) {
            val factFactories = lambdaFactFactories[lambda]
            val checkerFactories = lambdaCheckerFactories[lambda]
            bindingTrace.record(BindingContext.LAMBDA_CONTEXT_FACTS, lambda, FactsBindingInfo(factFactories, checkerFactories))
        }

        val allCalls = callCheckerFactories.keySet() union callFactFactories.keySet()
        for (call in allCalls) {
            val factFactories = callFactFactories[call]
            val checkerFactories = callCheckerFactories[call]
            bindingTrace.record(BindingContext.CALL_CONTEXT_FACTS, call, FactsBindingInfo(factFactories, checkerFactories))
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