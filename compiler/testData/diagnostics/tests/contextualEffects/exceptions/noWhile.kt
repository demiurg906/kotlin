// !LANGUAGE: +ContextualEffects

fun foo() {
    // contract { consumes Exception("aaa") }
    var x = 10
    while (x < 0) {
        bar()
    }
}

fun bar() {
    // contract { supplies Exception("aaa") }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad()<!> {
    var x = 10
    while (x < 0) {
        bar()
    }
}