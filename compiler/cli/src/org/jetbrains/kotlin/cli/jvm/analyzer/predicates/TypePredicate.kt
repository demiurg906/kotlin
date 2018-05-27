/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.WrappedType

class TypePredicate(
    private val typeName: String? = null,
    private val classPredicate: ClassPredicate? = null
) : Predicate {
    companion object {
        val Int = TypePredicate(typeName = "Int")
        val Double = TypePredicate(typeName = "Double")
        val Boolean = TypePredicate(typeName = "Boolean")
        val String = TypePredicate(typeName = "String")
    }

    override var printResult: Boolean = false
    override var label: String? = null

    fun checkType(type: KotlinType, element: IrElement): VisitorData {
        var matched = true
        val unwrappedType = if (type is WrappedType) {
            type.unwrap()
        } else {
            type
        }
        if (typeName != null || classPredicate != null) {
            matched = false
            val matchTypeName = typeName != null
            if (typeName != null) {
                val typeName = this.typeName
                matched = unwrappedType.toString() == typeName
            }
            if (classPredicate != null) {
                if (unwrappedType is SimpleType) {
                    val typeName = unwrappedType.constructor.declarationDescriptor?.name?.asString()
                    if (typeName != null) {
                        val classNames =
                            classPredicate.allMatchedElements().filter { it is IrClass }.map { (it as IrClass).name.asString() }
                        val result = classNames.any { it == typeName }
                        if (matchTypeName) {
                            matched = matched && result
                        } else {
                            matched = result
                        }
                    } else {
                        matched = false
                    }
                }
            }
        }
        return if (matched) {
            VisitorData(this, element, mutableMapOf())
        } else {
            falseVisitorData()
        }
    }

    override fun toString(): String = buildString {
        appendLabel()
        append("Type predicate")
        if (typeName != null) {
            appendDelimiter()
            append("Type name: $typeName")
        }
        if (classPredicate != null) {
            appendDelimiter()
            append("Class: $classPredicate")
        }
    }
}

/*
    TODO: как достать psi из Ir:
    у каждого элемента есть offset
    roadmap:
    из Ir достать IrFile
    из IrFile достать KtFile с помощью PsiSourceManager, который лежит в GeneratorContext
    в итоге получится PsiFile, из которого по offset находится элемент (надо будет
    походить по PSI дереву), после чего можно будет проверить имя на short
 */