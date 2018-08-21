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
    for (x in 1..10) {
        supplier()
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_1()<!> {
    for (x in 1..10) {
        supplier()
    }
}