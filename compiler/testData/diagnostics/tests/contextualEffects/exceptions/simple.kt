// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions

import kotlin.internal.contracts.*
import java.io.FileNotFoundException
import java.io.IOException

@Suppress("INVISIBLE_MEMBER")
fun supplier() {
    contract {
        supplies(ExceptionEffectDescription<FileNotFoundException>())
    }
    throw FileNotFoundException()
}

@Suppress("INVISIBLE_MEMBER")
fun good_1() {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    supplier()
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_1()<!> {
    supplier()
}