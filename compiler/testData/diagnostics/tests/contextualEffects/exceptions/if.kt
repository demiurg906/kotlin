// !LANGUAGE: +ContextualEffects

fun supplier() {
    // contract { supplies Exception("Exception") }
}

// good
fun consumerA(b: Boolean) {
    // contract { consumes Exception("Exception") }
    if (b) {
        supplier()
    } else {
        val <!UNUSED_VARIABLE!>x<!> = 10
    }
}

// good
fun consumerB(b: Boolean) {
    // contract { consumes Exception("Exception") }
    if (b) {
        supplier()
    } else {
        supplier()
    }
}

// good
fun consumerC(b: Boolean) {
    // contract { consumes Exception("Exception") }
    if (b) {
        val <!UNUSED_VARIABLE!>x<!> = 10
    } else {
        supplier()
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun badA(b: Boolean)<!> {
    if (b) {
        supplier()
    } else {
        val <!UNUSED_VARIABLE!>x<!> = 10
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun badB(b: Boolean)<!> {
    if (b) {
        supplier()
    } else {
        supplier()
    }
}

// good
<!CONTEXTUAL_EFFECT_WARNING!>fun badC(b: Boolean)<!> {
    if (b) {
        val <!UNUSED_VARIABLE!>x<!> = 10
    } else {
        supplier()
    }
}