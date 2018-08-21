// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions +UseReturnsEffect
// !DIAGNOSTICS: -INVISIBLE_MEMBER -INVISIBLE_REFERENCE -DATA_CLASS_WITHOUT_PARAMETERS
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.internal.contracts.*

data class XYZ(/*...*/)

class XYZBuilder {
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

    private var z_: Int? = null
    fun setVarZ(value: Int = 0) {
        contract {
            supplies(CallEffect(::setVarZ))
        }
        z_ = value
    }

    fun buildXYZ() = XYZ(/*...*/)
}

fun buildXYZ(init: XYZBuilder.() -> Unit): XYZ {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        consumes(init, RequiresCallEffect(XYZBuilder::setValX, DslCallKind.EXACTLY_ONCE))
        consumes(init, RequiresCallEffect(XYZBuilder::setDefaultValY, DslCallKind.AT_MOST_ONCE))
        consumes(init, RequiresCallEffect(XYZBuilder::setVarZ, DslCallKind.AT_LEAST_ONCE))
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
            supplies(CallEffect(::setValX))
        }
        x_ = value
    }
    
    fun buildX() = X(/*...*/)
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

data class Y(/*...*/)

class YBuilder {
    private var y_: Int? = null
    fun setValY(value: Int = 0) {
        contract {
            supplies(CallEffect(::setValY))
        }
        y_ = value
    }

    fun buildY() = Y(/*...*/)
}

fun buildY(init: YBuilder.() -> Unit): Y {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        consumes(init, RequiresCallEffect(YBuilder::setValY, DslCallKind.EXACTLY_ONCE))
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
            supplies(CallEffect(::setValZ))
        }
        z_ = value
    }

    fun buildZ() = Z(/*...*/)
}

fun buildZ(init: ZBuilder.() -> Unit): Z {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        consumes(init, RequiresCallEffect(ZBuilder::setValZ, DslCallKind.EXACTLY_ONCE))
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