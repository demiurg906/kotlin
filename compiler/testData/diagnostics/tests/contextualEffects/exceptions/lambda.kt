// !LANGUAGE: +ContextualEffects

fun supplier() {
    // contract { supplies Exception("Exception") }
}

//
fun consumerA() {
    // contract { consumes Exception("Exception") }
    val func = { supplier() }
    func()
}

<!CONTEXTUAL_EFFECT_WARNING!>fun badA()<!> {
    val func = { supplier() }
    func()
}

fun consumerB() {
    val x = 1
    x.run {
        supplier()
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun badB()<!> {
    val x = 1
    x.run {
        supplier()
    }
}