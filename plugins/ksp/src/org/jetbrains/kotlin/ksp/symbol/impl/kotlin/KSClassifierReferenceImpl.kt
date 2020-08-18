/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ksp.symbol.impl.kotlin

import org.jetbrains.kotlin.ksp.symbol.KSClassifierReference
import org.jetbrains.kotlin.ksp.symbol.KSTypeArgument
import org.jetbrains.kotlin.ksp.symbol.Location
import org.jetbrains.kotlin.ksp.symbol.Origin
import org.jetbrains.kotlin.ksp.symbol.impl.KSObjectCache
import org.jetbrains.kotlin.ksp.symbol.impl.toLocation
import org.jetbrains.kotlin.psi.*

class KSClassifierReferenceImpl private constructor(val ktUserType: KtUserType) : KSClassifierReference {
    companion object : KSObjectCache<KtUserType, KSClassifierReferenceImpl>() {
        fun getCached(ktUserType: KtUserType) = cache.getOrPut(ktUserType) { KSClassifierReferenceImpl(ktUserType) }
    }

    override val origin = Origin.KOTLIN

    override val location: Location by lazy {
        ktUserType.toLocation()
    }

    override val typeArguments: List<KSTypeArgument> by lazy {
        ktUserType.typeArguments.map { KSTypeArgumentKtImpl.getCached(it) }
    }

    override fun referencedName(): String {
        return ktUserType.referencedName ?: ""
    }

    override val qualifier: KSClassifierReference? by lazy {
        if (ktUserType.qualifier == null) {
            null
        } else {
            KSClassifierReferenceImpl.getCached(ktUserType.qualifier!!)
        }
    }

    override fun toString() = referencedName()
}