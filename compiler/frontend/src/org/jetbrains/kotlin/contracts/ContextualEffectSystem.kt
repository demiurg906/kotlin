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
import org.jetbrains.kotlin.contracts.model.ESFunction
import org.jetbrains.kotlin.contracts.parsing.*
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.KtElement
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

    fun declaredFacts(subroutine: KtElement, declaration: FunctionDescriptor, context: BindingContext): Collection<ContextFact> {
        val (factFactories, _) = context[BindingContext.FUNCTION_CONTEXT_FACTS, declaration] ?: return emptyList()
        return factFactories.filter { (it.owner as ESFunction).descriptor != declaration }.map { it.createFact(subroutine) }
    }

    fun declaredFacts(declaration: KtLambdaExpression, context: BindingContext): Collection<ContextFact> {
        val (factFactories, _) = context[BindingContext.LAMBDA_CONTEXT_FACTS, declaration] ?: return emptyList()
        return factFactories.map { it.createFact(declaration) }
    }

    fun declaredCheckers(subroutine: KtElement, declaration: FunctionDescriptor, context: BindingContext): Collection<ContextChecker> {
        val (_, checkerFactories) = context[BindingContext.FUNCTION_CONTEXT_FACTS, declaration] ?: return emptyList()
//        return checkerFactories.filter { (it.owner as ESFunction).descriptor != declaration }.map { it.createChecker(subroutine) }
        return checkerFactories.map { it.createChecker(subroutine) }
    }

    fun declaredCheckers(declaration: KtLambdaExpression, context: BindingContext): Collection<ContextChecker> {
        val (_, checkerFactories) = context[BindingContext.LAMBDA_CONTEXT_FACTS, declaration] ?: return emptyList()
        return checkerFactories.map { it.createChecker(declaration) }
    }
}

data class FactsBindingInfo(
    val factFactories: Collection<ContextFactFactory> = listOf(),
    val checkerFactories: Collection<ContextCheckerFactory> = listOf()
)