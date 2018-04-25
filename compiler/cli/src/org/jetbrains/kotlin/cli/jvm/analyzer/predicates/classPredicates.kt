/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer.predicates

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.utils.keysToMap

open class ClassPredicate(val classKind: ClassKind = ClassKind.CLASS, val isCompanion: Boolean = false) : ScopePredicate() {
    override val visitor: Visitor
        get() = MyVisitor()

    var modality: Modality? = null
    var name: String? = null
    private val superClassPredicates = mutableSetOf<ClassPredicate>()

    fun superClass(classKind: ClassKind, init: ClassPredicate.() -> Unit) {
        val predicate = when (classKind) {
            ClassKind.CLASS -> ClassPredicate()
            ClassKind.ANNOTATION_CLASS -> AnnotationPredicate()
            ClassKind.INTERFACE -> InterfacePredicate()
            ClassKind.OBJECT -> ObjectPredicate()
            // TODO: проверить, что от enum нельзя наследоваться
            else -> throw IllegalArgumentException("no deriving from enum")
        }
        predicate.init()
        superClass(predicate)
    }

    fun superClass(predicate: ClassPredicate) {
        superClassPredicates.add(predicate)
    }

    fun initializer(init: CodeBlockPredicate.() -> Unit): CodeBlockPredicate {
        val predicate = CodeBlockPredicate()
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    fun constructor(init: ConstructorPredicate.() -> Unit): ConstructorPredicate {
        val predicate = ConstructorPredicate()
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    fun propertyDefinition(init: PropertyPredicate.() -> Unit): PropertyPredicate {
        val predicate = PropertyPredicate()
        predicate.init()
        innerPredicates += predicate
        return predicate
    }

    fun companionObject(init: CompanionObjectPredicate.() -> Unit) {
        val predicate = CompanionObjectPredicate()
        predicate.init()
        companionObject(predicate)
    }

    fun companionObject(predicate: CompanionObjectPredicate) {
        innerPredicates += predicate
    }

    fun everywhere(init: CodeBlockPredicate.() -> Unit) {
        val predicate = CodeBlockPredicate()
        predicate.init()
        everywherePredicates += predicate
    }

    inner class MyVisitor : Visitor {
        private val recursiveVisitor = RecursiveVisitor(this)

        private fun recursiveVisit(data: VisitorData, element: IrElement) = recursiveVisit(recursiveVisitor, data, element)

        override fun visitElement(element: IrElement, data: Unit): VisitorData =
            recursiveVisit(falseVisitorData(), element)

        override fun visitClass(declaration: IrClass, data: Unit): VisitorData {
            if (
                declaration.kind != classKind ||
                name != null && declaration.name.asString() != name!! ||
                modality != null && declaration.modality != modality!!
            ) {
                return recursiveVisit(falseVisitorData(), declaration)
            }

            // TODO: fix
//            val superClasses = declaration.superClasses.flatMap {
//                val supers = mutableListOf(it.owner)
//                supers.add(it.owner)
//                supers
//            }.distinct()

            val superClasses = allSuperClasses(declaration)

            for (predicate in superClassPredicates) {
                // TODO: fix to for loop for collecting data
                val results = superClasses.map(predicate::checkIrNode)
                if (!results.filter { it.first }.any()) {
                    return falseVisitorData()
                }
            }
            val matches = mutableMapOf<AbstractPredicate, Boolean>()
            matches.putAll(innerPredicates.keysToMap { false })
            for (predicate in innerPredicates) {
                for (statement in declaration.declarations) {
                    val (result, map) = predicate.checkIrNode(statement)
                    if (result) {
                        matches[predicate] = true
                    }
                }
            }
            info()
            return recursiveVisit(
                if (matches.values.all { it }) {
                    true to Unit
                } else {
                    falseVisitorData()
                }, declaration
            )
        }

        private fun allSuperClasses(declaration: IrClass): Set<IrClass> {
            val res = mutableSetOf<IrClass>()
            res.addAll(declaration.superClasses.map { it.owner })
            res.addAll(declaration.superClasses.flatMap { allSuperClasses(it.owner) })
            return res
        }
    }
}

class ConstructorPredicate : FunctionPredicate() {
    // TODO: подумать, можно ли заблокировать некоторые поля из FunctionPredicate
    // например, name, returnType

    override val visitor: Visitor
        get() = MyCVisitor()

    inner class MyCVisitor : Visitor {
        private val functionVisitor = MyVisitor()

        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitConstructor(declaration: IrConstructor, data: Unit): VisitorData =
            functionVisitor.visitFunction(declaration, data)
    }
}

open class ObjectPredicate(isCompanion: Boolean = false) : ClassPredicate(ClassKind.OBJECT, isCompanion)

class CompanionObjectPredicate : ObjectPredicate(isCompanion = true)

class InterfacePredicate : ClassPredicate(ClassKind.INTERFACE)

class AnnotationPredicate : ClassPredicate(ClassKind.ANNOTATION_CLASS)

class EnumPredicate : ClassPredicate(ClassKind.ENUM_CLASS)

class PropertyPredicate : VariablePredicate() {
    override val visitor: Visitor
        get() = MyVisitor()

    private var getterPredicate: FunctionPredicate? = null
    private var setterPredicate: FunctionPredicate? = null

    fun getter(init: FunctionPredicate.() -> Unit) {
        val predicate = FunctionPredicate()
        predicate.init()
        getterPredicate = predicate
    }

    fun setter(init: FunctionPredicate.() -> Unit) {
        val predicate = FunctionPredicate()
        predicate.init()
        setterPredicate = predicate
    }

    inner class MyVisitor : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitProperty(declaration: IrProperty, data: Unit): VisitorData {
            if (isVar != null && declaration.isVar != isVar ||
                isVal != null && declaration.isVar == isVal ||
                isConst != null && declaration.isConst != isConst ||
                isLateinit != null && declaration.isLateinit != isLateinit ||
                typePredicate != null && !typePredicate!!.checkType(declaration.type)
            ) {
                return falseVisitorData()
            }

            if (getterPredicate != null) {
                if (declaration.getter != null) {
                    val (res, map) = getterPredicate!!.checkIrNode(declaration.getter!!)
                    if (!res) {
                        return falseVisitorData()
                    }
                } else {
                    return falseVisitorData()
                }
            }

            if (setterPredicate != null) {
                if (declaration.setter != null) {
                    val (res, map) = setterPredicate!!.checkIrNode(declaration.setter!!)
                    if (!res) {
                        return falseVisitorData()
                    }
                } else {
                    return falseVisitorData()
                }
            }

            info()
            var s = "variable ${declaration.name}"
            if (message != null) {
                s += ". message: $message"
            }
            println(s)
            return true to Unit
        }
    }
}
