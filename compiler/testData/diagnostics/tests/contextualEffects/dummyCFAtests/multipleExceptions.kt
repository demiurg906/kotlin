// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.ArithmeticException
import java.lang.IllegalArgumentException

fun throwsFileNotFoundException() {
    contract {
        supplies(ExceptionEffectDescription<FileNotFoundException>())
    }
    throw FileNotFoundException()
}

fun throwsArithmeticException() {
    contract {
        supplies(ExceptionEffectDescription<ArithmeticException>())
    }
    throw FileNotFoundException()
}

fun throwsAll() {
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
    throwsFileNotFoundException()
    throwsArithmeticException()
    throwsAll()
}

fun good_2() {
    contract {
        consumes(ExceptionEffectDescription<FileNotFoundException>())
        consumes(ExceptionEffectDescription<ArithmeticException>())
    }
    throwsFileNotFoundException()
    throwsFileNotFoundException()
    throwsArithmeticException()
}

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: ArithmeticException), CONTEXTUAL_EFFECT_WARNING(Unchecked exception: FileNotFoundException), CONTEXTUAL_EFFECT_WARNING(Unchecked exception: IllegalArgumentException)!>fun bad_1()<!> {
    throwsAll()
}

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: IllegalArgumentException)!>fun bad_2()<!> {
    contract {
        consumes(ExceptionEffectDescription<FileNotFoundException>())
        consumes(ExceptionEffectDescription<ArithmeticException>())
    }
    throwsAll()
}