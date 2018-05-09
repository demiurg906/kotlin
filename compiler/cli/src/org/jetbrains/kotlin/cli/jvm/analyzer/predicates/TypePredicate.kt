/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.types.DeferredType
import org.jetbrains.kotlin.types.KotlinType

class TypePredicate(
    private val typeName: String? = null,
    private val classPredicate: ClassPredicate? = null
) {
    companion object {
        val Int = TypePredicate(typeName = "Int")
        val Double = TypePredicate(typeName = "Double")
        val Boolean = TypePredicate(typeName = "Boolean")
        val String = TypePredicate(typeName = "String")
    }

    fun checkType(type: KotlinType): Boolean {
        if (typeName != null) {
            val typeName = this.typeName
            return type.toString() == typeName
        }
        if (classPredicate != null) {
            if (type is DeferredType) {
                val typeName = type.delegate.constructor.declarationDescriptor?.name?.asString() ?: return false
                val classNames = classPredicate.allMatchedElements().filter { it is IrClass }.map { (it as IrClass).name.asString() }
                return classNames.any { it == typeName }
            }
        }

        return true
//        val declarationType = type.toString().split(" ").getOrNull(2) ?: return false
//        val declarationType = type.toString().split(" ").getOrNull(2) ?: return false
//        return typeName == declarationType

        // type.value.constructor.this$0
        // irClass.symbol.descriptor
    }
}

/*
    как достать psi из Ir:
    у каждого элемента есть offset
    roadmap:
    из Ir достать IrFile
    из IrFile достать KtFile с помощью PsiSourceManager, который лежит в GeneratorContext
    в итоге получится PsiFile, из которого по offset находится элемент (надо будет
    походить по PSI дереву), после чего можно будет проверить имя на short
 */