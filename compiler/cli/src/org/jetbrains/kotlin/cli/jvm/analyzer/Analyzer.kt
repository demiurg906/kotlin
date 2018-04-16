/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.analyzer

import org.jetbrains.kotlin.cli.jvm.analyzer.predicates.AbstractPredicate
import org.jetbrains.kotlin.cli.jvm.analyzer.predicates.FilePredicate
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.resolve.BindingContext

class Analyzer(
    val title: String,
    val predicate: AbstractPredicate
) {
    fun execute(irModule: IrModuleFragment, moduleDescriptor: ModuleDescriptor, bindingContext: BindingContext): Boolean {
        var res = true
        for (file in irModule.files) {
            val (result, data) =  predicate.checkIrNode(file)
            println("${file.fqName}: predicate is $result")
            res = res && result
        }
        return res
    }
}

fun analyzer(title: String, init: FilePredicate.() -> Unit): Analyzer {
    val predicate = FilePredicate()
    predicate.init()
    return Analyzer(title, predicate)
}