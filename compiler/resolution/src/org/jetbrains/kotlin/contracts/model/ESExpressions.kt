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

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue
import org.jetbrains.kotlin.types.KotlinType

interface ESExpression {
    fun <T> accept(visitor: ESExpressionVisitor<T>): T
}

interface ESOperator : ESExpression {
    val functor: Functor
}

abstract class ESValue(override val type: KotlinType?) : Computation, ESExpression {
    override val effects: List<ESEffect> = listOf()
}

class ESFunction(val descriptor: FunctionDescriptor) : ESValue(null) {
    override fun <T> accept(visitor: ESExpressionVisitor<T>): T = visitor.visitFunction(this)
}

class ESReceiverReference(val lambda: ESValue) : ESValue(null) {
    override fun <T> accept(visitor: ESExpressionVisitor<T>): T = visitor.visitReceiverReference(this)
}

class ESReceiver(val receiver: ReceiverValue) : ESValue(null) {
    override fun <T> accept(visitor: ESExpressionVisitor<T>): T = visitor.visitReceiver(this)
}