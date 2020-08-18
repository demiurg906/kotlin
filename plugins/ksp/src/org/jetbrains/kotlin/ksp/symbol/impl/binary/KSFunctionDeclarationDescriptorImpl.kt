/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ksp.symbol.impl.binary

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.impl.AnonymousFunctionDescriptor
import org.jetbrains.kotlin.ksp.isOpen
import org.jetbrains.kotlin.ksp.isVisibleFrom
import org.jetbrains.kotlin.ksp.processing.impl.ResolverImpl
import org.jetbrains.kotlin.ksp.symbol.*
import org.jetbrains.kotlin.ksp.symbol.impl.KSObjectCache
import org.jetbrains.kotlin.ksp.symbol.impl.kotlin.KSNameImpl
import org.jetbrains.kotlin.ksp.symbol.impl.toFunctionKSModifiers
import org.jetbrains.kotlin.ksp.symbol.impl.toKSModifiers
import org.jetbrains.kotlin.load.java.isFromJava
import org.jetbrains.kotlin.resolve.OverridingUtil
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.parents

class KSFunctionDeclarationDescriptorImpl private constructor(val descriptor: FunctionDescriptor) : KSFunctionDeclaration,
    KSDeclarationDescriptorImpl(descriptor),
    KSExpectActual by KSExpectActualDescriptorImpl(descriptor) {
    companion object : KSObjectCache<FunctionDescriptor, KSFunctionDeclarationDescriptorImpl>() {
        fun getCached(descriptor: FunctionDescriptor) = cache.getOrPut(descriptor) { KSFunctionDeclarationDescriptorImpl(descriptor) }
    }

    override fun overrides(overridee: KSFunctionDeclaration): Boolean {
        if (!overridee.isOpen())
            return false
        if (!overridee.isVisibleFrom(this))
            return false
        val superDescriptor = ResolverImpl.instance.resolveFunctionDeclaration(overridee) ?: return false
        return OverridingUtil.DEFAULT.isOverridableBy(
            superDescriptor, descriptor, null
        ).result == OverridingUtil.OverrideCompatibilityInfo.Result.OVERRIDABLE
    }

    override val typeParameters: List<KSTypeParameter> by lazy {
        descriptor.typeParameters.map { KSTypeParameterDescriptorImpl.getCached(it) }
    }

    override val declarations: List<KSDeclaration> = emptyList()

    override val extensionReceiver: KSTypeReference? by lazy {
        val extensionReceiver = descriptor.extensionReceiverParameter?.type
        if (extensionReceiver != null) {
            KSTypeReferenceDescriptorImpl.getCached(extensionReceiver)
        } else {
            null
        }
    }

    override val functionKind: FunctionKind by lazy {
        when {
            descriptor.dispatchReceiverParameter == null -> if (descriptor.isFromJava) FunctionKind.STATIC else FunctionKind.TOP_LEVEL
            !descriptor.name.isSpecial && !descriptor.name.asString().isEmpty() -> FunctionKind.MEMBER
            descriptor is AnonymousFunctionDescriptor -> FunctionKind.ANONYMOUS
            else -> throw IllegalStateException()
        }
    }

    override val isAbstract: Boolean by lazy {
        this.modifiers.contains(Modifier.ABSTRACT)
    }

    override val modifiers: Set<Modifier> by lazy {
        val modifiers = mutableSetOf<Modifier>()
        modifiers.addAll(descriptor.toKSModifiers())
        modifiers.addAll(descriptor.toFunctionKSModifiers())
        modifiers
    }

    override val parameters: List<KSVariableParameter> by lazy {
        descriptor.valueParameters.map { KSVariableParameterDescriptorImpl.getCached(it) }
    }

    override val returnType: KSTypeReference? by lazy {
        val returnType = descriptor.returnType
        if (returnType == null) {
            null
        } else {
            KSTypeReferenceDescriptorImpl.getCached(returnType)
        }
    }

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
        return visitor.visitFunctionDeclaration(this, data)
    }
}