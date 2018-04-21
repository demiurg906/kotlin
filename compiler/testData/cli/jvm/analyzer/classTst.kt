interface I1

interface I2

interface I3 : I2

open class A : I1

class B(val x: Int, y: Int) : A(), I3 {
    val z: Int
    var a: String? = null

    val aU: Int?
        get() = a?.length

    init {
        z = y
    }

    companion object {
        val x: Int = 10
    }

    constructor() {
        a = "a"
        this(10, 10)
    }

    fun foo(arg: Int): Int {
        return z + arg
    }
}