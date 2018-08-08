// !LANGUAGE: +ContextualEffects

fun foo() {
    // contract { consumes Exception("aaa") }
    bar()
}

fun bar() {
    // contract { supplies Exception("aaa") }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad()<!> {
    if (false) {
        bar()
    }
}

