// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER -INVISIBLE_REFERENCE
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.RuntimeException

fun throwsIOException() {
    contract {
        requires(CatchesException<IOException>())
    }
    throw IOException()
}

inline fun myCatchIOException(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        provides(block, CatchesException<IOException>())
    }
    block()
}

// ---------------- TESTS ----------------

fun test_1() {
    myCatchIOException {
        throwsIOException()
    }
}

fun test_2() {
    throwsIOException()
}