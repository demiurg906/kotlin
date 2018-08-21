// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER -INVISIBLE_REFERENCE
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.RuntimeException

fun throwsFileNotFoundException() {
    contract {
        supplies(ExceptionEffectDescription<FileNotFoundException>())
    }
    throw FileNotFoundException()
}

fun throwsNullPointerException() {
    contract {
        supplies(ExceptionEffectDescription<NullPointerException>())
    }
    throw NullPointerException()
}

fun throwsIOException() {
    contract {
        supplies(ExceptionEffectDescription<IOException>())
    }
    throw java.io.IOException()
}

inline fun myCatchIOException(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        consumes(block, ExceptionEffectDescription<IOException>())
    }
    block()
}

inline fun myCatchRuntimeException(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        consumes(block, ExceptionEffectDescription<RuntimeException>())
    }
    block()
}

// ---------------- TESTS ----------------

fun test_1() {
    for (i in 1..10) {
        myCatchIOException {
            throwsIOException()
        }
    }
}

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: NullPointerException)!>fun test_2()<!> {
    val b = false
    if (b) {
        for (i in 1..10) {
            myCatchIOException {
                throwsIOException()
            }
        }
    } else {
        for (i in 1..10) {
            throwsNullPointerException()
        }
    }
}

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: IOException)!>fun test_3()<!> {
    val b = false
    for (i in 1..10) {
        if (b) {
            myCatchIOException {
                throwsIOException()
            }
        } else {
            throwsIOException()
        }
    }
}

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: NullPointerException)!>fun test_4()<!> {
    val b = false
    for (i in 1..10) {
        if (b) {
            myCatchIOException {
                throwsNullPointerException()
            }
        }
    }
}