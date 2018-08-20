/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cfg.effects

import org.jetbrains.kotlin.cfg.ImmutableHashMap
import org.jetbrains.kotlin.cfg.pseudocode.Pseudocode
import org.jetbrains.kotlin.cfg.pseudocode.instructions.Instruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.InstructionVisitorWithResult
import org.jetbrains.kotlin.cfg.pseudocode.instructions.eval.CallInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.InlinedLocalFunctionDeclarationInstruction
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.Edges
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.LocalFunctionAnalysisStrategy
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.TraversalOrder
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.collectData
import org.jetbrains.kotlin.contracts.ContextualEffectSystem
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectConsumer
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectFamily
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectsContext
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.resolve.BindingContext

class PseudocodeEffectsData(
    val pseudocode: Pseudocode,
    val bindingContext: BindingContext
) {
    val controlFlowInfo: EffectsControlFlowInfo? = computeEffectsControlFlowInfo(pseudocode)

    private fun computeEffectsControlFlowInfo(pseudocode: Pseudocode): EffectsControlFlowInfo? {
        val data = pseudocode.collectData(
            TraversalOrder.FORWARD,
            ::merge,
            ::combine,
            ::update,
            EffectsControlFlowInfo(),
            LocalFunctionAnalysisStrategy.ONLY_INLINED_LAMBDAS
        )
        return data[pseudocode.exitInstruction]?.incoming
    }

    private fun combine(instruction: Instruction, incoming: Collection<EffectsControlFlowInfo>): Edges<EffectsControlFlowInfo> =
        mergeWithOperation(instruction, incoming, Operation.AND)

    private fun merge(instruction: Instruction, incoming: Collection<EffectsControlFlowInfo>): Edges<EffectsControlFlowInfo> =
        mergeWithOperation(instruction, incoming, Operation.OR)

    private enum class Operation {
        OR, AND
    }

    private fun mergeWithOperation(
        instruction: Instruction,
        incoming: Collection<EffectsControlFlowInfo>,
        operation: Operation
    ): Edges<EffectsControlFlowInfo> {
        val incomingContext = when (incoming.size) {
            0 -> EffectsControlFlowInfo.EMPTY
            1 -> incoming.first()
            else -> mergeMultipleEdges(incoming, operation)
        }
        val visitor = EffectsInstructionVisitor(incomingContext)
        val outgoingContext = instruction.accept(visitor)
        return Edges(incomingContext, outgoingContext)
    }

    private fun mergeMultipleEdges(
        incoming: Collection<EffectsControlFlowInfo>,
        operation: Operation
    ): EffectsControlFlowInfo {
        val families = incoming.flatMap { it.keySet() }.toSet()

        val contextsGroupedByFamily = mutableMapOf<ContextualEffectFamily, List<ContextualEffectsContext>>()
        for (family in families) {
            val contexts = incoming.map { it[family].getOrElse(family.emptyContext) }
            contextsGroupedByFamily[family] = contexts
        }

        val groupedContexts = contextsGroupedByFamily
            .mapValues { (family, contexts) ->
                val lattice = family.lattice
                val (initial, foldFunction) = when (operation) {
                    Operation.OR -> lattice.bot() to lattice::or
                    Operation.AND -> lattice.top() to lattice::and
                }
                contexts.fold(initial, foldFunction)
            }
        return EffectsControlFlowInfo(ImmutableHashMap.ofAll(groupedContexts))
    }

    private fun update(from: Instruction, to: Instruction, info: EffectsControlFlowInfo): EffectsControlFlowInfo {
        val invocationKind = (to as? InlinedLocalFunctionDeclarationInstruction)?.kind ?: return info
        val context = EffectsControlFlowInfo(
            ImmutableHashMap.ofAll(info.convertToMap().mapValues { (family, context) ->
                family.lattice.updateContextWithInvocationKind(context, invocationKind)
            })
        )
        val lambda = to.element.parent as? KtLambdaExpression ?: return context
        val consumers = bindingContext[BindingContext.CONTEXTUAL_EFFECTS, lambda]?.consumers ?: return context
        return applyConsumers(context, consumers)
    }

    fun applyConsumers(controlFlowInfo: EffectsControlFlowInfo, allConsumers: List<ContextualEffectConsumer>): EffectsControlFlowInfo {
        var cfi = controlFlowInfo
        val allConsumersByFamily = allConsumers.groupBy { it.family }

        for ((family, consumers) in allConsumersByFamily) {
            var context = cfi[family].getOrElse(family.emptyContext)
            for (consumer in consumers) {
                val newContext = consumer.consume(context)
                context = newContext
            }
            cfi = cfi.put(family, context)
        }
        return cfi
    }

    private fun EffectsControlFlowInfo.convertToMap(): Map<ContextualEffectFamily, ContextualEffectsContext> {
        return iterator().map { it._1 to it._2 }.toList().toMap()
    }

    // TODO: переписать визитор на when
    private inner class EffectsInstructionVisitor(private val controlFlowInfo: EffectsControlFlowInfo) :
        InstructionVisitorWithResult<EffectsControlFlowInfo>() {

        override fun visitInstruction(instruction: Instruction): EffectsControlFlowInfo = controlFlowInfo

        override fun visitCallInstruction(instruction: CallInstruction): EffectsControlFlowInfo {
            // TODO: есть ли проблема с вызовом лямбд, лежащих в переменных?
            val descriptor =
                instruction.resolvedCall.resultingDescriptor as? FunctionDescriptor ?: TODO("check correctness") // return controlFlowInfo
            val suppliers = ContextualEffectSystem.declaredSuppliers(descriptor)
            var result = controlFlowInfo
            for (supplier in suppliers) {
                val family = supplier.family
                val context = result[family].getOrElse(family.emptyContext)
                result = result.put(family, supplier.supply(context))
            }
            return result
        }
    }
}