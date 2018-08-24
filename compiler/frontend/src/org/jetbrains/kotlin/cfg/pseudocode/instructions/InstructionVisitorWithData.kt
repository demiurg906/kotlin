/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license 
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cfg.pseudocode.instructions

import org.jetbrains.kotlin.cfg.pseudocode.instructions.eval.*
import org.jetbrains.kotlin.cfg.pseudocode.instructions.jumps.*
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.*

abstract class InstructionVisitorWithData<in D, out R> {
    abstract fun visitInstruction(instruction: Instruction, data: D): R

    open fun visitAccessInstruction(instruction: AccessValueInstruction, data: D): R = visitInstructionWithNext(instruction, data)

    open fun visitReadValue(instruction: ReadValueInstruction, data: D): R = visitAccessInstruction(instruction, data)

    open fun visitLocalFunctionDeclarationInstruction(instruction: LocalFunctionDeclarationInstruction, data: D): R =
        visitInstructionWithNext(instruction, data)

    open fun visitInlinedFunctionDeclarationInstruction(instruction: InlinedLocalFunctionDeclarationInstruction, data: D): R =
        visitLocalFunctionDeclarationInstruction(instruction, data)

    open fun visitVariableDeclarationInstruction(instruction: VariableDeclarationInstruction, data: D): R = visitInstructionWithNext(instruction, data)

    open fun visitUnconditionalJump(instruction: UnconditionalJumpInstruction, data: D): R = visitJump(instruction, data)

    open fun visitConditionalJump(instruction: ConditionalJumpInstruction, data: D): R = visitJump(instruction, data)

    open fun visitReturnValue(instruction: ReturnValueInstruction, data: D): R = visitJump(instruction, data)

    open fun visitReturnNoValue(instruction: ReturnNoValueInstruction, data: D): R = visitJump(instruction, data)

    open fun visitThrowExceptionInstruction(instruction: ThrowExceptionInstruction, data: D): R = visitJump(instruction, data)

    open fun visitNondeterministicJump(instruction: NondeterministicJumpInstruction, data: D): R = visitInstruction(instruction, data)

    open fun visitSubroutineExit(instruction: SubroutineExitInstruction, data: D): R = visitInstruction(instruction, data)

    open fun visitSubroutineSink(instruction: SubroutineSinkInstruction, data: D): R = visitInstruction(instruction, data)

    open fun visitJump(instruction: AbstractJumpInstruction, data: D): R = visitInstruction(instruction, data)

    open fun visitInstructionWithNext(instruction: InstructionWithNext, data: D): R = visitInstruction(instruction, data)

    open fun visitSubroutineEnter(instruction: SubroutineEnterInstruction, data: D): R = visitInstructionWithNext(instruction, data)

    open fun visitWriteValue(instruction: WriteValueInstruction, data: D): R = visitAccessInstruction(instruction, data)

    open fun visitLoadUnitValue(instruction: LoadUnitValueInstruction, data: D): R = visitInstructionWithNext(instruction, data)

    open fun visitOperation(instruction: OperationInstruction, data: D): R = visitInstructionWithNext(instruction, data)

    open fun visitCallInstruction(instruction: CallInstruction, data: D): R = visitOperation(instruction, data)

    open fun visitMerge(instruction: MergeInstruction, data: D): R = visitOperation(instruction, data)

    open fun visitMarkInstruction(instruction: MarkInstruction, data: D): R = visitInstructionWithNext(instruction, data)

    open fun visitMagic(instruction: MagicInstruction, data: D): R = visitOperation(instruction, data)
}