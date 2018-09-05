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
import org.jetbrains.kotlin.contracts.facts.Context
import org.jetbrains.kotlin.contracts.facts.ContextFamily

class ContractsContextsInfo(map: ImmutableMap<ContextFamily, Map<Int, Context>> = ImmutableHashMap.empty()) :
    ControlFlowInfo<ContractsContextsInfo, ContextFamily, Map<Int, Context>>(map) {

    constructor(map: Map<ContextFamily, Map<Int, Context>>) : this(ImmutableHashMap.ofAll(map))

    companion object {
        val EMPTY = ContractsContextsInfo()
    }

    override fun copy(newMap: ImmutableMap<ContextFamily, Map<Int, Context>>) = ContractsContextsInfo(newMap)
}