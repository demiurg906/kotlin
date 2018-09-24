/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.dslmarker

import org.jetbrains.kotlin.compiler.plugin.contracts.ContextEffectsComponent
import org.jetbrains.kotlin.contracts.ContextFamiliesRegistrar

class DslMarkerEffectsComponent : ContextEffectsComponent {
    override fun registerProjectComponents(contextFamiliesRegistrar: ContextFamiliesRegistrar) {
        contextFamiliesRegistrar.registerFamily(DslMarkerFamily, ::PsiDslMarkerEffectDeclarationExtractor)
    }
}