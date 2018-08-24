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

inline fun myCatchIOException(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        provides(block, CatchesException<IOException>())
    }
    block()
}

inline fun myCatchRuntimeException(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        provides(block, CatchesException<RuntimeException>())
    }
    block()
}

inline fun myRun(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
}

// ---------------- TESTS ----------------

fun test_1() {
    myRun {
        myCatchIOException {
            throwsIOException()
        }
    }
}

fun test_2() {
    myRun {
        myCatchIOException {
            <!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: NullPointerException)!>throwsNullPointerException()<!>
        }
    }
}

fun test_3() {
    myRun {
        myCatchIOException {
            myRun {
                throwsIOException()
            }
        }
    }
}

fun test_4() {
    myCatchRuntimeException {
        myRun {
            myCatchIOException {
                myRun {
                    throwsNullPointerException()
                }
            }
        }
    }
}

fun test_5() {
    myCatchIOException {
        myRun {
            myCatchIOException {
                myRun {
                    <!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: NullPointerException)!>throwsNullPointerException()<!>
                }
            }
        }
    }
}