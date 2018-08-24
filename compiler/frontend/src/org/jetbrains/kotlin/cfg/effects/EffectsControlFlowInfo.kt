/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cfg.effects

import com.intellij.util.containers.MultiMap
import org.jetbrains.kotlin.cfg.ControlFlowInfo
import org.jetbrains.kotlin.cfg.ImmutableHashMap
import org.jetbrains.kotlin.cfg.ImmutableMap
import org.jetbrains.kotlin.contracts.contextual.Context
import org.jetbrains.kotlin.contracts.contextual.ContextFact
import org.jetbrains.kotlin.contracts.contextual.ContextFamily
import org.jetbrains.kotlin.psi.KtElement

class FactsControlFlowInfo(map: ImmutableMap<ContextFamily, Context> = ImmutableHashMap.empty()) :
    ControlFlowInfo<FactsControlFlowInfo, ContextFamily, Context>(map) {

    constructor(map: Map<ContextFamily, Context>) : this(ImmutableHashMap.ofAll(map))

    companion object {
        val EMPTY = FactsControlFlowInfo()
    }

    override fun copy(newMap: ImmutableMap<ContextFamily, Context>) = FactsControlFlowInfo(newMap)
}

fun <K, V> MultiMap<K, V>.immutablePut(key: K, value: V): MultiMap<K, V> {
    val copy = this.copy()
    copy.putValue(key, value)
    return copy
}

fun <K, V> MultiMap<K, V>.immutableRemove(key: K): MultiMap<K, V> {
    val copy = this.copy()
    copy.remove(key)
    return copy
}