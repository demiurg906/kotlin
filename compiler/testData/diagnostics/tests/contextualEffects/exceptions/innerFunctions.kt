// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
inline fun myRun(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
}

@Suppress("INVISIBLE_MEMBER")
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

@Suppress("INVISIBLE_MEMBER")
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
    @Suppress("INVISIBLE_MEMBER")
    fun func() {
        contract {
            consumes(ExceptionEffectDescription<IOException>())
        }
        supplier()
    }
}

@Suppress("INVISIBLE_MEMBER")
fun good_2() {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    myRun { supplier() }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_3()<!> {
    myRun { supplier() }
}