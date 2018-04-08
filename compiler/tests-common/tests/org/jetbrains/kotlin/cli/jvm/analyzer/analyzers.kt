/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer

import org.jetbrains.kotlin.cli.jvm.analyzer.scope.newAnalyzer

val analyzers = listOf(
    analyzerWhile(),
    analyzerFor1(),
    analyzerFor2(),
    analyzerIf1(),
    analyzerIf2()
)

fun analyzerWhile() = newAnalyzer {
    title = "while"
    function {
        body {
            whileLoop {
                body {
                    variableDefinition {
                        message = "while"
                    }
                }
            }
        }
    }
}

fun analyzerFor1() = newAnalyzer {
    title = "for recursive true"
    function {
        body {
            forLoop {
                body{
                    recursiveSearch = true
                    variableDefinition {
                        message = "for"
                    }
                }
            }
        }
    }
}

fun analyzerFor2() = newAnalyzer {
    title = "for recursive false"
    function {
        body {
            forLoop {
                body{
                    recursiveSearch = false
                    variableDefinition {
                        message = "for"
                    }
                }
            }
        }
    }
}

fun analyzerIf1() = newAnalyzer {
    title = "if then else"
    function {
        body {
            ifCondition {
                thenBranch {
                    variableDefinition {
                        message = "then"
                    }
                }
                elseBranch {
                    variableDefinition {
                        message = "else"
                    }
                }
            }
        }
    }
}

fun analyzerIf2() = newAnalyzer {
    title = "if then else no rec"
    function {
        body {
            recursiveSearch = false
            ifCondition {
                thenBranch {
                    variableDefinition {
                        message = "then"
                    }
                }
                elseBranch {
                    variableDefinition {
                        message = "else"
                    }
                }
            }
        }
    }
}