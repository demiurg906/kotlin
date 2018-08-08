// !LANGUAGE: +ContextualEffects

fun foo(b: Boolean) {
    // contract { consumes Exception("aaa") }
    if (b) {
        bar()
    } else {
        val <!UNUSED_VARIABLE!>x<!> = 10
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad(b: Boolean)<!> {
    if (b) {
        bar()
    } else {
        val <!UNUSED_VARIABLE!>x<!> = 10
    }
}

fun bar() {
    // contract { supplies Exception("aaa") }
}