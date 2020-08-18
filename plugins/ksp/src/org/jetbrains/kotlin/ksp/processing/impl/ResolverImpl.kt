/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ksp.processing.impl

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.container.get
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ksp.processing.KSBuiltIns
import org.jetbrains.kotlin.ksp.processing.Resolver
import org.jetbrains.kotlin.ksp.symbol.*
import org.jetbrains.kotlin.ksp.symbol.impl.binary.*
import org.jetbrains.kotlin.ksp.symbol.impl.findPsi
import org.jetbrains.kotlin.ksp.symbol.impl.java.*
import org.jetbrains.kotlin.ksp.symbol.impl.kotlin.*
import org.jetbrains.kotlin.load.java.components.TypeUsage
import org.jetbrains.kotlin.load.java.lazy.JavaResolverComponents
import org.jetbrains.kotlin.load.java.lazy.LazyJavaResolverContext
import org.jetbrains.kotlin.load.java.lazy.ModuleClassResolver
import org.jetbrains.kotlin.load.java.lazy.TypeParameterResolver
import org.jetbrains.kotlin.load.java.lazy.descriptors.LazyJavaTypeParameterDescriptor
import org.jetbrains.kotlin.load.java.lazy.types.JavaTypeResolver
import org.jetbrains.kotlin.load.java.lazy.types.toAttributes
import org.jetbrains.kotlin.load.java.structure.impl.JavaClassImpl
import org.jetbrains.kotlin.load.java.structure.impl.JavaMethodImpl
import org.jetbrains.kotlin.load.java.structure.impl.JavaTypeImpl
import org.jetbrains.kotlin.load.java.structure.impl.JavaTypeParameterImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.bindingContextUtil.getAbbreviatedTypeOrType
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.resolve.constants.evaluate.ConstantExpressionEvaluator
import org.jetbrains.kotlin.resolve.jvm.multiplatform.JavaActualAnnotationArgumentExtractor
import org.jetbrains.kotlin.resolve.lazy.DeclarationScopeProvider
import org.jetbrains.kotlin.resolve.lazy.ResolveSession
import org.jetbrains.kotlin.resolve.multiplatform.findActuals
import org.jetbrains.kotlin.resolve.multiplatform.findExpects
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.replaceArgumentsWithStarProjections
import org.jetbrains.kotlin.util.containingNonLocalDeclaration

