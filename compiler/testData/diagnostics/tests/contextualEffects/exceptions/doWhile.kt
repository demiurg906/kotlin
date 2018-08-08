// !LANGUAGE: +ContextualEffects

fun supplier() {
    // contract { supplies Exception("Exception") }
}

// good
fun consumer(cond: Boolean) {
    // contract { consumes Exception("Exception") }
    var b = cond
    do {
        if (b) {
            supplier()
            break
        } else {
            supplier()
        }
        b = true
    } while(!b)
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad(cond: Boolean)<!> {
    var b = cond
    do {
        if (b) {
            supplier()
            break
        } else {
            supplier()
        }
        b = true
    } while(!b)
}
