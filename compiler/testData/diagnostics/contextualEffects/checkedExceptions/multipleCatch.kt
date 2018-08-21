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
    throw IOException()
}

inline fun myCatch(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        consumes(block, ExceptionEffectDescription<FileNotFoundException>())
        consumes(block, ExceptionEffectDescription<RuntimeException>())
    }
    block()
}

// ---------------- TESTS ----------------

fun test_1() {
    myCatch {
        throwsFileNotFoundException()
    }
}

fun test_2() {
    myCatch {
        throwsNullPointerException()
    }
}

fun test_3() {
    myCatch {
        throwsFileNotFoundException()
        throwsNullPointerException()
    }
}

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: IOException)!>fun test_4()<!> {
    myCatch {
        throwsIOException()
    }
}