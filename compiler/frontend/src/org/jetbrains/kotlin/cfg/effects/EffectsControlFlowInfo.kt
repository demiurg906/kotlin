/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cfg.effects

import org.jetbrains.kotlin.cfg.ControlFlowInfo
import org.jetbrains.kotlin.cfg.ImmutableHashMap
import org.jetbrains.kotlin.cfg.ImmutableMap
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsHolder

class EffectsControlFlowInfo(map: ImmutableMap<ContextualEffectFamily, ContextualEffectsHolder> = ImmutableHashMap.empty()) :
    ControlFlowInfo<EffectsControlFlowInfo, ContextualEffectFamily, ContextualEffectsHolder>(map) {

    override fun copy(newMap: ImmutableMap<ContextualEffectFamily, ContextualEffectsHolder>) = EffectsControlFlowInfo(newMap)
}
