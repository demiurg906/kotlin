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
    myCatchRuntimeException {
        throwsIOException()
    }
}

fun test_2() {
    myCatchIOException {
        myCatchRuntimeException {
            throwsIOException()
        }
    }
}

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: NullPointerException)!>fun test_3()<!> {
    myCatchIOException {
        throwsNullPointerException()
        myCatchRuntimeException {}
    }
}

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: NullPointerException)!>fun test_4()<!> {
    myCatchIOException {
        myCatchRuntimeException {}
        throwsNullPointerException()
    }
}

fun test_5() {
    myCatchIOException {
        throwsIOException()
        myCatchRuntimeException {}
    }
}

fun test_6() {
    myCatchIOException {
        myCatchRuntimeException {}
        throwsIOException()
    }
}