/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlin.contracts

import kotlin.reflect.KFunction

// ------------------ Checked Exceptions ------------------

class CatchesException<T : Throwable> : BlockProvidesContextDescription, RequiresContextDescription


// ------------------ Safe Builders ------------------

class Calls(func: KFunction<*>, thisReference: Any) : ProvidesContextDescription

class CallKind(func: KFunction<*>, callKind: DslCallKind, receiver: ReceiverOf) : BlockRequiresContextDescription

enum class DslCallKind {
    AT_MOST_ONCE,
    EXACTLY_ONCE,
    AT_LEAST_ONCE
}

// ------------------ Transactions ------------------

class OpenedTransaction(thisReference: Any) : StartsContextDescription, ClosesContextDescription, RequiresContextDescription

// ------------------ DSLMarker ------------------

class DslMarkers : RequiresContextDescription, BlockProvidesContextDescription {
    constructor(thisReference: Any)
    constructor(receiver: ReceiverOf)
}