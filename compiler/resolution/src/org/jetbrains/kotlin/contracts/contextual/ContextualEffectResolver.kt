/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual

// Интерфейс парсера PSI, который по декларации эффекта
//  создает корректные инстансы соответсвующих supplier'ов
//  и consumer'ов, согласно тем параметрам, которые описал
//  пользователь в контракте
interface ContextualEffectResolver {
    // Тут идут функции для парсинга PSI, о структуре которого я пока
    //  не сильно много знаю
}