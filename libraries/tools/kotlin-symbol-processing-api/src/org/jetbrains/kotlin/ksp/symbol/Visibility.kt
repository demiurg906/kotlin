/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ksp.symbol

/**
 * Visibility of the element
 */
enum class Visibility {
    PUBLIC,
    PRIVATE,
    PROTECTED,
    INTERNAL,
    LOCAL,
    JAVA_PACKAGE,
}