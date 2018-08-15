// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions +UseReturnsEffect
// !DIAGNOSTICS: -INVISIBLE_MEMBER -INVISIBLE_REFERENCE

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
    myCatchIOException {
        throwsIOException()
    }
}

<!CONTEXTUAL_EFFECT_WARNING!>fun test_2()<!> {
    throwsIOException()
}