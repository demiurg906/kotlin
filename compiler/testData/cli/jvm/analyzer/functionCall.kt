fun foo() : Int = 1

fun boo(x: Int) = x + 2

fun baz() {}

fun bar() {
    val x = 10 * 12 + boo(foo())
    baz()
}