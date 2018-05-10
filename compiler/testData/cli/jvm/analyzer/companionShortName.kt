open class Bar {
    companion object {
        class FooBarCompanion
    }
}

class Foo : Bar() {
    fun foo(): FooBarCompanion = TODO()
}
