/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.dslmarker

import org.jetbrains.kotlin.contracts.contextual.extensions.SpecificContractExtension
import org.jetbrains.kotlin.contracts.contextual.model.ContextFamily
import org.jetbrains.kotlin.contracts.contextual.parsing.PsiEffectDeclarationExtractor
import org.jetbrains.kotlin.contracts.parsing.PsiContractVariableParserDispatcher
import org.jetbrains.kotlin.resolve.BindingContext

class DslMarkerContract : SpecificContractExtension {
    override fun getFamily(): ContextFamily = DslMarkerFamily

    override fun getParser(bindingContext: BindingContext, dispatcher: PsiContractVariableParserDispatcher): PsiEffectDeclarationExtractor =
        PsiDslMarkerEffectDeclarationExtractor(bindingContext, dispatcher)
}