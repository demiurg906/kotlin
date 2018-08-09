// !LANGUAGE: +ContextualEffects

fun supplier_AAA() {
    // contract { supplies Exception("Exception") }
}

// good
fun consumer1() {
    while (true) {
        break
        <!UNREACHABLE_CODE!>supplier_AAA()<!>
    }
}

// good
fun consumer2() {
    while (true) {
        continue
        <!UNREACHABLE_CODE!>supplier_AAA()<!>
    }
}

// good
fun consumer3() {
    do {
        break
        <!UNREACHABLE_CODE!>supplier_AAA()<!>
    } while (<!UNREACHABLE_CODE!>true<!>)
}

// good
fun consumer4() {
    do {
        continue
        <!UNREACHABLE_CODE!>supplier_AAA()<!>
    } while (true)
}
