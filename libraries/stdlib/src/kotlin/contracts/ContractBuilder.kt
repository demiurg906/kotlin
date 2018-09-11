/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlin.contracts

import kotlin.internal.ContractsDsl
import kotlin.internal.InlineOnly

@Retention(AnnotationRetention.BINARY)
@SinceKotlin("1.3")
@Experimental
annotation class ExperimentalContracts

@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
interface ContractBuilder {
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
    fun provides(context: ProvidesContextDescription): ProvidesFact

    @ContractsDsl
    fun <R> provides(block: Function<R>, context: BlockProvidesContextDescription): BlockProvidesFact

    @ContractsDsl
    fun requires(requirement: RequiresContextDescription): RequiresContext

    @ContractsDsl
    fun <R> requires(block: Function<R>, requirement: BlockRequiresContextDescription): BlockRequiresContext

    @ContractsDsl
    fun requiresNot(requirement: NotRequiresContextDescription): RequiresNotContext

    @ContractsDsl
    fun <R> requiresNot(block: Function<R>, requirement: BlockNotRequiresContextDescription): BlockRequiresNotContext

    @ContractsDsl
    fun starts(context: StartsContextDescription): StartsContext

    @ContractsDsl
    fun closes(context: ClosesContextDescription): ClosesContext
}

@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
enum class InvocationKind {
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
@ExperimentalContracts
@InlineOnly
@SinceKotlin("1.3")
@Suppress("UNUSED_PARAMETER")
inline fun contract(builder: ContractBuilder.() -> Unit) {
}


@ContractsDsl
class ReceiverOf(func: Function<*>)