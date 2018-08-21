// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER -INVISIBLE_REFERENCE

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException

inline fun myRun(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
}

fun supplier() {
    contract {
        supplies(ExceptionEffectDescription<FileNotFoundException>())
    }
    throw FileNotFoundException()
}

fun bad_1() {
    <!CONTEXTUAL_EFFECT_WARNING!>fun func()<!> {
        supplier()
    }
}

fun bad_2() {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    <!CONTEXTUAL_EFFECT_WARNING!>fun func()<!> {
        supplier()
    }
    supplier()
}

fun good_1() {
    fun func() {
        contract {
            consumes(ExceptionEffectDescription<IOException>())
        }
        supplier()
    }
}

fun good_2() {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    myRun { supplier() }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_3()<!> {
    myRun { supplier() }
}

fun bad_4() {
    fun func() {
        contract {
            consumes(ExceptionEffectDescription<IOException>())
        }
        supplier()
    }
    func()
}