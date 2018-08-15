// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER -INVISIBLE_REFERENCE
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.RuntimeException

class A {
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
        throw IOException()
    }
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

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: IOException), CONTEXTUAL_EFFECT_WARNING(Unchecked exception: NullPointerException)!>fun test_1()<!> {
    val a = A()
    a.throwsIOException()
    a.throwsNullPointerException()
}

fun test_2() {
    val a = A()
    myCatchIOException {
        a.throwsIOException()
    }
}

fun test_3() {
    val a = A()
    myCatchRuntimeException {
        a.throwsNullPointerException()
    }
}

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: IOException)!>fun test_4()<!> {
    val a = A()
    myCatchRuntimeException {
        a.throwsIOException()
    }
}