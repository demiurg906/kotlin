/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.contracts.model

import org.jetbrains.kotlin.contracts.model.structure.*


interface ESExpressionVisitor<out T> {
    fun visitIs(isOperator: ESIs): T
    fun visitEqual(equal: ESEqual): T
    fun visitAnd(and: ESAnd): T
    fun visitNot(not: ESNot): T
    fun visitOr(or: ESOr): T

    fun visitVariable(esVariable: ESVariable): T
    fun visitConstant(esConstant: ESConstant): T

    fun visitReceiverReference(esReceiverReference: ESReceiverReference): T
    fun visitFunction(esFunction: ESFunction): T
    fun visitReceiver(esReceiver: ESReceiver): T
}

interface ESExpressionVisitorWithData<in D, out T> {
    fun visitIs(isOperator: ESIs, data: D): T
    fun visitEqual(equal: ESEqual, data: D): T
    fun visitAnd(and: ESAnd, data: D): T
    fun visitNot(not: ESNot, data: D): T
    fun visitOr(or: ESOr, data: D): T

    fun visitVariable(esVariable: ESVariable, data: D): T
    fun visitConstant(esConstant: ESConstant, data: D): T

    fun visitReceiverReference(esReceiverReference: ESReceiverReference, data: D): T
    fun visitFunction(esFunction: ESFunction, data: D): T
    fun visitReceiver(esReceiver: ESReceiver, data: D): T
}