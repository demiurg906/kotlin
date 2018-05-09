/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

abstract class ScopePredicate : AbstractPredicate() {
    protected val innerPredicates = mutableListOf<AbstractPredicate>()
    protected val everywherePredicates = mutableListOf<CodeBlockPredicate>()

    fun classDefinition(init: ClassPredicate.() -> Unit): ClassPredicate {
        val predicate = ClassPredicate()
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    fun objectDefinition(init: ObjectPredicate.() -> Unit): ObjectPredicate {
        val predicate = ObjectPredicate()
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    fun interfaceDefinition(init: InterfacePredicate.() -> Unit): InterfacePredicate {
        val predicate = InterfacePredicate()
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    fun annotationDefinition(init: AnnotationPredicate.() -> Unit): AnnotationPredicate {
        val predicate = AnnotationPredicate()
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    fun enumDefinition(init: EnumPredicate.() -> Unit): EnumPredicate {
        val predicate = EnumPredicate()
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    fun function(init: FunctionPredicate.() -> Unit): FunctionPredicate {
        val predicate = FunctionPredicate()
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    fun variableDefinition(init: VariablePredicate.() -> Unit): VariablePredicate {
        val predicate = VariablePredicate()
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    protected fun recursiveVisit(data: VisitorData, element: IrElement): VisitorData {
        if (everywherePredicates.isEmpty()) {
            return data
        }
        val recursiveVisitor = RecursiveVisitor()
        val dataList = mutableListOf<VisitorData>()
        for (predicate in everywherePredicates) {
            element.acceptChildren(recursiveVisitor, predicate to dataList)
        }
        val res = data.first || dataList.filter { it.first }.any()
        return res to Unit
    }

    protected inner class RecursiveVisitor : IrElementVisitor<Unit, Pair<CodeBlockPredicate, MutableList<VisitorData>>> {
        override fun visitElement(element: IrElement, data: Pair<CodeBlockPredicate, MutableList<VisitorData>>) {
            val (predicate, visitorDataList) = data
            val res = predicate.checkIrNode(element)
            if (res.first) {
                visitorDataList.add(res)
            }
            element.acceptChildren(this, data)
        }
    }
}