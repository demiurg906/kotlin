/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.utils.keysToMap

class CodeBlockPredicate(private val printName: String = "Code block") : ScopePredicate() {
    fun forLoop(init: ForLoopPredicate.() -> Unit): ForLoopPredicate {
        val predicate = ForLoopPredicate()
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    fun whileLoop(init: WhileLoopPredicate.() -> Unit): WhileLoopPredicate {
        val predicate = WhileLoopPredicate()
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    fun ifCondition(init: IfPredicate.() -> Unit): IfPredicate {
        val predicate = IfPredicate()
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    fun functionCall(func: FunctionPredicate, init: FunctionCallPredicate.() -> Unit = {}): FunctionCallPredicate {
        val predicate = FunctionCallPredicate(func)
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    fun everywhere(init: CodeBlockPredicate.() -> Unit) {
        val predicate = CodeBlockPredicate("Everywhere")
        predicate.init()
        everywherePredicates += predicate
    }

    override val visitor: Visitor
        get() = MyVisitor()

    override fun toString(): String = buildString {
        append("$printName predicate")
    }

    inner class MyVisitor : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData =
            falseVisitorData()

        override fun visitBlockBody(body: IrBlockBody, data: Unit): VisitorData =
            visitSmthWithStatements(body, data)

        override fun visitBlock(expression: IrBlock, data: Unit): VisitorData =
            visitSmthWithStatements(expression, data)

        private fun visitSmthWithStatements(body: IrStatementContainer, data: Unit): VisitorData {
            val matches: VisitorDataMap = mutableMapOf()
            matches.putAll(innerPredicates.keysToMap { mutableListOf<VisitorData>() })

            for (predicate in innerPredicates) {
                for (statement in body.statements) {
                    val result = predicate.checkIrNode(statement)
                    if (result.matched) {
                        matches[predicate]!!.add(result)
                    }
                }
            }
            var result = matchedPredicatesToVisitorData(body as IrElement, matches)
            result = recursiveVisit(result, body as IrElement)
            if (result.matched) {
                info()
            }
            return result
        }
    }
}