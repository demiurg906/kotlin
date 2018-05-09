open class Bar {
    companion object {
        class FooBarCompanion
    }
}

//class A {}

class Foo : Bar() {
    fun foo(): Bar.Companion.FooBarCompanion = TODO()
}