class ResolverImpl(
    val module: ModuleDescriptor,
    files: Collection<KtFile>,
    javaFiles: Collection<PsiJavaFile>,
    val bindingTrace: BindingTrace,
    val project: Project,
    componentProvider: ComponentProvider
) : Resolver {
    val ksFiles: List<KSFile>
    val psiDocumentManager = PsiDocumentManager.getInstance(project)
    val javaActualAnnotationArgumentExtractor = JavaActualAnnotationArgumentExtractor()
    private val nameToKSMap: MutableMap<KSName, KSClassDeclaration>

    companion object {
        lateinit var resolveSession: ResolveSession
        lateinit var bodyResolver: BodyResolver
        lateinit var constantExpressionEvaluator: ConstantExpressionEvaluator
        lateinit var declarationScopeProvider: DeclarationScopeProvider
        lateinit var topDownAnalyzer: LazyTopDownAnalyzer
        lateinit var instance: ResolverImpl
        lateinit var annotationResolver: AnnotationResolver
        lateinit var moduleClassResolver: ModuleClassResolver
        lateinit var javaTypeResolver: JavaTypeResolver
        lateinit var lazyJavaResolverContext: LazyJavaResolverContext
    }

    init {
        resolveSession = componentProvider.get()
        bodyResolver = componentProvider.get()
        declarationScopeProvider = componentProvider.get()
        topDownAnalyzer = componentProvider.get()
        constantExpressionEvaluator = componentProvider.get()
        annotationResolver = resolveSession.annotationResolver

        ksFiles = files.map { KSFileImpl.getCached(it) } + javaFiles.map { KSFileJavaImpl.getCached(it) }
        val javaResolverComponents = componentProvider.get<JavaResolverComponents>()
        lazyJavaResolverContext = LazyJavaResolverContext(javaResolverComponents, TypeParameterResolver.EMPTY) { null }
        javaTypeResolver = lazyJavaResolverContext.typeResolver
        moduleClassResolver = lazyJavaResolverContext.components.moduleClassResolver

        instance = this

        nameToKSMap = mutableMapOf()

        val visitor = object : KSVisitorVoid() {
            override fun visitFile(file: KSFile, data: Unit) {
                file.declarations.map { it.accept(this, data) }
            }

            override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
                val qualifiedName = classDeclaration.qualifiedName
                if (qualifiedName != null) {
                    nameToKSMap[qualifiedName] = classDeclaration
                }
                classDeclaration.declarations.map { it.accept(this, data) }
            }
        }
        ksFiles.map { it.accept(visitor, Unit) }


    }

    override fun getAllFiles(): List<KSFile> {
        return ksFiles
    }

    override fun getClassDeclarationByName(name: KSName): KSClassDeclaration? {
        nameToKSMap[name]?.let { return it }

        return (module.resolveClassByFqName(FqName(name.asString()), NoLookupLocation.FROM_BUILTINS)
            ?: module.resolveClassByFqName(FqName(name.asString()), NoLookupLocation.FROM_DESERIALIZATION))
            ?.let {
                val psi = it.findPsi()
                if (psi != null) {
                    when (psi) {
                        is KtClassOrObject -> KSClassDeclarationImpl.getCached(psi)
                        is PsiClass -> KSClassDeclarationJavaImpl.getCached(psi)
                        else -> throw IllegalStateException("unexpected psi: ${psi.javaClass}")
                    }
                } else {
                    KSClassDeclarationDescriptorImpl.getCached(it)
                }
            }
    }

    override fun getSymbolsWithAnnotation(annotationName: String): List<KSAnnotated> {
        val ksName = KSNameImpl.getCached(annotationName)

        val visitor = object : BaseVisitor() {
            val symbols = mutableSetOf<KSAnnotated>()
            override fun visitAnnotated(annotated: KSAnnotated, data: Unit) {
                if (annotated.annotations.any {
                        val annotationType = it.annotationType
                        (annotationType.element as? KSClassifierReference)?.referencedName().let { it == null || it == ksName.getShortName() }
                                && annotationType.resolve()?.declaration?.qualifiedName == ksName
                    }) {
                    symbols.add(annotated)
                }
            }

            override fun visitClassDeclaration(type: KSClassDeclaration, data: Unit) {
                visitAnnotated(type, data)
                super.visitClassDeclaration(type, data)
            }

            override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
                visitAnnotated(function, data)
                super.visitFunctionDeclaration(function, data)
            }

            override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
                visitAnnotated(property, data)
                super.visitPropertyDeclaration(property, data)
            }

            override fun visitTypeParameter(typeParameter: KSTypeParameter, data: Unit) {
                visitAnnotated(typeParameter, data)
                super.visitTypeParameter(typeParameter, data)
            }

            override fun visitTypeReference(typeReference: KSTypeReference, data: Unit) {
                visitAnnotated(typeReference, data)
                super.visitTypeReference(typeReference, data)
            }

        }

        for (file in ksFiles) {
            file.accept(visitor, Unit)
        }
        return visitor.symbols.toList()
    }

    override fun getKSNameFromString(name: String): KSName {
        return KSNameImpl.getCached(name)
    }

    fun evaluateConstant(expression: KtExpression?, expectedType: KotlinType): ConstantValue<*>? {
        return expression?.let { constantExpressionEvaluator.evaluateToConstantValue(it, bindingTrace, expectedType) }
    }

    fun resolveDeclaration(declaration: KtDeclaration): DeclarationDescriptor? {
        return if (KtPsiUtil.isLocal(declaration)) {
            resolveContainingFunctionBody(declaration)
            bindingTrace.bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, declaration)
        } else {
            resolveSession.resolveToDescriptor(declaration)
        }
    }

    // TODO: Resolve Java fields/variables is not supported by this function. Not needed currently.
    fun resolveJavaDeclaration(psi: PsiElement): DeclarationDescriptor? {
        return when (psi) {
            is PsiClass -> moduleClassResolver.resolveClass(JavaClassImpl(psi))
            is PsiMethod -> moduleClassResolver.resolveClass(JavaMethodImpl(psi).containingClass)
                ?.unsubstitutedMemberScope!!.getDescriptorsFiltered().single { it.findPsi() == psi } as FunctionDescriptor
            else -> throw IllegalStateException("unhandled psi element kind: ${psi.javaClass}")
        }
    }

    fun resolveFunctionDeclaration(function: KSFunctionDeclaration): FunctionDescriptor? {
        return when (function) {
            is KSFunctionDeclarationImpl -> resolveDeclaration(function.ktFunction)
            is KSFunctionDeclarationDescriptorImpl -> function.descriptor
            is KSFunctionDeclarationJavaImpl -> resolveJavaDeclaration(function.psi)
            else -> throw IllegalStateException("unexpected class: ${function.javaClass}")
        } as FunctionDescriptor?
    }

    fun resolvePropertyDeclaration(property: KSPropertyDeclaration): PropertyDescriptor? {
        return when (property) {
            is KSPropertyDeclarationImpl -> resolveDeclaration(property.ktProperty)
            is KSPropertyDeclarationParameterImpl -> resolveDeclaration(property.ktParameter)
            is KSPropertyDeclarationDescriptorImpl -> property.descriptor
            is KSPropertyDeclarationJavaImpl -> throw IllegalStateException("should not invoke resolve on Java fields")
            else -> throw IllegalStateException("unexpected class: ${property.javaClass}")
        } as PropertyDescriptor?
    }

    fun resolveJavaType(psi: PsiType): KotlinType {
        val javaType = JavaTypeImpl.create(psi)
        return javaTypeResolver.transformJavaType(javaType, TypeUsage.COMMON.toAttributes())
    }

    fun resolveUserType(type: KSTypeReference): KSType? {
        when (type) {
            is KSTypeReferenceImpl -> {
                val typeReference = type.ktTypeReference
                typeReference.getAbbreviatedTypeOrType(bindingTrace.bindingContext)?.let {
                    return KSTypeImpl.getCached(it, type.element.typeArguments, type.annotations)
                }
                KtStubbedPsiUtil.getContainingDeclaration(typeReference)?.let {
                    resolveDeclaration(it)
                    typeReference.getAbbreviatedTypeOrType(bindingTrace.bindingContext)?.let {
                        return KSTypeImpl.getCached(it, type.element.typeArguments, type.annotations)
                    }
                }
                val scope = resolveSession.fileScopeProvider.getFileResolutionScope(typeReference.containingKtFile)
                return resolveSession.typeResolver.resolveType(scope, typeReference, bindingTrace, false).let {
                    KSTypeImpl.getCached(it, type.element.typeArguments, type.annotations)
                }
            }
            is KSTypeReferenceDescriptorImpl -> {
                return KSTypeImpl.getCached(type.kotlinType)
            }
            is KSTypeReferenceJavaImpl -> {
                val psi = (type.psi as? PsiClassReferenceType)?.resolve()
                if (psi is PsiTypeParameter) {
                    val containingDeclaration = if (psi.owner is PsiClass) {
                        moduleClassResolver.resolveClass(JavaClassImpl(psi.owner as PsiClass))
                    } else {
                        moduleClassResolver.resolveClass(
                            JavaMethodImpl(psi.owner as PsiMethod).containingClass
                        )?.unsubstitutedMemberScope!!.getDescriptorsFiltered().single { it.findPsi() == psi.owner } as FunctionDescriptor
                    } as DeclarationDescriptor
                    return KSTypeImpl.getCached(
                        LazyJavaTypeParameterDescriptor(
                            lazyJavaResolverContext,
                            JavaTypeParameterImpl(psi),
                            psi.index,
                            containingDeclaration
                        ).defaultType
                    )

                } else {
                    return KSTypeImpl.getCached(resolveJavaType(type.psi), type.element.typeArguments, type.annotations)
                }
            }
            else -> throw IllegalStateException()
        }
    }

    fun findDeclaration(kotlinType: KotlinType): KSDeclaration {
        val descriptor = kotlinType.constructor.declarationDescriptor
        val psi = descriptor?.findPsi()
        return if (psi != null && psi !is KtTypeParameter) {
            when (psi) {
                is KtClassOrObject -> KSClassDeclarationImpl.getCached(psi)
                is PsiClass -> KSClassDeclarationJavaImpl.getCached(psi)
                is KtTypeAlias -> KSTypeAliasImpl.getCached(psi)
                else -> throw IllegalStateException()
            }
        } else {
            when (descriptor) {
                is ClassDescriptor -> KSClassDeclarationDescriptorImpl.getCached(descriptor)
                is TypeParameterDescriptor -> KSTypeParameterDescriptorImpl.getCached(descriptor)
                else -> throw IllegalStateException()
            }
        }
    }

    // Finds closest non-local scope.
    fun KtElement.findLexicalScope(): LexicalScope {
        return containingNonLocalDeclaration()?.let {
            resolveSession.declarationScopeProvider.getResolutionScopeForDeclaration(it)
        } ?: resolveSession.fileScopeProvider.getFileResolutionScope(this.containingKtFile)
    }

    fun resolveAnnotationEntry(ktAnnotationEntry: KtAnnotationEntry): AnnotationDescriptor? {
        bindingTrace.get(BindingContext.ANNOTATION, ktAnnotationEntry)?.let { return it }
        val containingDeclaration = KtStubbedPsiUtil.getContainingDeclaration(ktAnnotationEntry)
        return if (containingDeclaration?.let { KtPsiUtil.isLocal(containingDeclaration) } == true) {
            resolveContainingFunctionBody(containingDeclaration)
            bindingTrace.get(BindingContext.ANNOTATION, ktAnnotationEntry)
        } else {
            ktAnnotationEntry.findLexicalScope().let { scope ->
                annotationResolver.resolveAnnotationsWithArguments(scope, listOf(ktAnnotationEntry), bindingTrace)
                bindingTrace.get(BindingContext.ANNOTATION, ktAnnotationEntry)
            }
        }
    }

    fun resolveContainingFunctionBody(element: KtElement) {
        var containingFunction = KtStubbedPsiUtil.getContainingDeclaration(element) ?: return
        while (KtPsiUtil.isLocal(containingFunction))
            containingFunction = KtStubbedPsiUtil.getContainingDeclaration(containingFunction)!!
        if (containingFunction !is KtNamedFunction)
            return

        val containingFD = resolveSession.resolveToDescriptor(containingFunction) as FunctionDescriptor
        val dataFlowInfo = DataFlowInfo.EMPTY
        val scope = resolveSession.declarationScopeProvider.getResolutionScopeForDeclaration(containingFunction)
        bodyResolver.resolveFunctionBody(dataFlowInfo, bindingTrace, containingFunction, containingFD, scope)
    }

    override fun getTypeArgument(typeRef: KSTypeReference, variance: Variance): KSTypeArgument {
        return KSTypeArgumentLiteImpl.getCached(typeRef, variance)
    }

    override val builtIns: KSBuiltIns by lazy {
        val builtIns = module.builtIns
        object : KSBuiltIns {
            override val anyType: KSType by lazy { KSTypeImpl.getCached(builtIns.anyType) }
            override val nothingType by lazy { KSTypeImpl.getCached(builtIns.nothingType) }
            override val unitType: KSType by lazy { KSTypeImpl.getCached(builtIns.unitType) }
            override val numberType: KSType by lazy { KSTypeImpl.getCached(builtIns.numberType) }
            override val byteType: KSType by lazy { KSTypeImpl.getCached(builtIns.byteType) }
            override val shortType: KSType by lazy { KSTypeImpl.getCached(builtIns.shortType) }
            override val intType: KSType by lazy { KSTypeImpl.getCached(builtIns.intType) }
            override val longType: KSType by lazy { KSTypeImpl.getCached(builtIns.longType) }
            override val floatType: KSType by lazy { KSTypeImpl.getCached(builtIns.floatType) }
            override val doubleType: KSType by lazy { KSTypeImpl.getCached(builtIns.doubleType) }
            override val charType: KSType by lazy { KSTypeImpl.getCached(builtIns.charType) }
            override val booleanType: KSType by lazy { KSTypeImpl.getCached(builtIns.booleanType) }
            override val stringType: KSType by lazy { KSTypeImpl.getCached(builtIns.stringType) }
            override val iterableType: KSType by lazy { KSTypeImpl.getCached(builtIns.iterableType.replaceArgumentsWithStarProjections()) }
            override val annotationType: KSType by lazy { KSTypeImpl.getCached(builtIns.annotationType) }
            override val arrayType: KSType by lazy { KSTypeImpl.getCached(builtIns.array.defaultType.replaceArgumentsWithStarProjections()) }
        }
    }
}

