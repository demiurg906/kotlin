/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlin.internal.contracts

import kotlin.internal.ContractsDsl
import kotlin.internal.InlineOnly

@ContractsDsl
@SinceKotlin("1.2")
internal interface ContractBuilder {
    @ContractsDsl
    fun returns(): Returns

    @ContractsDsl
    fun returns(value: Any?): Returns

    @ContractsDsl
    fun returnsNotNull(): ReturnsNotNull

    @ContractsDsl
    fun <R> callsInPlace(lambda: Function<R>, kind: InvocationKind = InvocationKind.UNKNOWN): CallsInPlace

    // -----------------------------------------------------

    @ContractsDsl
    fun supplies(effect: ContextualEffectSuppliesDescription): Supplies

    @ContractsDsl
    fun <R> supplies(block: Function<R>, effect: ContextualEffectSuppliesDescription): ProvideSupplies

    @ContractsDsl
    fun consumes(effect: ContextualEffectConsumesDescription): Consumes

    @ContractsDsl
    fun <R> consumes(block: Function<R>, effect: ContextualEffectConsumesDescription): ProvideConsumes

    // -----------------------------------------------------

    @ContractsDsl
    fun provides(fact: FactDescription): ProvidesFact

    @ContractsDsl
    fun <R> provides(block: Function<R>, fact: FactDescription): BlockProvidesFact

    @ContractsDsl
    fun requires(requirement: CheckerDescription): RequiresContext

    @ContractsDsl
    fun <R> requires(block: Function<R>, requirement: CheckerDescription): BlockRequiresContext

    @ContractsDsl
    fun requiresNot(requirement: CheckerNotDescription): RequiresNotContext

    @ContractsDsl
    fun <R> requiresNot(block: Function<R>, requirement: CheckerNotDescription): BlockRequiresNotContext
}

@ContractsDsl
@SinceKotlin("1.2")
internal enum class InvocationKind {
    @ContractsDsl
    AT_MOST_ONCE,

    @ContractsDsl
    AT_LEAST_ONCE,

    @ContractsDsl
    EXACTLY_ONCE,

    @ContractsDsl
    UNKNOWN
}

@ContractsDsl
@InlineOnly
@SinceKotlin("1.2")
internal inline fun contract(builder: ContractBuilder.() -> Unit) {
}