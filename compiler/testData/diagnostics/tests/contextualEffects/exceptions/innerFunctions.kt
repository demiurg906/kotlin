// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions

import kotlin.internal.contracts.*

fun supplier() {
    // contract { supplies Exception("Exception") }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
inline fun myRun(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
}

// bad
fun consumer1() {
    <!CONTEXTUAL_EFFECT_WARNING!>fun func()<!> {
        supplier()
    }
}

fun bad2() {
    // contract { consumes Exception("Exception") }
    <!CONTEXTUAL_EFFECT_WARNING!>fun func()<!> {
        supplier()
    }
}

fun good1() {
    fun consumerInner() {
        // contract { consumes Exception("Exception") }
        supplier()
    }
}

// good
fun consumer2() {
    // contract { consumes Exception("Exception") }
    myRun { supplier() }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad3()<!> {
    myRun { supplier() }
}