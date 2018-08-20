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

    fun buildInstance() = A(x_, y_, z_)
}

data class B(val x: Int?, val y: Int?)

class BBuilder {
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

    fun buildInstance() = B(x_, y_)
}

fun buildA(init: ABuilder.() -> Unit): A {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        consumes(init, RequiresCallEffect(ABuilder::setX, DslCallKind.EXACTLY_ONCE))
        consumes(init, RequiresCallEffect(ABuilder::setY, DslCallKind.AT_MOST_ONCE))
        consumes(init, RequiresCallEffect(ABuilder::setZ, DslCallKind.AT_LEAST_ONCE))
    }
    val builder = ABuilder()
    builder.init()
    return builder.buildInstance()
}

fun buildB(init: BBuilder.() -> Unit): B {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        consumes(init, RequiresCallEffect(BBuilder::setX, DslCallKind.EXACTLY_ONCE))
        consumes(init, RequiresCallEffect(BBuilder::setY, DslCallKind.AT_LEAST_ONCE))
    }
    val builder = BBuilder()
    builder.init()
    return builder.buildInstance()
}
// ---------------- TESTS ----------------

fun test_1() {
    buildA {
        setX(10)
        buildB {
            setX(10)
            setY(10)
            setY(10)
        }
        setZ(10)
    }
}

fun test_2() {
    buildA {
        setX(10)
        buildB {
            setX(10)
            setY(10)
            setY(10)
            setZ(10)
        }
    }
}

fun test_3() {
    buildA <!CONTEXTUAL_EFFECT_WARNING(setX call mismatch: expected EXACTLY_ONCE, actual ZERO)!>{
        setZ(10)
        buildB {
            setX(10)
            setY(10)
            setY(10)
        }

    }<!>
}

fun test_4() {
    buildA {
        buildB {
            this@buildA.setX(10) // setX in ABuilder
            setX(10) // setX in BBuilder
            setY(10) // setY in BBuilder
            setY(10)
            setZ(10) // setZ in ABuilder
        }
    }
}

fun test_5() {
    buildA {
        setZ(10)
        buildB <!CONTEXTUAL_EFFECT_WARNING(setX call mismatch: expected EXACTLY_ONCE, actual ZERO)!>{
            // no setX
            setY(10)
        }<!>
        setX(10)
    }
}

fun test_6() {
    buildB {
        setX(10)
        setY(10)
    }
    buildB <!CONTEXTUAL_EFFECT_WARNING(setX call mismatch: expected EXACTLY_ONCE, actual ZERO), CONTEXTUAL_EFFECT_WARNING(setY call mismatch: expected AT_LEAST_ONCE, actual ZERO)!>{  }<!>
}