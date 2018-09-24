/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts

import org.jetbrains.kotlin.contracts.facts.ContextCleaner
import org.jetbrains.kotlin.contracts.facts.ContextProvider
import org.jetbrains.kotlin.contracts.facts.ContextVerifier

data class FactsBindingInfo(
    val providers: Collection<ContextProvider> = listOf(),
    val verifiers: Collection<ContextVerifier> = listOf(),
    val cleaners: Collection<ContextCleaner> = listOf()
)