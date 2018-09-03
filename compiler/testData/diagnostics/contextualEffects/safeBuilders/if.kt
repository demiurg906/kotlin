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