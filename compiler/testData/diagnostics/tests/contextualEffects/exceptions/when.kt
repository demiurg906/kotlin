// !LANGUAGE: +ContextualEffects

fun foo() {}

fun supplier() {
    // contract { supplies Exception("Exception") }
}

// good
fun consumerA() {
    // contract { consumes Exception("Exception") }
    val x: Int = 10
    when (x) {
        in 0..3 -> supplier()
        in 5..7 -> supplier()
        else -> supplier()
    }
}

// good
fun consumerB() {
    // contract { consumes Exception("Exception") }
    val x: Int = 10
    when (x) {
        in 0..3 -> supplier()
        in 5..7 -> supplier()
        else -> foo()
    }
}

// good
fun consumerC() {
    // contract { consumes Exception("Exception") }
    val x: Int = 10
    when (x) {
        in 0..3 -> foo()
        in 5..7 -> supplier()
        else -> foo()
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun badA()<!> {
    val x: Int = 10
    when (x) {
        in 0..3 -> supplier()
        in 5..7 -> supplier()
        else -> supplier()
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun badB()<!> {
    val x: Int = 10
    when (x) {
        in 0..3 -> supplier()
        in 5..7 -> supplier()
        else -> foo()
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun badC()<!> {
    val x: Int = 10
    when (x) {
        in 0..3 -> foo()
        in 5..7 -> supplier()
        else -> foo()
    }
}