// FIX: Rename to 'incorrectCatching'
package kotlin

class Result<T>(val value: T?) {
    fun getOrThrow(): T = value ?: throw AssertionError("")
}

fun <caret>incorrect() = Result("123")
