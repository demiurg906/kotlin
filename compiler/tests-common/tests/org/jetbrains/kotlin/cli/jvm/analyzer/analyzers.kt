/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer

import org.jetbrains.kotlin.cli.jvm.analyzer.predicates.ClassPredicate
import org.jetbrains.kotlin.cli.jvm.analyzer.predicates.TypePredicate
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality

// Live Template: testana

val analyzers = listOf(
    classTst(),
    functionDefinition(),
    ifThenElse(),
    functionName(),
    functionCall(),
    forLoop(),
    variableType(),
    whileLoop(),
    fileEverywhere(),
    whileEverywhere(),
    functionEverywhere(),
    objectEverywhere(),
    companionShortName()
).map { it.first.title to it }.toMap()

fun companionShortName() = analyzer("companionShortName") {
    var nestedClass: ClassPredicate? = null

    classDefinition {
        modality = Modality.OPEN
        companionObject {
            nestedClass = classDefinition { }
        }
    }

    classDefinition {
        function {
            returnType = TypePredicate(classPredicate = nestedClass)
        }
    }

} to true

fun whileEverywhere() = analyzer("whileEverywhere") {
    function {
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
        everywhere {
            variableDefinition { type = TypePredicate.Int }
        }
    }
} to true

fun fileEverywhere() = analyzer("fileEverywhere") {
    everywhere {
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
        numberOfArguments = 2
        argument { type = TypePredicate("Int") }
        argument { type = TypePredicate("A") }
        returnType = TypePredicate("Int")

        info = { println("foo founded") }
    }
} to true


fun ifThenElse() = analyzer("ifThenElse") {
    function {
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
    function { name = "foo" }
} to true


fun functionCall() = analyzer("functionCall") {
    val foo = function { name = "foo" }
    val baz = function { name = "baz" }
    function {
        body {
            functionCall(foo)
            functionCall(baz)
        }
    }
} to true


fun forLoop() = analyzer("forLoop") {
    function {
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
        body {
            variableDefinition { type = TypePredicate.Int }
        }
    }
} to true


fun whileLoop() = analyzer("whileLoop") {
    function {
        body {
            whileLoop {
                body {
                    variableDefinition { type = TypePredicate.Int }
                }
            }
        }
    }
} to true