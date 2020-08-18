/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ksp.symbol.impl.kotlin

import org.jetbrains.kotlin.ksp.processing.impl.ResolverImpl
import org.jetbrains.kotlin.ksp.symbol.*
import org.jetbrains.kotlin.ksp.symbol.impl.KSObjectCache
import org.jetbrains.kotlin.ksp.symbol.impl.toKSModifiers
import org.jetbrains.kotlin.ksp.symbol.impl.toLocation
import org.jetbrains.kotlin.psi.*
import java.lang.IllegalStateException

class KSTypeReferenceImpl private constructor(val ktTypeReference: KtTypeReference) : KSTypeReference {
    companion object : KSObjectCache<KtTypeReference, KSTypeReferenceImpl>() {
        fun getCached(ktTypeReference: KtTypeReference) = cache.getOrPut(ktTypeReference) { KSTypeReferenceImpl(ktTypeReference) }
    }

    override val origin = Origin.KOTLIN

    override val location: Location by lazy {
        ktTypeReference.toLocation()
    }

    override val annotations: List<KSAnnotation> by lazy {
        ktTypeReference.annotationEntries.map { KSAnnotationImpl.getCached(it) }
    }

    override val modifiers: Set<Modifier> by lazy {
        ktTypeReference.toKSModifiers()
    }

    override val element: KSReferenceElement by lazy {
        var typeElement = ktTypeReference.typeElement
        while (typeElement is KtNullableType)
            typeElement = typeElement.innerType
        when (typeElement) {
            is KtFunctionType -> KSCallableReferenceImpl.getCached(typeElement)
            is KtUserType -> KSClassifierReferenceImpl.getCached(typeElement)
            is KtDynamicType -> KSDynamicReferenceImpl.getCached(Unit)
            else -> throw IllegalStateException()
        }
    }

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
        return visitor.visitTypeReference(this, data)
    }

    override fun resolve(): KSType? = ResolverImpl.instance.resolveUserType(this)
}