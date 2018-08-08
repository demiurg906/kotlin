// !LANGUAGE: +ContextualEffects

fun supplier() {
    // contract { supplies Exception("Exception") }
}

// good
fun consumer() {
    // contract { consumes Exception("Exception") }
    supplier()
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad()<!> {
    supplier()
}