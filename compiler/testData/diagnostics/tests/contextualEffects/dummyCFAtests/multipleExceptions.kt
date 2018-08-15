// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.ArithmeticException
import java.lang.IllegalArgumentException

fun supplier_AAA() {
    contract {
        supplies(ExceptionEffectDescription<FileNotFoundException>())
    }
    throw FileNotFoundException()
}

fun supplier_BBB() {
    contract {
        supplies(ExceptionEffectDescription<ArithmeticException>())
    }
    throw FileNotFoundException()
}

fun supplier_AAA_BBB_CCC() {
    contract {
        supplies(ExceptionEffectDescription<FileNotFoundException>())
        supplies(ExceptionEffectDescription<ArithmeticException>())
        supplies(ExceptionEffectDescription<IllegalArgumentException>())
    }
    throw FileNotFoundException()
}

fun good_1() {
    contract {
        consumes(ExceptionEffectDescription<FileNotFoundException>())
        consumes(ExceptionEffectDescription<ArithmeticException>())
        consumes(ExceptionEffectDescription<IllegalArgumentException>())
    }
    supplier_AAA()
    supplier_BBB()
    supplier_AAA_BBB_CCC()
}

fun good_2() {
    contract {
        consumes(ExceptionEffectDescription<FileNotFoundException>())
        consumes(ExceptionEffectDescription<ArithmeticException>())
    }
    supplier_AAA()
    supplier_AAA()
    supplier_BBB()
}

<!CONTEXTUAL_EFFECT_WARNING, CONTEXTUAL_EFFECT_WARNING, CONTEXTUAL_EFFECT_WARNING!>fun bad_1()<!> {
    supplier_AAA_BBB_CCC()
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_2()<!> {
    contract {
        consumes(ExceptionEffectDescription<FileNotFoundException>())
        consumes(ExceptionEffectDescription<ArithmeticException>())
    }
    supplier_AAA_BBB_CCC()
}