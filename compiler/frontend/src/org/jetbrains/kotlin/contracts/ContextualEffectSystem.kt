/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts

import org.jetbrains.kotlin.contracts.contextual.ContextFact
import org.jetbrains.kotlin.contracts.contextual.ContextFamily
import org.jetbrains.kotlin.contracts.contextual.old.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.old.ContextualEffectFamily
import org.jetbrains.kotlin.contracts.contextual.old.ContextualEffectSupplier
import org.jetbrains.kotlin.contracts.description.ContractDescription
import org.jetbrains.kotlin.contracts.description.ContractProviderKey
import org.jetbrains.kotlin.contracts.parsing.*
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.resolve.BindingContext

@Deprecated("")
object ContextualEffectSystem {
    private val contextualEffectFamiliesProvider: ContextualEffectFamiliesProvider = ContextualEffectFamiliesService

    fun getFamilies(): Collection<ContextualEffectFamily> = contextualEffectFamiliesProvider.getFamilies()
    fun getParsers(): Collection<(BindingContext) -> ContextualEffectParser> = contextualEffectFamiliesProvider.getParsers()

    fun declaredSuppliers(declaration: FunctionDescriptor): List<ContextualEffectSupplier> {
        val contractDescription = declaration.contractDescription ?: return emptyList()
        return contractDescription.effects.mapNotNull { it as? ContextualEffectSupplier }
    }

    fun declaredConsumers(declaration: FunctionDescriptor): List<ContextualEffectConsumer> {
        val contractDescription = declaration.contractDescription ?: return emptyList()
        return contractDescription.effects.mapNotNull { it as? ContextualEffectConsumer }
    }

    private val FunctionDescriptor.contractDescription: ContractDescription?
        get() = getUserData(ContractProviderKey)?.getContractDescription()
}

@Deprecated("")
data class ContextualBindingInfo(
    val suppliers: List<ContextualEffectSupplier> = listOf(),
    val consumers: List<ContextualEffectConsumer> = listOf()
)

// ---------------------------------------------------------------------------------------------------------------------

object FactsEffectSystem {
    private val contextEffectsFamiliesProvider: ContextFamiliesProvider = ContextFamiliesService

    fun getFamilies(): Collection<ContextFamily> = contextEffectsFamiliesProvider.getFamilies()
    fun getParsers(): Collection<(BindingContext, PsiContractParserDispatcher) -> ContextFactParser> =
        contextEffectsFamiliesProvider.getParsers()

    // calls
    fun declaredFactsAndCheckers(
        callExpression: KtCallExpression,
        context: BindingContext
    ): Pair<Collection<ContextFact>, Collection<ContextChecker>> {
        val (factFactories, checkerFactories) = context[BindingContext.CALL_CONTEXT_FACTS, callExpression] ?: return emptyList() to emptyList()
        return factFactories.map { it.createFact(callExpression) } to checkerFactories.map { it.createChecker(callExpression) }
    }

    // lambdas
    fun declaredFacts(lambdaExpression: KtLambdaExpression, context: BindingContext): Collection<ContextFact> {
        val (factFactories, _) = context[BindingContext.LAMBDA_CONTEXT_FACTS, lambdaExpression] ?: return emptyList()
        return factFactories.map { it.createFact(lambdaExpression) }
    }

    fun declaredCheckers(lambdaExpression: KtLambdaExpression, context: BindingContext): Collection<ContextChecker> {
        val (_, checkerFactories) = context[BindingContext.LAMBDA_CONTEXT_FACTS, lambdaExpression] ?: return emptyList()
        return checkerFactories.map { it.createChecker(lambdaExpression) }
    }
}

data class FactsBindingInfo(
    val factFactories: Collection<ContextFactFactory> = listOf(),
    val checkerFactories: Collection<ContextCheckerFactory> = listOf()
)