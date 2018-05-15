/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

typealias VisitorDataMap = MutableMap<Predicate, MutableList<VisitorData>>

data class VisitorData(
    val predicate: Predicate,
    val element: IrElement?,
    val innerPredicatesMatches: VisitorDataMap = mutableMapOf()
) {
    val matched: Boolean
        get() = element != null
}
typealias Visitor = IrElementVisitor<VisitorData, Unit>

interface Predicate {
    var printResult: Boolean

    fun falseVisitorData() = VisitorData(this, null, mutableMapOf())

    fun matchedPredicatesToVisitorData(element: IrElement, matches: VisitorDataMap) =
        if (matches.values.all { it.isNotEmpty() }) {
            VisitorData(this, element, matches)
        } else {
            falseVisitorData()
        }

    fun StringBuilder.appendDelimiter(): StringBuilder = append(", ")
}

abstract class AbstractPredicate : Predicate {
    abstract val visitor: Visitor
    private val cachedResults = mutableMapOf<IrElement, VisitorData>()

    var info: () -> Unit = {}
    override var printResult: Boolean = false

    open fun checkIrNode(element: IrElement): VisitorData {
        if (element in cachedResults) {
            return cachedResults[element]!!
        }
        val result = element.accept(visitor, Unit)
        cachedResults[element] = result
        return result
    }

    fun allMatchedElements(): List<IrElement> = cachedResults.entries.filter { it.value.matched }.map { it.key }
}
