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
    val x = 10
    build <!CONTEXTUAL_EFFECT_WARNING(setZ call mismatch: expected AT_LEAST_ONCE, actual UNKNOWN)!>{
        when (x) {
            in 1..10 -> {
                for (i in 1..x) {
                    setZ(1)
                }
            }
            in 2..20 -> {
                setZ(1)
            }
            in 3..30 -> {
                for (i in 1..x) {
                    setZ(1)
                }
            }
            else -> {
                setZ(1)
                setZ(1)
            }
        }

        when (x) {
            in 1..10 -> setX(10)
            in 2..20 -> setX(20)
            else -> setX(30)
        }
    }<!>
}