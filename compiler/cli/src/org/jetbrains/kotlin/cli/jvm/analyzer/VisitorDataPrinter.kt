/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer

import org.jetbrains.kotlin.cli.jvm.analyzer.predicates.VisitorData
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

typealias StringVisitorDataMap = Map<String, List<StringVisitorData>>

data class StringVisitorData(
    val predicate: String,
    val element: String,
    val innerPredicates: StringVisitorDataMap
)

object IrToStringTransformer {
    private val visitor = IrToStringVisitor()

    fun transformIrElementsToString(data: VisitorData): StringVisitorData {
        val predicate = data.predicate.toString()
        val element: String = if (data.element != null) {
            data.element.accept(visitor, Unit)
        } else {
            "null"
        }
        val innerPredicates = data.innerPredicatesMatches.entries.map { (predicate, data) ->
            predicate.toString() to data.map { transformIrElementsToString(it)}
        }.toMap()
        return StringVisitorData(predicate, element, innerPredicates)
    }

    private class IrToStringVisitor : IrElementVisitor<String, Unit> {
        override fun visitElement(element: IrElement, data: Unit): String {
            if (element is IrSymbolOwner) {
                return element.symbol.descriptor.toString()
            } else {
                return "Not implemented: $element"
            }
        }
    }
}
