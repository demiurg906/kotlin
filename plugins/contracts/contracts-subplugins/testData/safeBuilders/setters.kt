// !LANGUAGE: +ContextualEffects +UseCallsInPlaceEffect +AllowContractsForCustomFunctions +UseReturnsEffect
// !DIAGNOSTICS: -EXPERIMENTAL_API_USAGE_ERROR -DATA_CLASS_WITHOUT_PARAMETERS -CONTRACT_NOT_ALLOWED -NO_REFLECTION_IN_CLASS_PATH
// !RENDER_DIAGNOSTICS_MESSAGES

import kotlin.contracts.*
import org.jetbrains.kotlin.contracts.contextual.*
import org.jetbrains.kotlin.contracts.contextual.safebuilders.*

//data class X(val x: Int)
//
//class XBuilder {
//    var x: Int? = null
//    set(value) {
//        contract {
//            provides(Calls(::x.setter, this@XBuilder))
//        }
//        field = value
//    }
//
//    fun buildX() = X(x!!)
//}
//
//fun buildX(init: XBuilder.() -> Unit): X {
//    contract {
//        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
//        expectsTo(init, CallKind(XBuilder::x.setter, InvocationKind.EXACTLY_ONCE, receiverOf(init)))
//    }
//    val builder = XBuilder()
//    builder.init()
//    return builder.buildX()
//}
//
//fun test_1() {
//    buildX {
//        x = 10
//    }
//}

class Foo {
    var x: Int = 10
    set(value) = field = value
}

fun test_1() {
    val foo = Foo()
    foo.x = 11
}