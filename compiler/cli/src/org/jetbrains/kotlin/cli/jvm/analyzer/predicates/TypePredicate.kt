/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.types.KotlinType

class TypePredicate(val typeName: String) {
    companion object {
        val Int = TypePredicate("Int")
        val Double = TypePredicate("Double")
        val Boolean = TypePredicate("Booolean")
        val String = TypePredicate("String")
    }

    fun checkType(type: KotlinType): Boolean {
        return type.toString() == typeName
//        val declarationType = type.toString().split(" ").getOrNull(2) ?: return false
//        val declarationType = type.toString().split(" ").getOrNull(2) ?: return false
//        return typeName == declarationType
    }
}