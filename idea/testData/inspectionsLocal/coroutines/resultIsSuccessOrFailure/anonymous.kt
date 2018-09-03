package kotlin

class SuccessOrFailure<T>(val value: T?) {
    fun getOrThrow(): T = value ?: throw AssertionError("")
}

fun test() {
    val x = <caret>fun() = SuccessOrFailure("123")
}
