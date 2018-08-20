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

fun simpleBuild(init: ABuilder.() -> Unit): A {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        consumes(init, RequiresCallEffect(ABuilder::setX, DslCallKind.EXACTLY_ONCE))
    }
    val builder = ABuilder()
    builder.init()
    return builder.buildA()
}


// ---------------- TESTS ----------------

fun test_1() {
    build {
        build {
            setX(10)
            setY(10)
            setZ(10)
            build {
                setX(10)
                setY(10)
                setZ(10)
            }
        }
        setX(10)
        setY(10)
        setZ(10)
        build {
            build {
                setX(10)
                setY(10)
                setZ(10)
            }
            setX(10)
            setY(10)
            setZ(10)
        }
    }
}

// TODO
fun test_2() {
    simpleBuild {
        setX(10)
        simpleBuild <!CONTEXTUAL_EFFECT_WARNING(setX call mismatch: expected EXACTLY_ONCE, actual ZERO)!>{  }<!>
    }
}

fun test_3() {
    simpleBuild {
        simpleBuild <!CONTEXTUAL_EFFECT_WARNING(setX call mismatch: expected EXACTLY_ONCE, actual ZERO)!>{  }<!>
        setX(10)
    }
}

fun test_4() {
    simpleBuild <!CONTEXTUAL_EFFECT_WARNING(setX call mismatch: expected EXACTLY_ONCE, actual ZERO)!>{
        simpleBuild {
            setX(10)
        }
    }<!>
}

// incorrect behavior, but that case is shit
// and hopefully no one will write code like this
fun test_5() {
    simpleBuild outer@ {
        setX(10)
        simpleBuild {
            this@outer.setX(15)
        }
    }
}

