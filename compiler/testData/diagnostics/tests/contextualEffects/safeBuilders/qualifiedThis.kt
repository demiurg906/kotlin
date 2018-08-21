// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions +UseReturnsEffect
// !DIAGNOSTICS: -INVISIBLE_MEMBER -INVISIBLE_REFERENCE -DATA_CLASS_WITHOUT_PARAMETERS
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.internal.contracts.*

data class XY(/*...*/)

class XYBuilder {
    private var x_: Int? = null
    fun setValX(value: Int = 0) {
        contract {
            supplies(CallEffect(::setValX))
        }
        x_ = value
    }

    private var y_: Int? = null
    fun setDefaultValY(value: Int = 0) {
        contract {
            supplies(CallEffect(::setDefaultValY))
        }
        y_ = value
    }

    fun buildXY() = XY(/*...*/)
}

data class X(/*...*/)

class XBuilder {
    private var x_: Int? = null
    fun setValX(value: Int = 0) {
        contract {
            supplies(CallEffect(::setValX))
        }
        x_ = value
    }

    fun buildX() = X(/*...*/)
}

fun buildXY(init: XYBuilder.() -> Unit): XY {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        consumes(init, RequiresCallEffect(XYBuilder::setValX, DslCallKind.EXACTLY_ONCE))
        consumes(init, RequiresCallEffect(XYBuilder::setDefaultValY, DslCallKind.AT_MOST_ONCE))
    }
    val builder = XYBuilder()
    builder.init()
    return builder.buildXY()
}

fun buildX(init: XBuilder.() -> Unit): X {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        consumes(init, RequiresCallEffect(XBuilder::setValX, DslCallKind.EXACTLY_ONCE))
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

// incorrect behavior, but that case is shit
// and hopefully no one will write code like this
fun test_2() {
    buildX outer@ {
        setValX()
        buildX {
            this@outer.setValX()
        }
    }
}
