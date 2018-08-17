/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.safebuilders

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

typealias CallsMap = Map<FunctionDescriptor, CallKind>
typealias BadCallsMap = Map<FunctionDescriptor, List<CallAnalysisResult>>

sealed class AbstractCallEffectsContext : ContextualEffectsContext {
    override val family = CallEffectFamily
}

data class CallEffectsContext(
    val calls: CallsMap = mapOf(),
    val badCalls: BadCallsMap = mapOf()
) : AbstractCallEffectsContext()

object BotCallEffectsContext : AbstractCallEffectsContext()