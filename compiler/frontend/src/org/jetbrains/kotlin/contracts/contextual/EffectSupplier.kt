/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

// базовый интерфейс для всех supplier'ов эффектов
// логика всех supplier'ов крайне проста -- просто сделать эффект
// возможно можно даже в реализации не использовать никаких supplier'ов
// строчка `supplies MyEffect()` будет просто вызывать конструктор
interface EffectSupplier {
    fun supply(): ContextualEffect
}