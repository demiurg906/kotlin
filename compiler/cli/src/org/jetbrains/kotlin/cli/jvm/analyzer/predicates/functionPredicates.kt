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
import org.jetbrains.kotlin.utils.keysToMap

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

    override fun toString(): String = buildString {
        append("Function declaration predicate")
        if (name != null) {
            appendDelimeter()
            append("Name: $name")
        }
        if (numberOfArguments != null) {
            appendDelimeter()
            append("Number of arguments: $numberOfArguments")
        }
        if (visibility != null) {
            appendDelimeter()
            append("Visibility: $visibility")
        }
        if (isInline != null) {
            appendDelimeter()
            if (isInline!!) {
                append("inline")
            } else {
                append("not inline")
            }
        }
    }

    open inner class MyVisitor(protected val finalClass: Boolean = true) : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitFunction(declaration: IrFunction, data: Unit): VisitorData {
            // definition
            if (numberOfArguments != null && declaration.valueParameters.size != numberOfArguments ||
                visibility != null && Visibilities.compare(declaration.visibility, visibility!!) != 0 ||
                isInline != null && declaration.isInline != isInline
            ) {
                return falseVisitorData()
            }

            val checkedArguments = mutableMapOf<IrValueParameter, Boolean>()
            val matches: VisitorDataMap = mutableMapOf()
            if (returnTypePredicate != null) {
                val result = returnTypePredicate!!.checkType(declaration.returnType, declaration)
                matches[returnTypePredicate!!] = mutableListOf(result)
            }

            matches.putAll(parameterPredicates.keysToMap { mutableListOf<VisitorData>() })
            for (parameterPredicate in parameterPredicates) {
                var found = false
                for (parameter in declaration.valueParameters) {
                    if (checkedArguments.getOrDefault(parameter, false)) {
                        continue
                    }
                    val result = parameterPredicate.checkIrNode(parameter)
                    if (result.matched) {
                        matches[parameterPredicate]!!.add(result)
                        checkedArguments[parameter] = true
                        found = true
                        break
                    }
                }
                if (!found) {
                    return falseVisitorData()
                }
            }
            val result = matchedPredicatesToVisitorData(declaration, matches)
            if (result.matched) {
                if (finalClass) {
                    info()
                }
                return result
            } else {
                return falseVisitorData()
            }
        }

        override fun visitSimpleFunction(declaration: IrSimpleFunction, data: Unit): VisitorData {
            if (name != null && declaration.name.identifier != name) {
                return falseVisitorData()
            }
            return visitFunction(declaration, data)
        }
    }
}

open class FunctionPredicate(private val printName: String = "Function") : FunctionDeclarationPredicate() {
    private var bodyPredicate: CodeBlockPredicate? = null

    override val visitor: Visitor
        get() = MyVisitor()

    fun body(init: CodeBlockPredicate.() -> Unit): CodeBlockPredicate {
        bodyPredicate = CodeBlockPredicate()
        bodyPredicate?.init()
        return bodyPredicate!!
    }

    override fun toString(): String = buildString {
        append("$printName predicate")
    }

    open inner class MyVisitor : FunctionDeclarationPredicate.MyVisitor(false) {
        override fun visitFunction(declaration: IrFunction, data: Unit): VisitorData {
            val declarationResult = super.visitFunction(declaration, data)
            if (!declarationResult.matched) {
                return falseVisitorData()
            }

            val matches: VisitorDataMap = mutableMapOf(this@FunctionPredicate to mutableListOf(declarationResult))
            if (bodyPredicate != null && declaration.body != null) {
                val bodyResult = bodyPredicate?.checkIrNode(declaration.body!!)!!
                matches[bodyPredicate!!] = mutableListOf(bodyResult)
            }

            val result = matchedPredicatesToVisitorData(declaration, matches)
            if (result.matched) {
                info()
            }
            return result
        }
    }
}