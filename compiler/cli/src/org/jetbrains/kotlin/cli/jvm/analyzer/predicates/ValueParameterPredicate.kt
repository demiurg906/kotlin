/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrValueParameter

class ValueParameterPredicate : AbstractPredicate() {
    override val visitor: Visitor
        get() = MyVisitor()

    private var typePredicate: TypePredicate? = null
    var type: TypePredicate?
        get() = typePredicate
        set(value) {
            typePredicate = value
        }

    inner class MyVisitor : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData =
            falseVisitorData()

        override fun visitValueParameter(declaration: IrValueParameter, data: Unit): VisitorData {
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