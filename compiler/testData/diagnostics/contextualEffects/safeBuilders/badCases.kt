// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions +UseReturnsEffect
// !DIAGNOSTICS: -EXPERIMENTAL_API_USAGE_ERROR -DATA_CLASS_WITHOUT_PARAMETERS -CONTRACT_NOT_ALLOWED
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.contracts.*

data class X(/*...*/)

class XBuilder {
    private var x_: Int? = null
    fun setValX(value: Int = 0) {
        contract {
            provides(Calls(::setValX, this@XBuilder))
        }
        x_ = value
    }

    fun buildX() = X(/*...*/)
}

fun buildX(init: XBuilder.() -> Unit): X {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        expectsTo(init, CallKind(XBuilder::setValX, InvocationKind.EXACTLY_ONCE, ReceiverOf(init)))
    }
    val builder = XBuilder()
    builder.init()
    return builder.buildX()
}

// ---------------- TESTS ----------------

// control flow info is empty on exit
// need to be discovered
fun test_1() {
    buildX {
        while (true) {
            setValX()
        }
    }
}