/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlin.contracts

import kotlin.internal.ContractsDsl
import kotlin.reflect.KFunction

// ------------------ Checked Exceptions ------------------

class CatchesException<T : Throwable> : FactDescription, CheckerDescription

// ------------------ Safe Builders ------------------

@ContractsDsl
class ReceiverOf(func: Function<*>)

class Calls(func: KFunction<*>, thisReference: Any) : FactDescription

class CallKind(func: KFunction<*>, callKind: DslCallKind, receiver: ReceiverOf) : CheckerDescription

enum class DslCallKind {
    AT_MOST_ONCE,
    EXACTLY_ONCE,
    AT_LEAST_ONCE
}