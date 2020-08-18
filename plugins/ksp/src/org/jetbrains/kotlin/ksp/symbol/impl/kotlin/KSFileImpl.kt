/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ksp.symbol.impl.kotlin

import org.jetbrains.kotlin.ksp.symbol.*
import org.jetbrains.kotlin.ksp.symbol.impl.KSObjectCache
import org.jetbrains.kotlin.ksp.symbol.impl.getKSDeclarations
import org.jetbrains.kotlin.ksp.symbol.impl.toLocation
import org.jetbrains.kotlin.psi.KtFile

class KSFileImpl private constructor(val file: KtFile) : KSFile {
    companion object : KSObjectCache<KtFile, KSFileImpl>() {
        fun getCached(file: KtFile) = cache.getOrPut(file) { KSFileImpl(file) }
    }

    override val origin = Origin.KOTLIN

    override val location: Location by lazy {
        file.toLocation()
    }

    override val packageName: KSName by lazy {
        KSNameImpl.getCached(file.packageFqName.toString())
    }

    override val annotations: List<KSAnnotation> by lazy {
        file.annotationEntries.map { KSAnnotationImpl.getCached(it) }
    }

    override val declarations: List<KSDeclaration> by lazy {
        file.declarations.getKSDeclarations()
    }

    override val fileName: String by lazy {
        file.name
    }

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
        return visitor.visitFile(this, data)
    }
}