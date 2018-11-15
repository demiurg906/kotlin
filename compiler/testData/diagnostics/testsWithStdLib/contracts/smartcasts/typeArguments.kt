// !LANGUAGE: +AllowContractsForCustomFunctions +UseCallsInPlaceEffect
// !USE_EXPERIMENTAL: kotlin.contracts.ExperimentalContracts
// !DIAGNOSTICS: -INVISIBLE_REFERENCE -INVISIBLE_MEMBER
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.contracts.*

inline fun <reified T> Any.mustbe(): Boolean {
    contract {
        returns(true) implies (this@mustbe is T)
    }
    return this is T
}

fun test_1() {
    val x: Any = 10
    if (x.mustbe<Int>()) {
        println(<!DEBUG_INFO_SMARTCAST!>x<!> + 1)
    }
}
