/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer

import org.jetbrains.kotlin.cli.jvm.analyzer.predicates.ClassPredicate
import org.jetbrains.kotlin.cli.jvm.analyzer.predicates.FunctionPredicate
import org.jetbrains.kotlin.cli.jvm.analyzer.predicates.TypePredicate
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality

val analyzers = listOf(
    companionShortName(),
    demoTest(),
    fileEverywhere(),
    forLoop(),
    functionCall(),
    functionDefinition(),
    functionEverywhere(),
    functionName(),
    ifThenElse(),
    objectEverywhere(),
    variableType(),
    whileEverywhere(),
    whileLoop(),
    classTst()
).map { it.first.title to it }.toMap()

fun demoTest() = analyzer("demoTest") {
    val abstractField = classDefinition {
        printResult = true
        label = "class AbstractField"
        name = "AbstractField"
        modality = Modality.ABSTRACT
    }

    val field = classDefinition {
        printResult = true
        label = "class Field"

        superClass(abstractField)
        function {
            numberOfArguments = 2
            argument { type = TypePredicate.Int }
            argument { type = TypePredicate.Int }
        }
    }

    val fieldType = TypePredicate(classPredicate = field)

    val next = function {
        printResult = true
        label = "fun next(...)"

        argument { type = fieldType }
        returnType = fieldType
    }

    val goal = function {
        printResult = true
        label = "fun runGameOfLive"

        body {
            everywhere {
                functionCall(next)
            }
        }
    }

    everywhere {
        printResult = true
        label = "Goal"

        functionCall(goal) {
            argument("steps", 3)
        }
    }

} to true

fun companionShortName() = analyzer("companionShortName") {
    var nestedClass: ClassPredicate? = null

    val parent = classDefinition {
        label = "Parent"
        modality = Modality.OPEN
        companionObject {
            nestedClass = classDefinition { }
        }
    }

    classDefinition {
        printResult = true
        label = "Child"
        superClass(parent)
        function {
            returnType = TypePredicate(classPredicate = nestedClass)
        }
    }

} to true

fun whileEverywhere() = analyzer("whileEverywhere") {
    function {
        printResult = true
        body {
            whileLoop {
                body {
                    everywhere {
                        variableDefinition { type = TypePredicate.Int }
                    }
                }
            }
        }
    }
} to true

fun functionEverywhere() = analyzer("functionEverywhere") {
    function {
        printResult = true
        body {
            everywhere {
                variableDefinition { type = TypePredicate.Int }
            }

            variableDefinition { type = TypePredicate.Double }
        }
    }
} to true

fun objectEverywhere() = analyzer("objectEverywhere") {
    objectDefinition {
        printResult = true
        everywhere {
            variableDefinition { type = TypePredicate.Int }
        }
    }
} to true

fun fileEverywhere() = analyzer("fileEverywhere") {
    everywhere {
        printResult = true
        variableDefinition { type = TypePredicate.Int }
    }
} to true

fun classTst() = analyzer("classTst") {
    val i2 = interfaceDefinition { name = "I2" }
    val a = classDefinition {
        name = "A"
        superClass(ClassKind.INTERFACE) { name = "I1" }
    }

    classDefinition {
        printResult = true
        name = "B"

        superClass(i2)
        superClass(a)

        function {
            argument { type = TypePredicate.Int }
        }

        propertyDefinition {
            isVal = true
            type = TypePredicate.Int
        }
    }
} to true


fun functionDefinition() = analyzer("functionDefinition") {
    function {
        printResult = true
        numberOfArguments = 2
        argument { type = TypePredicate("Int") }
        argument { type = TypePredicate("A") }
        returnType = TypePredicate("Int")

        info = { println("foo founded") }
    }
} to true


fun ifThenElse() = analyzer("ifThenElse") {
    function {
        printResult = true
        body {
            ifCondition {
                thenBranch {
                    variableDefinition { type = TypePredicate.Int }
                }
                elseBranch {
                    variableDefinition { type = TypePredicate.Int }
                }
            }
        }
    }
} to true


fun functionName() = analyzer("functionName") {
    function {
        printResult = true
        name = "foo"
    }
} to true


fun functionCall() = analyzer("functionCall") {
    val foo = function {
        name = "foo"
        label = "function foo"
    }
    val baz = function {
        name = "baz"
        label = "function baz"
    }
    function {
        printResult = true
        label = "function bar"
        body {
            functionCall(foo) { label = "call foo" }
            functionCall(baz) {
                label = "call baz"
                argument("x", 10)
            }
        }
    }
} to true


fun forLoop() = analyzer("forLoop") {
    function {
        printResult = true
        body {
            forLoop {
                body {
                    variableDefinition { type = TypePredicate.Int }
                }
            }
        }
    }
} to true


fun variableType() = analyzer("variableType") {
    function {
        printResult = true
        body {
            variableDefinition { type = TypePredicate.Int }
        }
    }
} to true


fun whileLoop() = analyzer("whileLoop") {
    function {
        printResult = true
        body {
            whileLoop {
                body {
                    variableDefinition { type = TypePredicate.Int }
                }
            }
        }
    }
} to true