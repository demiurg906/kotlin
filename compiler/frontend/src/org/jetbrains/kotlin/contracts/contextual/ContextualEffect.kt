package org.jetbrains.kotlin.contracts.contextual

import kotlin.reflect.KClass

// TODO: переписать на английском
// базовый интерфейс для всех контекстуальных эффектов
interface ContextualEffect {
    val effectClass: KClass<out ContextualEffect>
    // ссылки на supplier'а и consumer'а для этого эффекта
    val supplier: KClass<out EffectSupplier>
    val consumer: KClass<out EffectConsumer>

    // ссылка на объект решетки для данного типа эффектов
    // т.к. решетки -- чисто утильные классы без состояния,
    //    из можно сделать object'ами
    val lattice: EffectLattice<ContextualEffect>

    // сообщение об ошибке, которое выдаст компилятор
    // мб это будет не поле, а функция, мб в другом классе
    // но пока что так
    val warningMessage: String

    // функция, которая объединяет одинаковые эффекты
    // нужна для того, чтобы эффекты могли аккамулироваться
    // например, для подсчета количества вызовов какой-то функции
    // мб стоит добавить соглашение, что должна бросать какое-нибудь
    //  исключение, если на вход подается эффект, несвопадающий с данным
    fun combine(effect: ContextualEffect): ContextualEffect
}

