// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions

import kotlin.internal.contracts.*

@Suppress("INVISIBLE_MEMBER")
fun supplier() {
    contract {
        supplies(ExceptionEffectDescription("RuntimeException"))
    }
    throw java.lang.RuntimeException()
}

@Suppress("INVISIBLE_MEMBER")
fun good() {
    contract {
        consumes(ExceptionEffectDescription("RuntimeException"))
    }
    supplier()
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad()<!> {
    supplier()
}

