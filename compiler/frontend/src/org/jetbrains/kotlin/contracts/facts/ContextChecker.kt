/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.facts

import org.jetbrains.kotlin.resolve.BindingTrace

abstract class ContextChecker {
    abstract val family: ContextFamily
    abstract fun verifyContext(context: Context, trace: BindingTrace, shouldReport: Boolean): Context
}