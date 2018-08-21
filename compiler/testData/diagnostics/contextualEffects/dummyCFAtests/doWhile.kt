// !LANGUAGE: +ContextualEffects +AllowContractsForCustomFunctions
// !DIAGNOSTICS: -INVISIBLE_MEMBER

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
fun good_1(cond: Boolean) {
    contract {
        consumes(ExceptionEffectDescription<IOException>())
    }
    var b = cond
    do {
        if (b) {
            supplier()
            break
        } else {
            supplier()
        }
        b = true
    } while(!b)
}

<!CONTEXTUAL_EFFECT_WARNING!>fun bad_1(cond: Boolean)<!> {
    var b = cond
    do {
        if (b) {
            supplier()
            break
        } else {
            supplier()
        }
        b = true
    } while(!b)
}