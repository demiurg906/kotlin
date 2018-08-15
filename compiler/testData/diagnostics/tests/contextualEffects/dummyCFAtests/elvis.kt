// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER -UNUSED_VARIABLE -USELESS_ELVIS

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException

fun foo(): Int? {
    return null
}

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
    val x = foo() ?: supplier()
}

fun good_2() {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    val x = supplier() ?: foo()
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_1()<!> {
    val x = foo() ?: supplier()
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_2()<!> {
    val x = supplier() ?: foo()
}