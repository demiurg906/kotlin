// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException

fun foo(): Int? {
    return null
}

@Suppress("INVISIBLE_MEMBER")
fun supplier() {
    contract {
        supplies(ExceptionEffectDescription<FileNotFoundException>())
    }
    throw FileNotFoundException()
}

@Suppress("INVISIBLE_MEMBER", "UNUSED_VARIABLE")
fun good_1() {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    val x = foo() ?: supplier()
}

@Suppress("INVISIBLE_MEMBER", "UNUSED_VARIABLE")
fun good_2() {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    val x = supplier() <!USELESS_ELVIS!>?: foo()<!>
}

<!CONTEXTUAL_EFFECT_WARNING!>@Suppress("UNUSED_VARIABLE")
fun bad_1()<!> {
    val x = foo() ?: supplier()
}

<!CONTEXTUAL_EFFECT_WARNING!>@Suppress("UNUSED_VARIABLE")
fun bad_2()<!> {
    val x = supplier() <!USELESS_ELVIS!>?: foo()<!>
}