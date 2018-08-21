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

fun good_1(y: Int) {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    var x = y
    while (x < 10) {
        supplier()
        x += 1
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_1(y: Int)<!> {
    var x = y
    while (x < 10) {
        supplier()
        x += 1
    }
}