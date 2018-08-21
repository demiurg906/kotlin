/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.exceptions

import org.jetbrains.kotlin.compiler.plugin.contracts.ContextualEffectComponent
import org.jetbrains.kotlin.contracts.ContextualEffectFamiliesRegistrar

class ExceptionEffectComponent : ContextualEffectComponent {
    override fun registerProjectComponents(contextualEffectFamiliesRegistrar: ContextualEffectFamiliesRegistrar) {
        contextualEffectFamiliesRegistrar.registerFamily(ExceptionEffectFamily, ::ExceptionEffectParser)
    }
}