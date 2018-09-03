// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions +UseReturnsEffect
// !DIAGNOSTICS: -EXPERIMENTAL_API_USAGE_ERROR -DATA_CLASS_WITHOUT_PARAMETERS -CONTRACT_NOT_ALLOWED
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.contracts.*

data class XYZ(/*...*/)

class XYZBuilder {
    private var x_: Int? = null
    fun setValX(value: Int = 0) {
        contract {
            provides(Calls(::setValX, this@XYZBuilder))
        }
        x_ = value
    }

    private var y_: Int? = null
    fun setDefaultValY(value: Int = 0) {
        contract {
            provides(Calls(::setDefaultValY, this@XYZBuilder))
        }
        y_ = value
    }

    private var z_: Int? = null
    fun setVarZ(value: Int = 0) {
        contract {
            provides(Calls(::setVarZ, this@XYZBuilder))
        }
        z_ = value
    }

    fun buildXYZ() = XYZ(/*...*/)
}

fun buildXYZ(init: XYZBuilder.() -> Unit): XYZ {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        requires(init, CallKind(XYZBuilder::setValX, DslCallKind.EXACTLY_ONCE, ReceiverOf(init)))
        requires(init, CallKind(XYZBuilder::setDefaultValY, DslCallKind.AT_MOST_ONCE, ReceiverOf(init)))
        requires(init, CallKind(XYZBuilder::setVarZ, DslCallKind.AT_LEAST_ONCE, ReceiverOf(init)))
    }
    val builder = XYZBuilder()
    builder.init()
    return builder.buildXYZ()
}

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
// ---------------- TESTS ----------------

fun test_1() {
    buildXYZ {
        setValX() // XYZ
        buildXY {
            setValX() // XY
            setVarZ() // XYZ
            setVarZ() // XYZ
        }
        setVarZ() // XYZ
    }
}

fun test_2() {
    buildXYZ {
        setValX() // XYZ
        buildXY {
            setValX() // XYZ
            setVarZ() // XYZ
            setVarZ() // XYZ
            setDefaultValY() // XY
        }
    }
}

fun test_3() {
    buildXYZ <!CONTEXTUAL_EFFECT_WARNING(setValX call mismatch: expected EXACTLY_ONCE, actual ZERO)!>{
        setVarZ() // XYZ
        buildXY {
            setValX() // XY
            setDefaultValY() //XY
        }
    }<!>
}

fun test_5() {
    buildXYZ {
        setVarZ() // XYZ
        buildXY <!CONTEXTUAL_EFFECT_WARNING(setValX call mismatch: expected EXACTLY_ONCE, actual ZERO)!>{
            setDefaultValY() // XY
        }<!>
        setValX() // XYZ
    }
}

fun test_6() {
    buildXY {
        setValX()
        setDefaultValY()
    }
    buildXY <!CONTEXTUAL_EFFECT_WARNING(setValX call mismatch: expected EXACTLY_ONCE, actual ZERO)!>{}<!>
}