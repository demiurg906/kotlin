/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.utils.keysToMap

class FilePredicate : ScopePredicate() {
    override val visitor: Visitor
        get() = MyVisitor()

    fun everywhere(init: CodeBlockPredicate.() -> Unit) {
        val predicate = CodeBlockPredicate()
        predicate.init()
        everywherePredicates += predicate
    }

    override fun toString(): String = buildString {
        append("File predicate")
    }

    inner class MyVisitor : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitFile(declaration: IrFile, data: Unit): VisitorData {
            val matches: VisitorDataMap = mutableMapOf()
            matches.putAll(innerPredicates.keysToMap { mutableListOf<VisitorData>() })
            for (predicate in innerPredicates) {
                for (innerDeclaration in declaration.declarations) {
                    val result = predicate.checkIrNode(innerDeclaration)
                    if (result.matched) {
                        matches[predicate]!!.add(result)
                    }
                }
            }
            var result = matchedPredicatesToVisitorData(declaration, matches)
            result = recursiveVisit(result, declaration)
            if (result.matched) {
                info()
            }
            return result
        }
    }
}