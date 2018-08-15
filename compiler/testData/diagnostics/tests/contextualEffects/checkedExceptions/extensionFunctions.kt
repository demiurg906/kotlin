// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER -INVISIBLE_REFERENCE
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.RuntimeException

inline fun myCatchIOException(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        consumes(block, ExceptionEffectDescription<IOException>())
    }
    block()
}

class A

fun A.throwsIOException() {
    contract {
        supplies(ExceptionEffectDescription<IOException>())
    }
    throw IOException()
}

// ---------------- TESTS ----------------

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: IOException)!>fun test_1()<!> {
    val a = A()
    a.throwsIOException()
}

fun test_2() {
    val a = A()
    myCatchIOException {
        a.throwsIOException()
    }
}