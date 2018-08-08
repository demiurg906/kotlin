// !LANGUAGE: +ContextualEffects

fun foo() {
    // contract { consumes Exception("aaa") }
    for (x in 1..10) {
        bar()
    }
}

fun bar() {
    // contract { supplies Exception("aaa") }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad()<!> {
    for (x in 1..10) {
        bar()
    }
}