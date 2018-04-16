/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.expressions.*

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
            var thenResult = true
            var elseResult = true
            if (thenPredicate != null) {
                val (result, map) = thenPredicate!!.checkIrNode(expression.branches[0])
                thenResult = result
            }
            if (elsePredicate != null) {
                val (result, map) = elsePredicate!!.checkIrNode(expression.branches[1])
                elseResult = result
            }
            val result = thenResult && elseResult
            if (result) {
                info()
            }
            return result to Unit
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
                    info()
                    return body!!.checkIrNode(loopBody.statements[1])
                }
            }
            info()
            return true to Unit
        }
    }

    fun body(init: CodeBlockPredicate.() -> Unit): CodeBlockPredicate {
        body = CodeBlockPredicate()
        body?.init()
        return body!!
    }
}

class ForLoopPredicate : LoopPredicate()

class WhileLoopPredicate : LoopPredicate()

class FunctionCallPredicate(val functionPredicate: FunctionPredicate) : AbstractPredicate() {
    override val visitor: Visitor
        get() = MyVisitor()

    inner class MyVisitor : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitCall(expression: IrCall, data: Unit): VisitorData {
            val calledFunction = expression.symbol.owner
            return functionPredicate.checkIrNode(calledFunction)
        }
    }
}
