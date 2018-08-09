// !LANGUAGE: +ContextualEffects

fun foo(): Int? {
    return null
}

fun supplier(): Int? {
    // contract { supplies Exception("Exception") }
    throw java.lang.Exception()
}

// good
fun consumerA() {
    // contract { consumes Exception("Exception") }
    val <!UNUSED_VARIABLE!>x<!> = foo() ?: supplier()
}

// good
fun consumerB() {
    // contract { consumes Exception("Exception") }
    val <!UNUSED_VARIABLE!>x<!> = supplier() ?: foo()
}

<!CONTEXTUAL_EFFECT_WARNING!>fun badA()<!> {
    val <!UNUSED_VARIABLE!>x<!> = foo() ?: supplier()
}

<!CONTEXTUAL_EFFECT_WARNING!>fun badB()<!> {
    val <!UNUSED_VARIABLE!>x<!> = supplier() ?: foo()
}