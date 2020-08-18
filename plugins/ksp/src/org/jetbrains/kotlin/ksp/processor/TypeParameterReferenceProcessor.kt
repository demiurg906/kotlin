/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ksp.processor

import org.jetbrains.kotlin.ksp.processing.Resolver
import org.jetbrains.kotlin.ksp.symbol.*
import org.jetbrains.kotlin.ksp.symbol.impl.binary.KSTypeReferenceDescriptorImpl
import org.jetbrains.kotlin.ksp.symbol.impl.kotlin.KSTypeImpl
import org.jetbrains.kotlin.ksp.visitor.KSTopDownVisitor

open class TypeParameterReferenceProcessor: AbstractTestProcessor() {
    val results = mutableListOf<String>()
    val collector = ReferenceCollector()
    val references = mutableSetOf<KSTypeReference>()

    override fun process(resolver: Resolver) {
        val files = resolver.getAllFiles()

        files.forEach {
            it.accept(collector, references)
        }

        val sortedReferences = references.filter { it.element is KSClassifierReference && it.origin == Origin.KOTLIN }.sortedBy { (it.element as KSClassifierReference).referencedName() }

        for (i in sortedReferences) {
            val r = i.resolve()
            results.add("${r?.declaration?.qualifiedName?.asString()}")
        }
    }

    override fun toResult(): List<String> {
        return results
    }

}
