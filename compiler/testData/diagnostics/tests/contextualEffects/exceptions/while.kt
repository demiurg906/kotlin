// !LANGUAGE: +ContextualEffects

fun supplier() {
    // contract { supplies Exception("Exception") }
}

// good
fun consumer(y: Int) {
    // contract { consumes Exception("Exception") }
    var x = y
    while (x < 10) {
        supplier()
        x += 1
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad(y: Int)<!> {
    var x = y
    while (x < 10) {
        supplier()
        x += 1
    }
}
