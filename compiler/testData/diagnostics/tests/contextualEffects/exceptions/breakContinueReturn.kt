// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException

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
    var x = 0
    loop@ while (x > 0) {
        when (x) {
            1 -> supplier()
            2 -> break@loop
            3 -> return
        }
        x += 1
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_1()<!> {
    var x = 0
    loop@ while (x > 0) {
        when (x) {
            1 -> supplier()
            2 -> break@loop
            3 -> return
        }
        x += 1
    }
}