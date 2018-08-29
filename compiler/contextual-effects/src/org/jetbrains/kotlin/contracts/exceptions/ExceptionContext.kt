/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.exceptions

import com.intellij.util.containers.MultiMap
import org.jetbrains.kotlin.contracts.facts.AbstractContext
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.types.KotlinType

data class ExceptionContext(
    val depths: MultiMap<KotlinType, Int> = MultiMap.create()
) : AbstractContext() {
    val cachedExceptions: Set<KotlinType>
        get() = depths.keySet()

    constructor(cachedException: KotlinType) : this(MultiMap.create()) {
        depths.putValue(cachedException, -1)
    }

    override val family = ExceptionFamily

    override fun reportRemaining(sink: DiagnosticSink) {}
}