/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlin.internal.contracts

import kotlin.reflect.KFunction

// TODO: move to plugin
class CatchesException<T : Throwable> : FactDescription, CheckerDescription


class Calls(func: KFunction<*>) : FactDescription

class CallKind(func: KFunction<*>, callKind: DslCallKind)

enum class DslCallKind {
    AT_MOST_ONCE,
    EXACTLY_ONCE,
    AT_LEAST_ONCE
}