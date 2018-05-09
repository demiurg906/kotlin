/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallWithIndexedArgumentsBase

class IfPredicate : AbstractPredicate() {
    private var thenPredicate: CodeBlockPredicate? = null
    private var elsePredicate: CodeBlockPredicate? = null

    override val visitor: Visitor
        get() = MyVisitor()

    inner class MyVisitor : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitWhen(expression: IrWhen, data: Unit): VisitorData {
            if (expression.branches.size < 2 && elsePredicate != null) {
                return falseVisitorData()
            }
            val matches: VisitorDataMap = mutableMapOf()

            if (thenPredicate != null) {
                val result = thenPredicate!!.checkIrNode(expression.branches[0].result)
                matches[thenPredicate!!] = mutableListOf(result)
            }
            if (elsePredicate != null) {
                val result = elsePredicate!!.checkIrNode(expression.branches[0].result)
                matches[elsePredicate!!] = mutableListOf(result)
            }

            val result = matchedPredicatesToVisitorData(expression, matches)
            if (result.matched) {
                info()
            }
            return result
        }
    }

    fun thenBranch(init: CodeBlockPredicate.() -> Unit): CodeBlockPredicate {
        val predicate = CodeBlockPredicate()
        predicate.init()
        thenPredicate = predicate
        return predicate
    }

    fun elseBranch(init: CodeBlockPredicate.() -> Unit): CodeBlockPredicate {
        val predicate = CodeBlockPredicate()
        predicate.init()
        elsePredicate = predicate
        return predicate
    }
}

abstract class LoopPredicate : AbstractPredicate() {
    var body: CodeBlockPredicate? = null

    fun body(init: CodeBlockPredicate.() -> Unit): CodeBlockPredicate {
        body = CodeBlockPredicate()
        body?.init()
        return body!!
    }
}

class ForLoopPredicate : LoopPredicate() {
    override val visitor: Visitor
        get() = MyVisitor()

    inner class MyVisitor : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitBlock(expression: IrBlock, data: Unit): VisitorData {
            if (expression.origin != IrStatementOrigin.FOR_LOOP) {
                return falseVisitorData()
            }
            val whileLoop = expression.statements.firstOrNull { it is IrWhileLoop }
            if (whileLoop != null && body != null) {
                val loopBody = (whileLoop as IrWhileLoop).body ?: return falseVisitorData()
                if (loopBody is IrBlock && loopBody.statements.size >= 2) {
                    val result = body!!.checkIrNode(loopBody.statements[1])
                    return VisitorData(this@ForLoopPredicate, expression, mutableMapOf(body!! to mutableListOf(result)))
                }
            }
            info()
            return VisitorData(this@ForLoopPredicate, expression, mutableMapOf())
        }
    }
}

class WhileLoopPredicate : LoopPredicate() {
    override val visitor: Visitor
        get() = MyVisitor()

    inner class MyVisitor : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitWhileLoop(loop: IrWhileLoop, data: Unit): VisitorData {
            val matches: VisitorDataMap = mutableMapOf()
            if (body != null) {
                val loopBody = loop.body ?: return falseVisitorData()
                val result = body!!.checkIrNode(loopBody)
                matches[body!!] = mutableListOf(result)
            }
            val result = matchedPredicatesToVisitorData(loop, matches)
            if (result.matched) {
                info()
            }
            return result
        }
    }
}

class FunctionCallPredicate(val functionPredicate: FunctionPredicate) : AbstractPredicate() {
    override val visitor: Visitor
        get() = MyVisitor()

    inner class MyVisitor : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitVariable(declaration: IrVariable, data: Unit): VisitorData {
            val initializer = declaration.initializer ?: return falseVisitorData()
            return checkIrNode(initializer)
        }

        override fun visitCall(expression: IrCall, data: Unit): VisitorData {
            val calledFunction = expression.symbol.owner
            val results = mutableListOf(functionPredicate.checkIrNode(calledFunction))

            if (expression is IrCallWithIndexedArgumentsBase) {
                var i = 0
                while (true) {
                    try {
                        val argument = expression.getValueArgument(i) ?: continue
                        results.add(checkIrNode(argument))
                        i += 1
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        break
                    }
                }
            }
            val goodResults = results.filter(VisitorData::matched)
            if (goodResults.isNotEmpty()) {
                info()
                return VisitorData(this@FunctionCallPredicate, expression, mutableMapOf(this@FunctionCallPredicate to goodResults.toMutableList()))
            } else {
                return falseVisitorData()
            }
        }
    }
}
