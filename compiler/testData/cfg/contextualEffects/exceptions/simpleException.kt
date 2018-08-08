// !LANGUAGE: +ContextualEffects

fun foo() {
    // contract { consumes Exception("aaa") }
    bar()
}

fun bar() {
    // contract { supplies Exception("aaa") }
}

fun bad() {
    bar()
}