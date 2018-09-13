/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.dslmarker

import org.jetbrains.kotlin.contracts.facts.ContextProvider
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue

class DslMarkerProvider(val receiver: ReceiverValue) : ContextProvider {
    override val family = DslMarkerFamily
}