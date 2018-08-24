/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.facts

abstract class ContextFamily {
    abstract val id: String
    abstract val combiner: FactsCombiner
    abstract val emptyContext: Context
}