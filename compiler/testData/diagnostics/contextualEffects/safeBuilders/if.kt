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

fun test_1(b: Boolean) {
    buildXYZ {
        setVarZ()
        if (b) {
            setValX()
        } else {
            setValX()
        }
    }
}

fun test_2(b: Boolean) {
    buildXYZ <!CONTEXTUAL_EFFECT_WARNING(setValX call mismatch: expected EXACTLY_ONCE, actual AT_MOST_ONCE), CONTEXTUAL_EFFECT_WARNING(setVarZ call mismatch: expected AT_LEAST_ONCE, actual AT_MOST_ONCE)!>{
        if(b) {
            setValX()
        } else {
            setVarZ()
        }
    }<!>
}

fun test_3(b: Boolean) {
    buildXYZ <!CONTEXTUAL_EFFECT_WARNING(setValX call mismatch: expected EXACTLY_ONCE, actual AT_LEAST_ONCE)!>{
        setVarZ()
        if (b) {
            setValX()
            if (b) {
                setValX()
            } else {
                setVarZ()
            }
        } else {
            setValX()
            setVarZ()
        }
    }<!>
}