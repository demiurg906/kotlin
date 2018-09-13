/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlin.contracts

import kotlin.internal.ContractsDsl
import kotlin.internal.InlineOnly

/**
 * Marker of the use experimental contracts API.
 * Any declaration annotated with that marker must be used with the [UseExperimental] annotation
 * or the compiler argument `-Xuse-experimental=kotlin.contracts.ExperimentalContracts`.
 */
@Retention(AnnotationRetention.BINARY)
@SinceKotlin("1.3")
@Experimental
annotation class ExperimentalContracts

/**
 * The builder is used to specify contract effects for some function.
 *
 * @see contract
 */
@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
interface ContractBuilder {
    /**
     * Expresses that a function returned successfully.
     *
     * @sample samples.contracts.returnsContract
     */
    @ContractsDsl
    fun returns(): Returns

    /**
     * Expresses that a function returned with some value.
     * Value can only be `true`, `false` or `null`.
     *
     * @sample samples.contracts.returnsTrueContract
     * @sample samples.contracts.returnsFalseContract
     * @sample samples.contracts.returnsNullContract
     */
    @ContractsDsl
    fun returns(value: Any?): Returns

    /**
     * Expresses that a function returned with any not null value.
     *
     * @sample samples.contracts.returnsNotNullContract
     */
    @ContractsDsl
    fun returnsNotNull(): ReturnsNotNull

    /**
     * Expresses that:
     *  1) [lambda] will not be called after the call to owner-function is finished;
     *  2) [lambda] will not be passed to another function without the similar contract.
     *
     * @param kind amount of times, that a [lambda] guaranteed will be invoked.
     *
     * Note that a function with the `callsInPlace` effect must be inline.
     *
     * @sample samples.contracts.callsInPlaceAtMostOnceContract
     * @sample samples.contracts.callsInPlaceAtLeastOnceContract
     * @sample samples.contracts.callsInPlaceExactlyOnceContract
     * @sample samples.contracts.callsInPlaceUnknownContract
     */
    @ContractsDsl
    fun <R> callsInPlace(lambda: Function<R>, kind: InvocationKind = InvocationKind.UNKNOWN): CallsInPlace

    // -----------------------------------------------------

    @ContractsDsl
    fun provides(context: ProvidesContextDescription): ProvidesContext

    @ContractsDsl
    fun requires(context: RequiresContextDescription): RequiresContext

    @ContractsDsl
    fun requiresNot(context: NotRequiresContextDescription): RequiresNotContext

    @ContractsDsl
    fun starts(context: StartsContextDescription): StartsContext

    @ContractsDsl
    fun closes(context: ClosesContextDescription): ClosesContext

    @ContractsDsl
    fun <R> callsIn(block: Function<R>, context: CallsBlockInContextDescription): CallsBlockInContext

    @ContractsDsl
    fun <R> expectsTo(block: Function<R>, context: BlockExpectsToContextDescription): BlockExpectsToContext

    @ContractsDsl
    fun <R> notExpectsTo(block: Function<R>, context: BlockNotExpectsToContextDescription): BlockNotExpectsToContext
}

/**
 * This enum class is used to specify the amount of times, that `callable` which is used in the [ContractBuilder.callsInPlace] effect guaranteed will be invoked.
 */
@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
enum class InvocationKind {
    /**
     * Expresses that the `callable` will be invoked zero or one time.
     *
     * @sample samples.contracts.callsInPlaceAtMostOnceContract
     */
    @ContractsDsl
    AT_MOST_ONCE,

    /**
     * Expresses that the `callable` will be invoked one or more times.
     *
     * @sample samples.contracts.callsInPlaceAtLeastOnceContract
     */
    @ContractsDsl
    AT_LEAST_ONCE,

    /**
     * Expresses that the `callable` will be invoked exactly one time.
     *
     * @sample samples.contracts.callsInPlaceExactlyOnceContract
     */
    @ContractsDsl
    EXACTLY_ONCE,

    /**
     * Expresses that the `callable` will be invoked unknown amount of times.
     *
     * @sample samples.contracts.callsInPlaceUnknownContract
     */
    @ContractsDsl
    UNKNOWN
}

/**
 * The function to describe a contract.
 * The contract description must be at the beginning of a function and has at least one effect.
 * Also the contract description can be used only in the top-level functions.
 *
 * @param builder the lambda in the body of which the effects from the [ContractBuilder] are specified.
 *
 * @sample samples.contracts.returnsContract
 * @sample samples.contracts.returnsTrueContract
 * @sample samples.contracts.returnsFalseContract
 * @sample samples.contracts.returnsNullContract
 * @sample samples.contracts.returnsNotNullContract
 * @sample samples.contracts.callsInPlaceAtMostOnceContract
 * @sample samples.contracts.callsInPlaceAtLeastOnceContract
 * @sample samples.contracts.callsInPlaceExactlyOnceContract
 * @sample samples.contracts.callsInPlaceUnknownContract
 */
@ContractsDsl
@ExperimentalContracts
@InlineOnly
@SinceKotlin("1.3")
@Suppress("UNUSED_PARAMETER")
inline fun contract(builder: ContractBuilder.() -> Unit) {}


@ContractsDsl
class ReceiverOf(func: Function<*>)