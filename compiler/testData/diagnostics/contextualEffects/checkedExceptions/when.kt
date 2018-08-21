// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER -INVISIBLE_REFERENCE -UNUSED_VARIABLE
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
    throw IOException()
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
    val x: Int = 10
    when (x) {
        in 0..3 -> myCatchIOException {
            throwsIOException()
        }
        in 5..7 -> myCatchRuntimeException {
            throwsIOException()
        }
        else -> throwsNullPointerException()
    }
}

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: IOException), CONTEXTUAL_EFFECT_WARNING(Unchecked exception: NullPointerException)!>fun test_2()<!> {
    val x: Int = 10
    throwsNullPointerException()
    when (x) {
        in 0..3 -> myCatchIOException {
            throwsFileNotFoundException()
        }
        in 5..7 -> myCatchRuntimeException {
            throwsIOException()
        }
        else -> {
            val y = x
        }
    }
}