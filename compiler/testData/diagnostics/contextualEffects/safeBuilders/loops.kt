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

// ---------------- TESTS ----------------

fun test_1() {
    buildXYZ <!CONTEXTUAL_EFFECT_WARNING(setValX call mismatch: expected EXACTLY_ONCE, actual UNKNOWN), CONTEXTUAL_EFFECT_WARNING(setDefaultValY call mismatch: expected AT_MOST_ONCE, actual UNKNOWN), CONTEXTUAL_EFFECT_WARNING(setVarZ call mismatch: expected AT_LEAST_ONCE, actual UNKNOWN)!>{
        for (i in 0..10) {
            setValX()
            setDefaultValY()
            setVarZ()
        }
    }<!>
}

fun test_2(b: Boolean) {
    buildXYZ <!CONTEXTUAL_EFFECT_WARNING(setValX call mismatch: expected EXACTLY_ONCE, actual UNKNOWN), CONTEXTUAL_EFFECT_WARNING(setDefaultValY call mismatch: expected AT_MOST_ONCE, actual UNKNOWN), CONTEXTUAL_EFFECT_WARNING(setVarZ call mismatch: expected AT_LEAST_ONCE, actual UNKNOWN)!>{
        do {
            setValX()
            setDefaultValY()
            setVarZ()
        } while (b)
    }<!>
}

fun test_3() {
    buildXYZ {
        setValX()
        setVarZ()
        for (i in 1..2) {
            setVarZ()
        }
    }
}