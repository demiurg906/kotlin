// !LANGUAGE: +ContextualEffects

// In that case we don't check inner functions declarations

fun supplier() {
    // contract { supplies Exception("Exception") }
}

fun consumerA() {
    // contract { consumes Exception("Exception") }
    val func = { supplier() }
    func()
}

fun badA() {
    val func = { supplier() }
    func()
}

fun consumerB() {
    val x = 1
    x.run {
        supplier()
    }
}

fun badB() {
    val x = 1
    x.run {
        supplier()
    }
}