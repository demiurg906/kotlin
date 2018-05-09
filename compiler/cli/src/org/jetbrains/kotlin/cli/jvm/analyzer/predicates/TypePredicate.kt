/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.types.DeferredType
import org.jetbrains.kotlin.types.KotlinType

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

    fun checkType(type: KotlinType, element: IrElement): VisitorData {
        var matched = true
        if (typeName != null || classPredicate != null) {
            matched = false
            val matchTypeName = typeName != null
            if (typeName != null) {
                val typeName = this.typeName
                matched = type.toString() == typeName
            }
            if (classPredicate != null) {
                if (type is DeferredType) {
                    val typeName = type.delegate.constructor.declarationDescriptor?.name?.asString()
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
//        val declarationType = type.toString().split(" ").getOrNull(2) ?: return false
//        val declarationType = type.toString().split(" ").getOrNull(2) ?: return false
//        return typeName == declarationType

        // type.value.constructor.this$0
        // irClass.symbol.descriptor
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