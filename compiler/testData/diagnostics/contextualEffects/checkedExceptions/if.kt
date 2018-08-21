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

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: IOException)!>fun test_1()<!> {
    val b = false
    if (b) {
        myCatchIOException {
            throwsIOException()
        }
    } else {
        throwsIOException()
    }
}

fun test_2() {
    val b = false
    myCatchIOException {
        if (b) {
            throwsIOException()
        } else {
            throwsFileNotFoundException()
        }
    }
}

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: IOException), CONTEXTUAL_EFFECT_WARNING(Unchecked exception: NullPointerException)!>fun test_3()<!> {
    val b = false
    if(b) {
        if (b) {
            myCatchIOException {
                throwsNullPointerException()
            }
        }
    } else {
        myCatchRuntimeException {
            throwsIOException()
        }
    }
}