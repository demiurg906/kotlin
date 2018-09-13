/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.description

/*
| or   | 0..0 | 0..1 | 1..1 | 1..* | 0..* |
| ---- | -----| ---- | ---- | ---- | ---- |
| 0..0 | 0..0 | 0..1 | 0..1 | 0..* | 0..* |
| 0..1 | 0..1 | 0..1 | 0..1 | 0..* | 0..* |
| 1..1 | 0..1 | 0..1 | 1..1 | 1..* | 0..* |
| 1..* | 0..* | 0..* | 1..* | 1..* | 0..* |
| 0..* | 0..* | 0..* | 0..* | 0..* | 0..* |

| or            | ZERO         | AT_MOST_ONCE | EXACTLY_ONCE  | AT_LEAST_ONCE | UNKNOWN |
| ------------- | ------------ | ------------ | ------------- | ------------- | ------- |
| ZERO          | ZERO         | AT_MOST_ONCE | AT_MOST_ONCE  | UNKNOWN       | UNKNOWN |
| AT_MOST_ONCE  | AT_MOST_ONCE | AT_MOST_ONCE | AT_MOST_ONCE  | UNKNOWN       | UNKNOWN |
| EXACTLY_ONCE  | AT_MOST_ONCE | AT_MOST_ONCE | EXACTLY_ONCE  | AT_LEAST_ONCE | UNKNOWN |
| AT_LEAST_ONCE | UNKNOWN      | UNKNOWN      | AT_LEAST_ONCE | AT_LEAST_ONCE | UNKNOWN |
| UNKNOWN       | UNKNOWN      | UNKNOWN      | UNKNOWN       | UNKNOWN       | UNKNOWN |


| combine | 0..0 | 0..1 | 1..1 | 1..* | 0..* |
| ------- | ---- | ---- | ---- | ---- | ---- |
| 0..0    | 0..0 | 0..1 | 1..1 | 1..* | 0..* |
| 0..1    | 0..1 | 0..* | 1..* | 1..* | 0..* |
| 1..1    | 1..1 | 1..* | 1..* | 1..* | 1..* |
| 1..*    | 1..* | 1..* | 1..* | 1..* | 1..* |
| 0..*    | 0..* | 0..* | 1..* | 1..* | 0..* |

| combine       | ZERO          | AT_MOST_ONCE  | EXACTLY_ONCE  | AT_LEAST_ONCE | UNKNOWN       |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| ZERO          | ZERO          | AT_MOST_ONCE  | EXACTLY_ONCE  | AT_LEAST_ONCE | UNKNOWN       |
| AT_MOST_ONCE  | AT_MOST_ONCE  | UNKNOWN       | AT_LEAST_ONCE | AT_LEAST_ONCE | UNKNOWN       |
| EXACTLY_ONCE  | EXACTLY_ONCE  | AT_LEAST_ONCE | AT_LEAST_ONCE | AT_LEAST_ONCE | AT_LEAST_ONCE |
| AT_LEAST_ONCE | AT_LEAST_ONCE | AT_LEAST_ONCE | AT_LEAST_ONCE | AT_LEAST_ONCE | AT_LEAST_ONCE |
| UNKNOWN       | UNKNOWN       | UNKNOWN       | AT_LEAST_ONCE | AT_LEAST_ONCE | UNKNOWN       |
*/

enum class InvocationKind {
    ZERO,          // 0..0
    AT_MOST_ONCE,  // 0..1
    EXACTLY_ONCE,  // 1..1
    AT_LEAST_ONCE, // 1..*
    UNKNOWN;       // 0..*

    companion object {
        fun or(x: InvocationKind, y: InvocationKind) = when (x) {
            ZERO -> when (y) {
                ZERO -> ZERO
                AT_MOST_ONCE -> AT_MOST_ONCE
                EXACTLY_ONCE -> AT_MOST_ONCE
                AT_LEAST_ONCE -> UNKNOWN
                UNKNOWN -> UNKNOWN
            }
            AT_MOST_ONCE -> when (y) {
                ZERO -> AT_MOST_ONCE
                AT_MOST_ONCE -> AT_MOST_ONCE
                EXACTLY_ONCE -> AT_MOST_ONCE
                AT_LEAST_ONCE -> UNKNOWN
                UNKNOWN -> UNKNOWN
            }
            EXACTLY_ONCE -> when (y) {
                ZERO -> AT_MOST_ONCE
                AT_MOST_ONCE -> AT_MOST_ONCE
                EXACTLY_ONCE -> EXACTLY_ONCE
                AT_LEAST_ONCE -> AT_LEAST_ONCE
                UNKNOWN -> UNKNOWN
            }
            AT_LEAST_ONCE -> when (y) {
                ZERO -> UNKNOWN
                AT_MOST_ONCE -> UNKNOWN
                EXACTLY_ONCE -> AT_LEAST_ONCE
                AT_LEAST_ONCE -> AT_LEAST_ONCE
                UNKNOWN -> UNKNOWN
            }
            UNKNOWN -> UNKNOWN
        }

        fun combine(x: InvocationKind, y: InvocationKind) = when (x) {
            ZERO -> when (y) {
                ZERO -> ZERO
                AT_MOST_ONCE -> AT_MOST_ONCE
                EXACTLY_ONCE -> EXACTLY_ONCE
                AT_LEAST_ONCE -> AT_LEAST_ONCE
                UNKNOWN -> UNKNOWN
            }
            AT_MOST_ONCE -> when (y) {
                ZERO -> AT_MOST_ONCE
                AT_MOST_ONCE -> UNKNOWN
                EXACTLY_ONCE -> AT_LEAST_ONCE
                AT_LEAST_ONCE -> AT_LEAST_ONCE
                UNKNOWN -> UNKNOWN
            }
            EXACTLY_ONCE -> when (y) {
                ZERO -> EXACTLY_ONCE
                else -> AT_LEAST_ONCE
            }
            AT_LEAST_ONCE -> AT_LEAST_ONCE
            UNKNOWN -> when (y) {
                EXACTLY_ONCE -> AT_LEAST_ONCE
                AT_LEAST_ONCE -> AT_LEAST_ONCE
                else -> UNKNOWN
            }
        }
    }

}

fun InvocationKind.isDefinitelyVisited(): Boolean = this == InvocationKind.EXACTLY_ONCE || this == InvocationKind.AT_LEAST_ONCE
fun InvocationKind.canBeRevisited(): Boolean = this == InvocationKind.UNKNOWN || this == InvocationKind.AT_LEAST_ONCE

