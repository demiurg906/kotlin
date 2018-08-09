// !LANGUAGE: +ContextualEffects

fun supplier() {
    // contract { supplies Exception("Exception") }
}

// good
fun consumer() {
    // contract { consumes Exception("Exception") }
    var x = 0
    loop@ while (x > 0) {
        when (x) {
            1 -> supplier()
            2 -> break@loop
            3 -> return
        }
        x += 1
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad()<!> {
    var x = 0
    loop@ while (x > 0) {
        when (x) {
            1 -> supplier()
            2 -> break@loop
            3 -> return
        }
        x += 1
    }
}