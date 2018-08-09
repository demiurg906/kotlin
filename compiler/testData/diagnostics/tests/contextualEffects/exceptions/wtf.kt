// !LANGUAGE: +ContextualEffects

fun supplier() {
    // contract { supplies Exception("Exception") }
}

fun consume_rA() {
    // contract { consumes Exception("Exception") }
    val func = { supplier() }

    run(func)
//    func()
}

//fun badA() {
//    val func = { supplier() }
//    func()
//}
