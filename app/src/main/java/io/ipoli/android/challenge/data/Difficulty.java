package io.ipoli.android.challenge.data;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/23/16.
 */
public enum Difficulty {
    EASY(1), NORMAL(2), HARD(3), HELL(4);

    private final int value;

    Difficulty(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Difficulty getByValue(int value) {
        for (Difficulty difficulty : values()) {
            if (difficulty.getValue() == value) {
                return difficulty;
            }
        }

        throw new IllegalArgumentException("Difficulty value not found");
    }

    public static int getNameRes(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return R.string.difficulty_easy;
            case NORMAL:
                return R.string.difficulty_normal;
            case HARD:
                return R.string.difficulty_hard;
            default:
                return R.string.difficulty_hell;
        }
    }
}
