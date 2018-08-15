// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException

// In that case we don't check inner functions declarations

fun supplier() {
    contract {
        supplies(ExceptionEffectDescription<FileNotFoundException>())
    }
    throw FileNotFoundException()
}

fun good_1() {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    val func = { supplier() }
    func()
}

fun good_2() {
    val func = { supplier() }
    func()
}