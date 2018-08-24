/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.facts.exceptions

import org.jetbrains.kotlin.compiler.plugin.contracts.ContextEffectsComponent
import org.jetbrains.kotlin.contracts.ContextFamiliesRegistrar
import org.jetbrains.kotlin.contracts.contextual.Context
import org.jetbrains.kotlin.contracts.contextual.ContextFact
import org.jetbrains.kotlin.contracts.contextual.ContextFamily
import org.jetbrains.kotlin.contracts.contextual.FactsCombiner
import org.jetbrains.kotlin.contracts.description.InvocationKind
import org.jetbrains.kotlin.contracts.description.expressions.VariableReference
import org.jetbrains.kotlin.contracts.model.ESValue
import org.jetbrains.kotlin.contracts.parsing.*
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isSubtypeOf

class ExceptionFact(val exceptionType: KotlinType) : ContextFact() {
    override val family = ExceptionFamily
    override val isAllowedStayInContext = true
}

class ExceptionContext(override val facts: Set<ExceptionFact> = setOf()) : Context() {
    override fun addFact(fact: ContextFact): Context {
        if (fact !is ExceptionFact) throw AssertionError()
        return ExceptionContext(facts + fact)
    }
}

object ExceptionFactsCombiner : FactsCombiner() {
    override fun or(a: Context, b: Context): Context {
        if (a !is ExceptionContext || b !is ExceptionContext) throw AssertionError()
        return ExceptionContext(a.facts + b.facts)
    }

    override fun combine(context: Context, fact: ContextFact): Context {
        if (context !is ExceptionContext || fact !is ExceptionFact) throw AssertionError()
        return context.addFact(fact)
    }

    override fun updateWithInvocationKind(context: Context, invocationKind: InvocationKind): Context {
        if (context !is ExceptionContext) throw AssertionError()
        return context
    }
}

object ExceptionFamily : ContextFamily() {
    override val id: String = "Checked exceptions"
    override val combiner = ExceptionFactsCombiner
    override val emptyContext = ExceptionContext()
}

class ExceptionChecker(val exceptionType: KotlinType, val calledElement: KtElement) : ContextChecker() {
    override val family = ExceptionFamily

    override fun verifyContext(context: Context, trace: BindingTrace, shouldReport: Boolean): Context {
        if (context !is ExceptionContext) throw AssertionError()
        val isOk = context.facts.asSequence().any { exceptionType.isSubtypeOf(it.exceptionType) }
        if (!isOk && shouldReport) {
            trace.report(Errors.CONTEXTUAL_EFFECT_WARNING.on(calledElement, "Unchecked exception: $exceptionType"))
        }
        return context
    }
}

class ExceptionFactFactoryDeclaration(val exceptionType: KotlinType) : ContextFactFactoryDeclaration() {
    override fun resolveFactory(owner: ESValue, references: List<ESValue?>) = ExceptionFactFactory(owner, exceptionType)
}

class ExceptionFactFactory(owner: ESValue, val exceptionType: KotlinType) : ContextFactFactory(owner) {
    override fun createFact(calledElement: KtElement) = ExceptionFact(exceptionType)
}

class ExceptionCheckerFactoryDeclaration(val exceptionType: KotlinType) : ContextCheckerFactoryDeclaration() {
    override fun resolveFactory(owner: ESValue, references: List<ESValue?>) = ExceptionCheckerFactory(owner, exceptionType)
}

class ExceptionCheckerFactory(owner: ESValue, val exceptionType: KotlinType) : ContextCheckerFactory(owner) {
    override fun createChecker(calledElement: KtElement) = ExceptionChecker(exceptionType, calledElement)
}

class ExceptionFactParser(context: BindingContext, dispatcher: PsiContractParserDispatcher) : ContextFactParser(context, dispatcher) {
    companion object {
        private const val CONSTRUCTOR_NAME = "CatchesException"
    }

    override fun parseDeclarationForFactFactory(declaration: KtExpression): Pair<ContextFactFactoryDeclaration, List<VariableReference>>? {
        val exceptionType = getExceptionType(declaration) ?: return null
        val factory = ExceptionFactFactoryDeclaration(exceptionType)
        return factory to emptyList()
    }

    override fun parseDeclarationForCheckerFactory(declaration: KtExpression): Pair<ContextCheckerFactoryDeclaration, List<VariableReference>>? {
        val exceptionType = getExceptionType(declaration) ?: return null
        val factory = ExceptionCheckerFactoryDeclaration(exceptionType)
        return factory to emptyList()
    }

    private fun getExceptionType(expression: KtExpression): KotlinType? {
        if (expression !is KtCallExpression) return null
        val resolvedCall = expression.getResolvedCall(context) ?: return null
        val descriptor = resolvedCall.resultingDescriptor

        val constructorName = extractConstructorName(descriptor) ?: return null
        if (constructorName != CONSTRUCTOR_NAME) return null

        return resolvedCall.typeArguments.values.firstOrNull()
    }
}

class ExceptionContextEffectsComponent : ContextEffectsComponent {
    override fun registerProjectComponents(contextFamiliesRegistrar: ContextFamiliesRegistrar) {
        contextFamiliesRegistrar.registerFamily(ExceptionFamily, ::ExceptionFactParser)
    }
}