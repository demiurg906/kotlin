/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer

import com.google.gson.*
import org.jetbrains.kotlin.cli.jvm.analyzer.predicates.AbstractPredicate
import org.jetbrains.kotlin.cli.jvm.analyzer.predicates.FilePredicate
import org.jetbrains.kotlin.cli.jvm.analyzer.predicates.Predicate
import org.jetbrains.kotlin.cli.jvm.analyzer.predicates.VisitorData
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.resolve.BindingContext
import java.lang.reflect.Type

class Analyzer(
    val title: String,
    val predicate: AbstractPredicate
) {
    fun execute(irModule: IrModuleFragment, moduleDescriptor: ModuleDescriptor, bindingContext: BindingContext): Pair<Boolean, Map<IrFile, VisitorData>> {
        var result = true
        val resultMap = mutableMapOf<IrFile, VisitorData>()
        for (file in irModule.files) {
            val predicateResult = predicate.checkIrNode(file)

            resultMap[file] = predicateResult
            printResult(predicateResult)
            result = result && predicateResult.matched
        }
        return result to resultMap
    }

    private fun printResult(result: VisitorData) {
        val gson = GsonBuilder()
            .registerTypeAdapter(Predicate::class.java, PredicateJsonConverter())
            .registerTypeAdapter(IrElement::class.java, IrElementJsonConverter())
            .setPrettyPrinting()
            .create()
        try {
            val json = gson.toJson(result)
            println(json)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
}

class PredicateJsonConverter : JsonSerializer<Predicate> {
    override fun serialize(src: Predicate, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.toString())
    }
}

class IrElementJsonConverter : JsonSerializer<IrElement> {
    override fun serialize(src: IrElement?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.toString())
    }
}

fun analyzer(title: String, init: FilePredicate.() -> Unit): Analyzer {
    val predicate = FilePredicate()
    predicate.init()
    return Analyzer(title, predicate)
}