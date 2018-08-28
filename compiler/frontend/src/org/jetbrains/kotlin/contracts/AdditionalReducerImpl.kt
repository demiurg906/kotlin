/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts

import org.jetbrains.kotlin.contracts.model.ESReceiver
import org.jetbrains.kotlin.contracts.model.ESReceiverReference
import org.jetbrains.kotlin.contracts.model.visitors.AdditionalReducer
import org.jetbrains.kotlin.resolve.BindingContext

class AdditionalReducerImpl(private val bindingContext: BindingContext) : AdditionalReducer {
    override fun visitReceiverReference(esReceiverReference: ESReceiverReference): ESReceiver? {
        val lambda = esReceiverReference.lambda as? ESLambda ?: throw AssertionError()
        val lambdaExpression = lambda.lambda
        val functionLiteral = lambdaExpression.functionLiteral
        val literal = bindingContext[BindingContext.FUNCTION, functionLiteral] ?: throw AssertionError()
        val receiverParameter = literal.extensionReceiverParameter ?: throw AssertionError()
        return ESReceiver(receiverParameter.value)
    }
}