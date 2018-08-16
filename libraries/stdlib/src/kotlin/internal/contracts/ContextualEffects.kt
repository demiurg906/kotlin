/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlin.internal.contracts

import kotlin.internal.ContractsDsl
import kotlin.reflect.KFunction

@ContractsDsl
class ExceptionEffectDescription<T : Throwable> : ContextualEffectSuppliesDescription, ContextualEffectConsumesDescription

@ContractsDsl
class CallEffect(func: KFunction<*>) : ContextualEffectSuppliesDescription

@ContractsDsl
class RequiresCallEffect(func: KFunction<*>, callKind: DslCallKind) : ContextualEffectConsumesDescription

enum class DslCallKind {
    AT_MOST_ONCE,
    EXACTLY_ONCE,
    AT_LEAST_ONCE
}