/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts

import org.jetbrains.kotlin.contracts.facts.ContextFamily
import org.jetbrains.kotlin.contracts.parsing.PsiContractVariableParserDispatcher
import org.jetbrains.kotlin.contracts.parsing.PsiEffectDeclarationExtractor
import org.jetbrains.kotlin.resolve.BindingContext

typealias FactParserConstructorKind = (BindingContext, PsiContractVariableParserDispatcher) -> PsiEffectDeclarationExtractor

interface ContextFamiliesRegistrar {
    fun registerFamily(family: ContextFamily, newParser: FactParserConstructorKind)
}

interface ContextFamiliesProvider {
    fun getFamilies(): Collection<ContextFamily>
    fun getParsers(): Collection<FactParserConstructorKind>
}

object ContextFamiliesService : ContextFamiliesRegistrar, ContextFamiliesProvider {
    private val families = mutableSetOf<ContextFamily>()
    private val parsers = mutableSetOf<FactParserConstructorKind>()

    override fun registerFamily(family: ContextFamily, newParser: FactParserConstructorKind) {
        families.add(family)
        parsers.add(newParser)
    }

    override fun getFamilies(): Collection<ContextFamily> = families

    override fun getParsers(): Collection<FactParserConstructorKind> = parsers
}