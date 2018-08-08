// !LANGUAGE: +ContextualEffects

fun foo(y: Int) {
    // contract { consumes Exception("aaa") }
    var x = y
    while (x < 10) {
        bar()
        x += 1
    }
}

fun bar() {
    // contract { supplies Exception("aaa") }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad(y: Int)<!> {
    var x = y
    while (x < 10) {
        bar()
        x += 1
    }
}