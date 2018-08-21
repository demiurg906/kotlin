/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compiler.plugin.contracts

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.contracts.ContextualEffectFamiliesRegistrar

interface ContextualEffectComponent {
    companion object {
        val PLUGIN_CONTEXTUAL_EFFECTS: CompilerConfigurationKey<MutableList<ContextualEffectComponent>> =
            CompilerConfigurationKey.create("contextual effects component")
    }

    fun registerProjectComponents(contextualEffectFamiliesRegistrar: ContextualEffectFamiliesRegistrar)
}
