/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual.safebuilders

enum class CallKind {
    // <= 1 call
    AT_MOST_ONCE {
        override fun plus(other: CallKind): CallKind {
            throw AssertionError("never be invoked")
        }
    },

    // 0 calls
    ZERO {
        override fun plus(other: CallKind): CallKind {
            throw AssertionError("never be invoked")
        }
    },

    // == 1 call
    EXACTLY_ONCE {
        override fun plus(other: CallKind): CallKind {
            if (other == AT_MOST_ONCE) {
                throw AssertionError("never be invoked")
            }
            return AT_LEAST_ONCE
        }
    },

    // >= 1 call
    AT_LEAST_ONCE {
        override fun plus(other: CallKind): CallKind {
            if (other == AT_MOST_ONCE) {
                throw AssertionError("never be invoked")
            }
            return AT_LEAST_ONCE
        }
    };

    companion object {
        /**
         * Checks that actual kind satisfies expected kind
         *     |  0  | <=1 |  =1 | >=1 | <- actual
         *   0 |  ?  |  ?  |  ?  |  ?  |
         * <=1 |  +  |  +  |  +  |  -  |
         *  =1 |  -  |  -  |  +  |  -  |
         * >=1 |  -  |  -  |  +  |  +  |
         *  ^
         * expected
         */
        fun check(expected: CallKind, actual: CallKind): Boolean {
            assert(expected != ZERO)
            return actual == EXACTLY_ONCE ||
                    actual == expected ||
                    expected == AT_MOST_ONCE && actual == ZERO
        }
    }

    abstract operator fun plus(other: CallKind): CallKind
}