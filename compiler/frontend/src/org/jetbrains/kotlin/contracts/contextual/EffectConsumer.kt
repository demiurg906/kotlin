/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

import org.jetbrains.kotlin.resolve.BindingTrace
import kotlin.reflect.KClass

// базовый интерфейс для все consume'ов эффектов
// основная логика по обработке каждого конкретного типа эффектов
//   находится имменно здесь
interface EffectConsumer {
    // Флажок, который указывает на то, удовлетворен ли consumer
    // теми эффектами, которые в него пришли
    val satisfied: Boolean

    val effectClazz: KClass<out ContextualEffect>

    // consumer получает текущий контекст и обрабатывает все
    //   эффекты из него, которые может обработать (вся информация записывается
    //   в контекст в метаинформацию, связанную с каждым эффектом)
    fun consume(context: BindingTrace) {
//        context.giveEffectsByType(effectClazz)
//            .forEach { context[it] = consumeOneEffect(it) }
    }

    // функция, которая съедает один эффект
    fun consumeOneEffect(effect: ContextualEffect)
}