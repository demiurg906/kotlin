/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.facts

import org.jetbrains.kotlin.diagnostics.DiagnosticSink

abstract class ContextVerifier {
    abstract val family: ContextFamily
    abstract fun verify(context: Context, diagnosticSink: DiagnosticSink)
    abstract fun cleanupProcessed(context: Context): Context
}

