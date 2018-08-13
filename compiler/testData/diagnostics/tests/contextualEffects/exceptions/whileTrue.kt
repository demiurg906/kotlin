// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER -UNREACHABLE_CODE

import kotlin.internal.contracts.*
import java.io.FileNotFoundException

fun supplier() {
    contract {
        supplies(ExceptionEffectDescription<FileNotFoundException>())
    }
    throw FileNotFoundException()
}

fun good_1() {
    while (true) {
        break
        supplier()
    }
}

fun good_2() {
    while (true) {
        continue
        supplier()
    }
}

fun good_3() {
    do {
        break
        supplier()
    } while (true)
}

fun good_4() {
    do {
        continue
        supplier()
    } while (true)
}
