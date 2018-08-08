fun foo(b: Boolean) {
    // contract { consumes Exception("aaa") }
    if (b) {
        bar()
    } else {
        val x = 10
    }
}

fun bad(b: Boolean) {
    if (b) {
        bar()
    } else {
        val x = 10
    }
}

fun bar() {
    // contract { supplies Exception("aaa") }
}