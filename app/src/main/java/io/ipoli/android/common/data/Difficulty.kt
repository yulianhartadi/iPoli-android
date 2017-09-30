package io.ipoli.android.common.data

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/23/17.
 */
enum class Difficulty private constructor(val value: Int) {
    EASY(1), NORMAL(2), HARD(3), HELL(4);

    companion object {

        fun getByValue(value: Int): Difficulty {
            values()
                .filter { it.value == value }
                .forEach { return it }

            throw IllegalArgumentException("Difficulty value not found")
        }
    }
}