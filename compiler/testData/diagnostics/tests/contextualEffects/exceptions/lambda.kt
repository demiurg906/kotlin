// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException

// In that case we don't check inner functions declarations

@Suppress("INVISIBLE_MEMBER")
fun supplier() {
    contract {
        supplies(ExceptionEffectDescription<FileNotFoundException>())
    }
    throw FileNotFoundException()
}

@Suppress("INVISIBLE_MEMBER")
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