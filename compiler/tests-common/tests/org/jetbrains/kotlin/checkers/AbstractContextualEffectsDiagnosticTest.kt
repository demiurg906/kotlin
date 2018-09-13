/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.checkers

import org.jetbrains.kotlin.compiler.plugin.contracts.ContextEffectsComponent
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.contracts.dslmarker.DslMarkerEffectsComponent
import org.jetbrains.kotlin.contracts.exceptions.ExceptionContextEffectsComponent
import org.jetbrains.kotlin.contracts.safebuilders.CallContextEffectsComponent
import org.jetbrains.kotlin.contracts.transactions.TransactionContextEffectsComponent

abstract class AbstractContextualEffectsDiagnosticTest : AbstractDiagnosticsTest() {
    override fun performCustomConfiguration(configuration: CompilerConfiguration) {
        configuration.add(
            ContextEffectsComponent.PLUGIN_CONTEXT_EFFECTS,
            ExceptionContextEffectsComponent()
        )

        configuration.add(
            ContextEffectsComponent.PLUGIN_CONTEXT_EFFECTS,
            CallContextEffectsComponent()
        )

        configuration.add(
            ContextEffectsComponent.PLUGIN_CONTEXT_EFFECTS,
            TransactionContextEffectsComponent()
        )

        configuration.add(
            ContextEffectsComponent.PLUGIN_CONTEXT_EFFECTS,
            DslMarkerEffectsComponent()
        )
    }
}