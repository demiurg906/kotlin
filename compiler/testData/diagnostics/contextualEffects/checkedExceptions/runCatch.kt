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

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: NullPointerException)!>fun test_2()<!> {
    myRun {
        myCatchIOException {
            throwsNullPointerException()
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

<!CONTEXTUAL_EFFECT_WARNING(Unchecked exception: NullPointerException)!>fun test_5()<!> {
    myCatchIOException {
        myRun {
            myCatchIOException {
                myRun {
                    throwsNullPointerException()
                }
            }
        }
    }
}