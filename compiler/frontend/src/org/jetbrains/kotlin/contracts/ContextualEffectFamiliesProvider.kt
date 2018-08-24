/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts

import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily
import org.jetbrains.kotlin.contracts.parsing.ContextualEffectParser
import org.jetbrains.kotlin.resolve.BindingContext

interface ContextualEffectFamiliesRegistrar {
    fun registerFamily(family: ContextualEffectFamily, newParser: (BindingContext) -> ContextualEffectParser)
}

interface ContextualEffectFamiliesProvider {
    fun getFamilies(): Collection<ContextualEffectFamily>
    fun getParsers(): Collection<(BindingContext) -> ContextualEffectParser>
}

object ContextualEffectFamiliesService : ContextualEffectFamiliesRegistrar, ContextualEffectFamiliesProvider {
    private val families = mutableSetOf<ContextualEffectFamily>()
    private val parsers = mutableSetOf<(BindingContext) -> ContextualEffectParser>()

    override fun registerFamily(family: ContextualEffectFamily, newParser: (BindingContext) -> ContextualEffectParser) {
        families.add(family)
        parsers.add(newParser)
    }

    override fun getFamilies(): Collection<ContextualEffectFamily> = families

    override fun getParsers(): Collection<(BindingContext) -> ContextualEffectParser> = parsers
}

// -----------------------------------------------------------------------------------------------------------------

typealias FactParserConstructorKind = (BindingContext, PsiContractParserDispatcher) -> ContextFactParser

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