/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

typealias VisitorData = Pair<Boolean, Unit>
typealias Visitor = IrElementVisitor<VisitorData, Unit>

fun falseVisitorData() = false to Unit

abstract class AbstractPredicate {
    abstract val visitor: Visitor
    private val cachedResults = mutableMapOf<IrElement, VisitorData>()
    var info: () -> Unit = {}

    open fun checkIrNode(element: IrElement): VisitorData {
        if (element in cachedResults) {
            return cachedResults[element]!!
        }
        val result = element.accept(visitor, Unit)
        cachedResults[element] = result
        return result
    }
}

/*
    TODO:
    DataHolder = Map<>? / emptyMap
    change recursiveSearch to everywhere {...} *minor
    сделать свой класс для типов, сравнивать типы по fqn
*/
