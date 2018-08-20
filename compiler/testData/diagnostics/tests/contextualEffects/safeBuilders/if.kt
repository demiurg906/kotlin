// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions +UseReturnsEffect
// !DIAGNOSTICS: -INVISIBLE_MEMBER -INVISIBLE_REFERENCE
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.internal.contracts.*

data class A(val x: Int?, val y: Int?, val z: Int?)

class ABuilder {
    private var x_: Int? = null
    fun setX(value: Int) {
        contract {
            supplies(CallEffect(::setX))
        }
        x_ = value
    }

    private var y_: Int? = null
    fun setY(value: Int) {
        contract {
            supplies(CallEffect(::setY))
        }
        y_ = value
    }

    private var z_: Int? = null
    fun setZ(value: Int) {
        contract {
            supplies(CallEffect(::setZ))
        }
        z_ = value
    }

    fun buildA() = A(x_, y_, z_)
}

fun build(init: ABuilder.() -> Unit): A {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        consumes(init, RequiresCallEffect(ABuilder::setX, DslCallKind.EXACTLY_ONCE))
        consumes(init, RequiresCallEffect(ABuilder::setY, DslCallKind.AT_MOST_ONCE))
        consumes(init, RequiresCallEffect(ABuilder::setZ, DslCallKind.AT_LEAST_ONCE))
    }
    val builder = ABuilder()
    builder.init()
    return builder.buildA()
}


// ---------------- TESTS ----------------

fun test_1() {
    val b = false
    build {
        setZ(10)
        if (b) {
            setX(10)
        } else {
            setX(15)
        }
    }
}

<!CONTEXTUAL_EFFECT_WARNING(setX call mismatch: expected EXACTLY_ONCE, actual AT_MOST_ONCE), CONTEXTUAL_EFFECT_WARNING(setZ call mismatch: expected AT_LEAST_ONCE, actual AT_MOST_ONCE)!>fun test_2()<!> {
    val b = false
    build {
        if (b) {
            setX(10)
        } else {
            setZ(10)
        }
    }
}

<!CONTEXTUAL_EFFECT_WARNING(setX call mismatch: expected EXACTLY_ONCE, actual AT_LEAST_ONCE)!>fun test_3()<!> {
    val b = false
    build {
        setZ(15)
        if (b) {
            setX(1)
            if (b) {
                setX(1)
            } else {
                setZ(1)
            }
        } else {
            setX(1)
            setZ(1)
        }
    }
}