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
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.Edges
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.LocalFunctionAnalysisStrategy
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.TraversalOrder
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.collectData
import org.jetbrains.kotlin.contracts.contextual.ContextualEffectSystem
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.BindingContext

class PseudocodeEffectsData(val pseudocode: Pseudocode, private val bindingContext: BindingContext) {
    val controlFlowInfo = computeEffectsControlFlowInfo(pseudocode)

    private fun computeEffectsControlFlowInfo(pseudocode: Pseudocode): EffectsControlFlowInfo? {
        val data = pseudocode.collectData(
            TraversalOrder.FORWARD,
            ::merge,
            ::update,
            EffectsControlFlowInfo(),
            LocalFunctionAnalysisStrategy.DoNotAnalyse
        )
        // TODO: may be problems with sink (or exit?)
        return data[pseudocode.exitInstruction]?.incoming
    }

    private fun merge(instruction: Instruction, incoming: Collection<EffectsControlFlowInfo>): Edges<EffectsControlFlowInfo> {
        val incomingContext = when(incoming.size) {
            0 -> EffectsControlFlowInfo()
            1 -> incoming.first()
            else -> mergeMultipleEdges(incoming)
        }
        val visitor = EffectsInstructionVisitor(incomingContext)
        val outgoingContext = instruction.accept(visitor)
        return Edges(incomingContext, outgoingContext)
    }

    private fun mergeMultipleEdges(incoming: Collection<EffectsControlFlowInfo>): EffectsControlFlowInfo {
        val groupedContexts = incoming
            .flatMap { context -> context.iterator().map { it._1 to it._2 } }
            .groupBy { (family, _) -> family }
            .mapValues { it.value.map { (_, contexts) -> contexts } }
            .mapValues { (family, contexts) ->
                val lattice = family.lattice
                contexts.fold(lattice.bot(), lattice::or)
            }
        return EffectsControlFlowInfo(ImmutableHashMap.ofAll(groupedContexts))
    }

    private fun update(from: Instruction, to: Instruction, info: EffectsControlFlowInfo) = info

    private inner class EffectsInstructionVisitor(private val controlFlowInfo: EffectsControlFlowInfo) :
        InstructionVisitorWithResult<EffectsControlFlowInfo>() {

        override fun visitInstruction(instruction: Instruction): EffectsControlFlowInfo = controlFlowInfo

        override fun visitCallInstruction(instruction: CallInstruction): EffectsControlFlowInfo {
            // TODO: есть ли проблема с вызовом лямбд, лежащих в переменных?
            val descriptor = instruction.resolvedCall.candidateDescriptor as? FunctionDescriptor ?: TODO("check correctness") // return controlFlowInfo
            val suppliers = ContextualEffectSystem.declaredSuppliers(descriptor)
            var result = controlFlowInfo
            for (supplier in suppliers) {
                val family = supplier.family
                val context = result[family].getOrElse(family.emptyHolder)
                result = result.put(family, supplier.supply(context))
            }
            return result
        }
    }
}