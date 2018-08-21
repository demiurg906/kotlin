/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.checkers

import org.jetbrains.kotlin.compiler.plugin.contracts.ContextualEffectComponent
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.contracts.contextual.exceptions.ExceptionEffectComponent
import org.jetbrains.kotlin.contracts.contextual.safebuilders.CallEffectComponent

abstract class AbstractContextualEffectsDiagnosticTest : AbstractDiagnosticsTest() {
    override fun performCustomConfiguration(configuration: CompilerConfiguration) {
        configuration.add(ContextualEffectComponent.PLUGIN_CONTEXTUAL_EFFECTS, ExceptionEffectComponent())
        configuration.add(ContextualEffectComponent.PLUGIN_CONTEXTUAL_EFFECTS, CallEffectComponent())
    }

//    override fun createEnvironment(file: File): KotlinCoreEnvironment = super.createEnvironment(file).apply {
//
//    }

}