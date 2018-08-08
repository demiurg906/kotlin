// !LANGUAGE: +ContextualEffects

fun foo(cond: Boolean) {
    // contract { consumes Exception("aaa") }
    var b = cond
    do {
        if (b) {
            bar()
            break
        } else {
            bar()
        }
        b = true
    } while(!b)
}

fun bar() {
    // contract { supplies Exception("aaa") }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad(cond: Boolean)<!> {
    var b = cond
    do {
        if (b) {
            bar()
            break
        } else {
            bar()
        }
        b = true
    } while(!b)
}