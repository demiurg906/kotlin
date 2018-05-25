fun foo() : Int = 1

fun boo(x: Int) = x + 2

fun baz(x: Int) {}

fun bar() {
    val x = 10 * 12 + boo(foo())
    baz(10)
}