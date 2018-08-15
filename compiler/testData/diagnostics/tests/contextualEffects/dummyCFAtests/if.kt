// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER -UNUSED_VARIABLE

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException

val b = false

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
    if (b) {
        supplier()
    } else {
        val x = 10
    }
}


fun good_2() {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    if (b) {
        supplier()
    } else {
        supplier()
    }
}


fun good_3() {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    if (b) {
        val x = 10
    } else {
        supplier()
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_1()<!> {
    if (b) {
        supplier()
    } else {
        val x = 10
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_2()<!> {
    if (b) {
        supplier()
    } else {
        supplier()
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_3()<!> {
    if (b) {
        val x = 10
    } else {
        supplier()
    }
}