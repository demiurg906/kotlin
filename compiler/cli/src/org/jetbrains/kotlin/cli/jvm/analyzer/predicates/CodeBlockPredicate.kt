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

class CodeBlockPredicate : ScopePredicate() {
    override val visitor: Visitor
        get() = MyVisitor()

    inner class MyVisitor : Visitor {
        private val recursiveVisitor = RecursiveVisitor(this)

        private fun recursiveVisit(data: VisitorData, element: IrElement) = recursiveVisit(recursiveVisitor, data, element)

        override fun visitElement(element: IrElement, data: Unit): VisitorData =
            recursiveVisit(falseVisitorData(), element)

        override fun visitBlockBody(body: IrBlockBody, data: Unit): VisitorData =
            recursiveVisit(visitSmthWithStatements(body, data), body)


        override fun visitBlock(expression: IrBlock, data: Unit): VisitorData =
            recursiveVisit(visitSmthWithStatements(expression, data), expression)

        private fun visitSmthWithStatements(body: IrStatementContainer, data: Unit): VisitorData {
            val matches = mutableMapOf<AbstractPredicate, Boolean>()
            matches.putAll(innerPredicates.keysToMap { false })
            for (predicate in innerPredicates) {
                for (statement in body.statements) {
                    val (result, map) = predicate.checkIrNode(statement)
                    if (result) {
                        matches[predicate] = true
                    }
                }
            }
            info()
            return if (matches.values.all{ it }) {
                true to Unit
            } else {
                falseVisitorData()
            }
        }
    }

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
        val predicate = CodeBlockPredicate()
        predicate.init()
        everywherePredicates += predicate
    }
}