open class BaseVisitor : KSVisitorVoid() {
    override fun visitClassDeclaration(type: KSClassDeclaration, data: Unit) {
        for (declaration in type.declarations) {
            declaration.accept(this, Unit)
        }
    }

    override fun visitFile(file: KSFile, data: Unit) {
        for (declaration in file.declarations) {
            declaration.accept(this, Unit)
        }
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        for (declaration in function.declarations) {
            declaration.accept(this, Unit)
        }
    }
}

// TODO: cross module resolution
fun DeclarationDescriptor.findExpectsInKSDeclaration(): List<KSDeclaration> =
    findExpects().map {
        it.toKSDeclaration()
    }

// TODO: cross module resolution
fun DeclarationDescriptor.findActualsInKSDeclaration(): List<KSDeclaration> =
    findActuals().map {
        it.toKSDeclaration()
    }

fun MemberDescriptor.toKSDeclaration(): KSDeclaration =
    when (val psi = findPsi()) {
        is KtClassOrObject -> KSClassDeclarationImpl.getCached(psi)
        is KtFunction -> KSFunctionDeclarationImpl.getCached(psi)
        is KtProperty -> KSPropertyDeclarationImpl.getCached((psi))
        is KtTypeAlias -> KSTypeAliasImpl.getCached(psi)
        else -> when (this) {
            is ClassDescriptor -> KSClassDeclarationDescriptorImpl.getCached(this)
            is FunctionDescriptor -> KSFunctionDeclarationDescriptorImpl.getCached(this)
            is PropertyDescriptor -> KSPropertyDeclarationDescriptorImpl.getCached(this)
            else -> throw IllegalStateException("Unknown expect/actual implementation")
        }
    }
