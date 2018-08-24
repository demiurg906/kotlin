/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compiler.plugin.contracts

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.contracts.ContextFamiliesRegistrar

interface ContextEffectsComponent {
    companion object {
        val PLUGIN_CONTEXT_EFFECTS: CompilerConfigurationKey<MutableList<ContextEffectsComponent>> =
            CompilerConfigurationKey.create("context/facts effects component")
    }

    fun registerProjectComponents(contextFamiliesRegistrar: ContextFamiliesRegistrar)
}
