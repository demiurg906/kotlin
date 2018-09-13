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
        expectsTo(init, CallKind(XYZBuilder::setValX, DslCallKind.EXACTLY_ONCE, ReceiverOf(init)))
        expectsTo(init, CallKind(XYZBuilder::setDefaultValY, DslCallKind.AT_MOST_ONCE, ReceiverOf(init)))
        expectsTo(init, CallKind(XYZBuilder::setVarZ, DslCallKind.AT_LEAST_ONCE, ReceiverOf(init)))
    }
    val builder = XYZBuilder()
    builder.init()
    return builder.buildXYZ()
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
        expectsTo(init, CallKind(XBuilder::setValX, DslCallKind.EXACTLY_ONCE, ReceiverOf(init)))
    }
    val builder = XBuilder()
    builder.init()
    return builder.buildX()
}

data class Y(/*...*/)

class YBuilder {
    private var y_: Int? = null
    fun setValY(value: Int = 0) {
        contract {
            provides(Calls(::setValY, this@YBuilder))
        }
        y_ = value
    }

    fun buildY() = Y(/*...*/)
}

fun buildY(init: YBuilder.() -> Unit): Y {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        expectsTo(init, CallKind(YBuilder::setValY, DslCallKind.EXACTLY_ONCE, ReceiverOf(init)))
    }
    val builder = YBuilder()
    builder.init()
    return builder.buildY()
}

data class Z(/*...*/)

class ZBuilder {
    private var z_: Int? = null
    fun setValZ(value: Int = 0) {
        contract {
            provides(Calls(::setValZ, this@ZBuilder))
        }
        z_ = value
    }

    fun buildZ() = Z(/*...*/)
}

fun buildZ(init: ZBuilder.() -> Unit): Z {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        expectsTo(init, CallKind(ZBuilder::setValZ, DslCallKind.EXACTLY_ONCE, ReceiverOf(init)))
    }
    val builder = ZBuilder()
    builder.init()
    return builder.buildZ()
}

// ---------------- TESTS ----------------

fun test_1() {
    buildXYZ {
        buildXYZ {
            setValX()
            setDefaultValY()
            setVarZ()
            buildXYZ {
                setValX()
                setDefaultValY()
                setVarZ()
            }
        }
        setValX()
        setDefaultValY()
        setVarZ()
        buildXYZ {
            buildXYZ {
                setValX()
                setDefaultValY()
                setVarZ()
            }
            setValX()
            setDefaultValY()
            setVarZ()
        }
    }
}


fun test_2() {
    buildX {
        buildY {
            buildZ {
                setValX()
                setValY()
                setValZ()
            }
        }
    }
}

fun test_3() {
    buildX {
        setValX()
        buildX <!CONTEXTUAL_EFFECT_WARNING(setValX call mismatch: expected EXACTLY_ONCE, actual ZERO)!>{}<!>
    }
}

fun test_4() {
    buildX {
        buildX <!CONTEXTUAL_EFFECT_WARNING(setValX call mismatch: expected EXACTLY_ONCE, actual ZERO)!>{}<!>
        setValX()
    }
}

fun test_5() {
    buildX <!CONTEXTUAL_EFFECT_WARNING(setValX call mismatch: expected EXACTLY_ONCE, actual ZERO)!>{
        buildX {
            setValX()
        }
    }<!>

}