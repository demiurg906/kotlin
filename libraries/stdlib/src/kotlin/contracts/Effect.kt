/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlin.contracts

import kotlin.internal.ContractsDsl

@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
interface Effect

@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
interface ConditionalEffect : Effect

@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
interface SimpleEffect {
    @ContractsDsl
    @ExperimentalContracts
    infix fun implies(booleanExpression: Boolean): ConditionalEffect
}


@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
interface Returns : SimpleEffect

@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
interface ReturnsNotNull : SimpleEffect

@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
interface CallsInPlace : SimpleEffect

// -----------------------------------------------------

@ContractsDsl
@SinceKotlin("1.3")
interface ProvidesContextDescription

@ContractsDsl
@SinceKotlin("1.3")
interface RequiresContextDescription

@ContractsDsl
@SinceKotlin("1.3")
interface RequirementNotDescription

@ContractsDsl
@SinceKotlin("1.3")
interface StartsContextDescription

@ContractsDsl
@SinceKotlin("1.3")
interface ClosesContextDescription


@ContractsDsl
@SinceKotlin("1.3")
interface ProvidesFact : Effect

@ContractsDsl
@SinceKotlin("1.3")
interface BlockProvidesFact : Effect

@ContractsDsl
@SinceKotlin("1.3")
interface RequiresContext : Effect

@ContractsDsl
@SinceKotlin("1.3")
interface BlockRequiresContext : Effect

@ContractsDsl
@SinceKotlin("1.3")
interface RequiresNotContext : Effect

@ContractsDsl
@SinceKotlin("1.3")
interface BlockRequiresNotContext : Effect

@ContractsDsl
@SinceKotlin("1.3")
interface StartsContext : Effect

@ContractsDsl
@SinceKotlin("1.3")
interface ClosesContext : Effect