/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallWithIndexedArgumentsBase
import org.jetbrains.kotlin.ir.util.getArguments
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

class IfPredicate : AbstractPredicate() {
    private var thenPredicate: CodeBlockPredicate? = null
    private var elsePredicate: CodeBlockPredicate? = null

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

    override val visitor: Visitor
        get() = MyVisitor()

    override fun toString(): String = buildString {
        append("If predicate")
    }

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
}

abstract class LoopPredicate(private val printName: String) : AbstractPredicate() {
    var body: CodeBlockPredicate? = null

    fun body(init: CodeBlockPredicate.() -> Unit): CodeBlockPredicate {
        body = CodeBlockPredicate()
        body?.init()
        return body!!
    }

    override fun toString(): String = buildString {
        append("$printName predicate")
    }
}

class ForLoopPredicate : LoopPredicate("For loop") {
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

class WhileLoopPredicate : LoopPredicate("While loop") {
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

    private val argumentPredicates = mutableMapOf<String, Any>()

    override fun toString(): String = buildString {
        append("Function call predicate")
    }

    fun argument(name: String, value: Any) {
        argumentPredicates[name] = value
    }

    inner class MyVisitor : Visitor {
        private val recursiveCallVisitor = RecursiveCallVisitor()

        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitVariable(declaration: IrVariable, data: Unit): VisitorData {
            val initializer = declaration.initializer ?: return falseVisitorData()
            return checkIrNode(initializer)
        }

        override fun visitSetVariable(expression: IrSetVariable, data: Unit): VisitorData {
            return checkIrNode(expression.value)
        }

        override fun visitSetField(expression: IrSetField, data: Unit): VisitorData {
            return checkIrNode(expression.value)
        }

        inner class CallVisitor : IrElementVisitor<VisitorData?, Unit> {
            override fun visitElement(element: IrElement, data: Unit): VisitorData? = null

            override fun visitCall(expression: IrCall, data: Unit): VisitorData? {
                val calledFunction = expression.symbol.owner as IrFunction
                val result = functionPredicate.checkIrNode(calledFunction)
                if (result.matched) {
                    val argsMap: MutableMap<String, IrElement?> = argumentPredicates.keys.map { it to null }.toMap().toMutableMap()
                    for ((arg, value) in argumentPredicates) {
                        for ((descriptor, argument) in expression.getArguments()) {
                            if (descriptor.name.asString() == arg && argument is IrConst<*> && argument.value == value) {
                                argsMap[arg] = argument
                            }
                        }
                    }
                    if (argsMap.values.all { it != null }) {
                        return result
                    }
                }
                return null
            }
        }

        inner class RecursiveCallVisitor : IrElementVisitor<List<VisitorData>, Unit> {
            private val callVisitor = CallVisitor()

            override fun visitElement(element: IrElement, data: Unit): List<VisitorData> = listOf()

            override fun visitCall(expression: IrCall, data: Unit): List<VisitorData> {
                val results = mutableListOf(expression.accept(callVisitor, Unit))
                for ((_, argument) in expression.getArguments()) {
                    results.addAll(argument.accept(this, Unit))
                }
                return results.filterNotNull()
            }
        }

        override fun visitCall(expression: IrCall, data: Unit): VisitorData {
            val results = expression.accept(recursiveCallVisitor, Unit)
            if (results.isNotEmpty()) {
                info()
                return VisitorData(
                    this@FunctionCallPredicate,
                    expression,
                    mutableMapOf(this@FunctionCallPredicate to results.toMutableList())
                )
            } else {
                return falseVisitorData()
            }
        }
    }
}
