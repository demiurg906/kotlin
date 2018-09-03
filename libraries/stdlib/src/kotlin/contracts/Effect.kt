/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlin.contracts

import kotlin.internal.ContractsDsl

@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public interface Effect

@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public interface ConditionalEffect : Effect

@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public interface SimpleEffect {
    @ContractsDsl
    @ExperimentalContracts
    public infix fun implies(booleanExpression: Boolean): ConditionalEffect
}


@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public interface Returns : SimpleEffect

@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public interface ReturnsNotNull : SimpleEffect

@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public interface CallsInPlace : SimpleEffect

// -----------------------------------------------------

@ContractsDsl
@SinceKotlin("1.3")
interface FactDescription

@ContractsDsl
@SinceKotlin("1.3")
interface CheckerDescription

@ContractsDsl
@SinceKotlin("1.3")
interface CheckerNotDescription

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