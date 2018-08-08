// !LANGUAGE: +ContextualEffects

fun supplier() {
    // contract { supplies Exception("Exception") }
}

// good
fun consumer() {
    // contract { consumes Exception("Exception") }
    for (x in 1..10) {
        supplier()
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad()<!> {
    for (x in 1..10) {
        supplier()
    }
}
