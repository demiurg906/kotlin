// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER -INVISIBLE_REFERENCE
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.RuntimeException

fun throwsFileNotFoundException() {
    contract {
        requires(CatchesException<FileNotFoundException>())
    }
    throw FileNotFoundException()
}

fun throwsNullPointerException() {
    contract {
        requires(CatchesException<NullPointerException>())
    }
    throw NullPointerException()
}

fun throwsIOException() {
    contract {
        requires(CatchesException<IOException>())
    }
    throw java.io.IOException()
}

inline fun myCatch(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        provides(block, CatchesException<FileNotFoundException>())
        provides(block, CatchesException<RuntimeException>())
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

fun test_4() {
    myCatch {
        <!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: IOException)!>throwsIOException()<!>
    }
}