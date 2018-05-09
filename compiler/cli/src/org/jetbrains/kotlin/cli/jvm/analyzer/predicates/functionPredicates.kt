/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter

open class FunctionDeclarationPredicate: AbstractPredicate() {
    private val parameterPredicates = mutableListOf<ValueParameterPredicate>()

    var name: String? = null
    var numberOfArguments: Int? = null
    var visibility: Visibility? = null
    var isInline: Boolean? = null

    private var returnTypePredicate: TypePredicate? = null
    var returnType: TypePredicate?
        get() = returnTypePredicate
        set(value) {
            returnTypePredicate = value
        }

    fun argument(init: ValueParameterPredicate.() -> Unit): ValueParameterPredicate {
        val predicate = ValueParameterPredicate()
        predicate.init()
        parameterPredicates += predicate
        return predicate
    }

    override val visitor: Visitor
        get() = MyVisitor()

    open inner class MyVisitor(protected val finalClass: Boolean = true) : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitFunction(declaration: IrFunction, data: Unit): VisitorData {
            // definition
            if (numberOfArguments != null && declaration.valueParameters.size != numberOfArguments ||
                visibility != null && Visibilities.compare(declaration.visibility, visibility!!) != 0 ||
                isInline != null && declaration.isInline != isInline ||
                returnTypePredicate != null && !returnTypePredicate!!.checkType(declaration.returnType)
            ) {
                return falseVisitorData()
            }

            val checkedArguments = mutableMapOf<IrValueParameter, Boolean>()
            for (parameterPredicate in parameterPredicates) {
                var found = false
                for (parameter in declaration.valueParameters) {
                    if (checkedArguments.getOrDefault(parameter, false)) {
                        continue
                    }
                    val (res, map) = parameterPredicate.checkIrNode(parameter)
                    if (res) {
                        checkedArguments[parameter] = true
                        found = true
                    }
                }
                if (!found) {
                    return falseVisitorData()
                }
            }
            if (finalClass) {
                info()
            }
            return true to Unit
        }

        override fun visitSimpleFunction(declaration: IrSimpleFunction, data: Unit): VisitorData {
            if (name != null && declaration.name.identifier != name) {
                return falseVisitorData()
            }
            return visitFunction(declaration, data)
        }
    }
}

open class FunctionPredicate : FunctionDeclarationPredicate() {
    private var bodyPredicate: CodeBlockPredicate? = null

    override val visitor: Visitor
        get() = MyVisitor()

    fun body(init: CodeBlockPredicate.() -> Unit): CodeBlockPredicate {
        bodyPredicate = CodeBlockPredicate()
        bodyPredicate?.init()
        return bodyPredicate!!
    }

    inner open class MyVisitor : FunctionDeclarationPredicate.MyVisitor(false) {
        override fun visitFunction(declaration: IrFunction, data: Unit): VisitorData {
            val (res, map) = super.visitFunction(declaration, data)
            if (!res) {
                return false to Unit
            }
            // body
            var result = true
            if (bodyPredicate != null && declaration.body != null) {
                val (res2, map2) = bodyPredicate?.checkIrNode(declaration.body!!)!!
                result = res2
            }
            if (result) {
                info()
                return true to Unit
            }
            return falseVisitorData()
        }
    }
}