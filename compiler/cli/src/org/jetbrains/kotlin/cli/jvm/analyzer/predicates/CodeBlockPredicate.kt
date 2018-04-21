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
        override fun visitElement(element: IrElement, data: Unit): VisitorData =
            falseVisitorData()

        override fun visitBlockBody(body: IrBlockBody, data: Unit): VisitorData {
            return visitSmthWithStatements(body, data)
        }

        override fun visitBlock(expression: IrBlock, data: Unit): VisitorData {
            return visitSmthWithStatements(expression, data)
        }

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
            if (matches.values.all{ it }) {
                return true to Unit
            } else {
                return falseVisitorData()
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

}