/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrVariable

open class VariablePredicate : AbstractPredicate() {
    protected var typePredicate: TypePredicate? = null
    var type: TypePredicate?
        get() = typePredicate
        set(value) {
            typePredicate = value
        }

    var message: String? = null

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

    inner class MyVisitor : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData =
            falseVisitorData()

        override fun visitVariable(declaration: IrVariable, data: Unit): VisitorData {
            if (isVar != null && declaration.isVar != isVar ||
                isVal != null && declaration.isVar == isVal ||
                isConst != null && declaration.isConst != isConst ||
                isLateinit != null && declaration.isLateinit != isLateinit ||
                typePredicate != null && !typePredicate!!.checkType(declaration.type)
            ) {
                return falseVisitorData()
            }

            info()
            var s = "variable ${declaration.name}"
            if (message != null) {
                s += ". message: $message"
            }
            println(s)
            return true to Unit
        }
    }
}