/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrVariable

open class VariablePredicate(private val printName: String = "Variable") : AbstractPredicate() {
    protected var typePredicate: TypePredicate? = null
    var type: TypePredicate?
        get() = typePredicate
        set(value) {
            typePredicate = value
        }

    private var _isVar: Boolean? = null
    var isVar: Boolean?
        get() = _isVar
        set(value) {
            _isVal = null
            _isVar = value
        }

    private var _isVal: Boolean? = null
    var isVal: Boolean?
        get() = _isVal
        set(value) {
            _isVar = null
            _isVal = value
        }

    var isConst: Boolean? = null
    var isLateinit: Boolean? = null

    override val visitor: Visitor
        get() = MyVisitor()

    override fun toString(): String = buildString {
        append("$printName predicate")
        if (isVal != null) {
            appendDelimeter()
            append("val")
        }
        if (isVar != null) {
            appendDelimeter()
            append("var")
        }
        if (isConst != null) {
            appendDelimeter()
            if (isConst!!) {
                append("const")
            } else {
                append("not cost")
            }
        }
        if (isLateinit != null) {
            appendDelimeter()
            if (isLateinit!!) {
                append("lateinit")
            } else {
                append("not lateinit")
            }
        }
    }

    inner class MyVisitor : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData =
            falseVisitorData()

        override fun visitVariable(declaration: IrVariable, data: Unit): VisitorData {
            if (isVar != null && declaration.isVar != isVar ||
                isVal != null && declaration.isVar == isVal ||
                isConst != null && declaration.isConst != isConst ||
                isLateinit != null && declaration.isLateinit != isLateinit
            ) {
                return falseVisitorData()
            }

            val matches: VisitorDataMap = mutableMapOf()
            if (typePredicate != null) {
                val result = typePredicate!!.checkType(declaration.type, declaration)
                matches[typePredicate!!] = mutableListOf(result)
            }

            val result = matchedPredicatesToVisitorData(declaration, matches)
            if (result.matched) {
                info()
            }
            return result
        }
    }
}