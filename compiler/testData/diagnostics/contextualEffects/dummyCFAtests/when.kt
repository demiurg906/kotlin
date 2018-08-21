// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException

fun foo() {}

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
    val x: Int = 10
    when (x) {
        in 0..3 -> supplier()
        in 5..7 -> supplier()
        else -> supplier()
    }
}

fun good_2() {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    val x: Int = 10
    when (x) {
        in 0..3 -> supplier()
        in 5..7 -> supplier()
        else -> foo()
    }
}

fun good_3() {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    val x: Int = 10
    when (x) {
        in 0..3 -> foo()
        in 5..7 -> supplier()
        else -> foo()
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_1()<!> {
    val x: Int = 10
    when (x) {
        in 0..3 -> supplier()
        in 5..7 -> supplier()
        else -> supplier()
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_2()<!> {
    val x: Int = 10
    when (x) {
        in 0..3 -> supplier()
        in 5..7 -> supplier()
        else -> foo()
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_3()<!> {
    val x: Int = 10
    when (x) {
        in 0..3 -> foo()
        in 5..7 -> supplier()
        else -> foo()
    }
}