// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions

import kotlin.internal.contracts.*
import java.io.FileNotFoundException

@Suppress("INVISIBLE_MEMBER")
fun supplier() {
    contract {
        supplies(ExceptionEffectDescription<FileNotFoundException>())
    }
    throw FileNotFoundException()
}

@Suppress("UNREACHABLE_CODE")
fun good_1() {
    while (true) {
        break
        supplier()
    }
}

@Suppress("UNREACHABLE_CODE")
fun good_2() {
    while (true) {
        continue
        supplier()
    }
}

@Suppress("UNREACHABLE_CODE")
fun good_3() {
    do {
        break
        supplier()
    } while (true)
}

@Suppress("UNREACHABLE_CODE")
fun good_4() {
    do {
        continue
        supplier()
    } while (true)
}
