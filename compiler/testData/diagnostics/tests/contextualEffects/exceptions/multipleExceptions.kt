// !LANGUAGE: +ContextualEffects

fun supplier_AAA() {}

fun supplier_BBB() {}

fun supplier_AAA_BBB_CCC() {}

// good
fun consumer_AAA_BBB_CCC() {
    supplier_AAA()
    supplier_BBB()
    supplier_AAA_BBB_CCC()
}

// good
fun consumer1_AAA_BBB() {
    supplier_AAA()
    supplier_BBB()
}

// bad
<!CONTEXTUAL_EFFECT_WARNING!>fun consumer2_AAA_BBB()<!> {
    supplier_AAA_BBB_CCC()
}

<!CONTEXTUAL_EFFECT_WARNING, CONTEXTUAL_EFFECT_WARNING, CONTEXTUAL_EFFECT_WARNING!>fun bad()<!> {
    supplier_AAA_BBB_CCC()
}