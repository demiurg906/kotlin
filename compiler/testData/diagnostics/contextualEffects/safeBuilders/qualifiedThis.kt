// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions +UseReturnsEffect
// !DIAGNOSTICS: -EXPERIMENTAL_API_USAGE_ERROR -DATA_CLASS_WITHOUT_PARAMETERS -CONTRACT_NOT_ALLOWED
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.contracts.*

data class XY(/*...*/)

class XYBuilder {
    private var x_: Int? = null
    fun setValX(value: Int = 0) {
        contract {
            provides(Calls(::setValX, this@XYBuilder))
        }
        x_ = value
    }

    private var y_: Int? = null
    fun setDefaultValY(value: Int = 0) {
        contract {
            provides(Calls(::setDefaultValY, this@XYBuilder))
        }
        y_ = value
    }

    fun buildXY() = XY(/*...*/)
}

fun buildXY(init: XYBuilder.() -> Unit): XY {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        requires(init, CallKind(XYBuilder::setValX, DslCallKind.EXACTLY_ONCE, ReceiverOf(init)))
        requires(init, CallKind(XYBuilder::setDefaultValY, DslCallKind.AT_MOST_ONCE, ReceiverOf(init)))
    }
    val builder = XYBuilder()
    builder.init()
    return builder.buildXY()
}

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
        requires(init, CallKind(XBuilder::setValX, DslCallKind.EXACTLY_ONCE, ReceiverOf(init)))
    }
    val builder = XBuilder()
    builder.init()
    return builder.buildX()
}

// ---------------- TESTS ----------------

fun test_1() {
    buildXY {
        buildX {
            this@buildXY.setValX() // XY builder
            setValX() // X builder
            setDefaultValY() // Y builder
        }
    }
}

fun test_2() {
    buildX outer@ {
        buildX {
            this@outer.setValX()
            setValX()
        }
    }
}

fun test_3() {
    buildX outer@ <!CONTEXTUAL_EFFECT_WARNING(setValX call mismatch: expected EXACTLY_ONCE, actual AT_LEAST_ONCE)!>{
        buildX {
            this@outer.setValX()
            this@outer.setValX()
            setValX()
        }
    }<!>
}

fun test_4() {
    buildXY <!CONTEXTUAL_EFFECT_WARNING(setValX call mismatch: expected EXACTLY_ONCE, actual AT_LEAST_ONCE)!>{
        buildX <!CONTEXTUAL_EFFECT_WARNING(setValX call mismatch: expected EXACTLY_ONCE, actual AT_LEAST_ONCE)!>{
            this@buildXY.setValX()
            this@buildXY.setValX()
            setValX()
            setValX()
        }<!>
    }<!>
}