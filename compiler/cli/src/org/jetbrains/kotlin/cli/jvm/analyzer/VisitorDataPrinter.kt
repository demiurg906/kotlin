/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer

import com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.cli.jvm.analyzer.predicates.VisitorData
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.psi.KtFile

typealias StringVisitorDataMap = Map<String, List<StringVisitorData>>

data class StringVisitorData(
    val predicate: String,
    val element: String,
    val innerPredicates: StringVisitorDataMap
)

class IrToStringTransformer(ktFile: KtFile) {
    private val visitor = IrToStringVisitor(ktFile)

    fun transformIrElementsToString(data: VisitorData): StringVisitorData {
        val predicate = data.predicate.toString()
        val element: String = if (data.element != null) {
            data.element.accept(visitor, Unit)
        } else {
            "null"
        }
        val innerPredicates = data.innerPredicatesMatches.entries.map { (predicate, data) ->
            predicate.toString() to data.map { transformIrElementsToString(it) }
        }.toMap()
        return StringVisitorData(predicate, element, innerPredicates)
    }

    private class IrToStringVisitor(private val ktFile: KtFile) : IrElementVisitor<String, Unit> {
        private val document = ktFile.manager.findViewProvider(ktFile.virtualFile)!!.document!!

        override fun visitElement(element: IrElement, data: Unit): String {
            val psiElement = ktFile.findElementAt(element.startOffset) ?: return "ERROR: no element at offset ${element.startOffset}"
            val offset = psiElement.textOffset
            val line = document.getLineNumber(offset)

            val startOffset = document.getLineStartOffset(line)
            val endOffset = document.getLineEndOffset(line)
            val range = TextRange(startOffset, endOffset)

            val column = offset - startOffset
            val text = document.getText(range).trim()
            return "${ktFile.name}:${line + 1}:${column + 1}: $text"
        }
    }
}
