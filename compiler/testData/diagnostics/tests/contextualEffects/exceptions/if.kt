// !LANGUAGE: +ContextualEffects

fun supplier() {
    // contract { supplies Exception("Exception") }
}

// good
fun consumer(b: Boolean) {
    // contract { consumes Exception("Exception") }
    if (b) {
        supplier()
    } else {
        val <!UNUSED_VARIABLE!>x<!> = 10
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad(b: Boolean)<!> {
    if (b) {
        supplier()
    } else {
        val <!UNUSED_VARIABLE!>x<!> = 10
    }
}
