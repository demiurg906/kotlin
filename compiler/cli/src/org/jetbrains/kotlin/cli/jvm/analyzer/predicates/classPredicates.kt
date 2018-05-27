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
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.utils.keysToMap

open class ClassPredicate(
    val classKind: ClassKind = ClassKind.CLASS,
    private val isCompanion: Boolean = false
) : ScopePredicate() {
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
        val predicate = CodeBlockPredicate("Everywhere")
        predicate.init()
        everywherePredicates += predicate
    }

    override fun toString(): String = buildString {
        appendLabel()
        when (classKind) {
            ClassKind.CLASS -> append("Class")
            ClassKind.INTERFACE -> append("Interface")
            ClassKind.ENUM_CLASS -> append("Enum class")
            ClassKind.ENUM_ENTRY -> append("Enum entry")
            ClassKind.ANNOTATION_CLASS -> append("Annotation class")
            ClassKind.OBJECT -> if (isCompanion) {
                append("Companion object")
            } else {
                append("Object")
            }
        }
        append(" predicate")
        if (name != null) {
            appendDelimiter()
            append("name: $name")
        }
        if (modality != null) {
            appendDelimiter()
            append("modality: $modality")
        }
    }

    inner class MyVisitor : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitClass(declaration: IrClass, data: Unit): VisitorData {
            if (
                declaration.kind != classKind ||
                name != null && declaration.name.asString() != name!! ||
                modality != null && declaration.modality != modality!!
            ) {
                return falseVisitorData()
            }

            // TODO: fix
//            val superClasses = declaration.superClasses.flatMap {
//                val supers = mutableListOf(it.owner)
//                supers.add(it.owner)
//                supers
//            }.distinct()

            val matches: VisitorDataMap = mutableMapOf()
            matches.putAll(superClassPredicates.keysToMap { mutableListOf<VisitorData>() })
            val superClasses = allSuperClasses(declaration)

            for (predicate in superClassPredicates) {
                // TODO: fix to for loop for collecting data
                val results = superClasses.map(predicate::checkIrNode).filter(VisitorData::matched)
                matches[predicate]!!.addAll(results)
            }

            matches.putAll(innerPredicates.keysToMap { mutableListOf<VisitorData>() })
            for (predicate in innerPredicates) {
                for (statement in declaration.declarations) {
                    val result = predicate.checkIrNode(statement)
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

        private fun allSuperClasses(declaration: IrClass): Set<IrClass> {
            val res = mutableSetOf<IrClass>()
            res.addAll(declaration.superClasses.map { it.owner })
            res.addAll(declaration.superClasses.flatMap { allSuperClasses(it.owner) })
            return res
        }
    }
}

class ConstructorPredicate : FunctionPredicate("Constructor") {
    // TODO: подумать, можно ли заблокировать некоторые поля из FunctionPredicate
    // например, name, returnType
    // TODO: отнаследовать FunctionPredicate и ConstructorPredicate от какого-нибудь CallablePredicate

    override val visitor: Visitor
        get() = MyVisitor()

    inner class MyVisitor : FunctionPredicate.MyVisitor() {
        private val functionVisitor = MyVisitor()

        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitFunction(declaration: IrFunction, data: Unit): VisitorData = falseVisitorData()

        override fun visitConstructor(declaration: IrConstructor, data: Unit): VisitorData =
            functionVisitor.visitFunction(declaration, data)
    }
}

open class ObjectPredicate(isCompanion: Boolean = false) : ClassPredicate(ClassKind.OBJECT, isCompanion)

class CompanionObjectPredicate : ObjectPredicate(isCompanion = true)

class InterfacePredicate : ClassPredicate(ClassKind.INTERFACE)

class AnnotationPredicate : ClassPredicate(ClassKind.ANNOTATION_CLASS)

class EnumPredicate : ClassPredicate(ClassKind.ENUM_CLASS)

class PropertyPredicate : VariablePredicate("Property") {
    override val visitor: Visitor
        get() = MyVisitor()

    private var getterPredicate: FunctionPredicate? = null
    private var setterPredicate: FunctionPredicate? = null

    fun getter(init: FunctionPredicate.() -> Unit) {
        val predicate = FunctionPredicate("Getter")
        predicate.init()
        getterPredicate = predicate
    }

    fun setter(init: FunctionPredicate.() -> Unit) {
        val predicate = FunctionPredicate("Setter")
        predicate.init()
        setterPredicate = predicate
    }

    inner class MyVisitor : Visitor {
        override fun visitElement(element: IrElement, data: Unit): VisitorData = falseVisitorData()

        override fun visitProperty(declaration: IrProperty, data: Unit): VisitorData {
            if (isVar != null && declaration.isVar != isVar ||
                isVal != null && declaration.isVar == isVal ||
                isConst != null && declaration.isConst != isConst ||
                isLateinit != null && declaration.isLateinit != isLateinit
            ) {
                return falseVisitorData()
            }

            val matches: VisitorDataMap = mutableMapOf()
            if (typePredicate != null) {
                val result = typePredicate!!.checkType(declaration.type, declaration)
                matches[typePredicate!!] = mutableListOf(result)
            }

            if (getterPredicate != null) {
                val result = if (declaration.getter != null) {
                    getterPredicate!!.checkIrNode(declaration.getter!!)
                } else {
                    return falseVisitorData()
                }
                matches[getterPredicate!!] = mutableListOf(result)
            }

            if (setterPredicate != null) {
                val result = if (declaration.setter != null) {
                    setterPredicate!!.checkIrNode(declaration.setter!!)
                } else {
                    return falseVisitorData()
                }
                matches[setterPredicate!!] = mutableListOf(result)
            }
            val result = matchedPredicatesToVisitorData(declaration, matches)
            if (result.matched) {
                info()
            }
            return result
        }
    }
}
