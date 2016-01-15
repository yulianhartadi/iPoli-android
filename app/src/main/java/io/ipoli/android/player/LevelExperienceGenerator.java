package io.ipoli.android.player;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class LevelExperienceGenerator {

    private static final int SKIP_FIB_NUMBER = 2;

    public static int experienceForLevel(int level) {
        return fibNumber(level + SKIP_FIB_NUMBER) * 10;
    }

    private static int fibNumber(int n) {
        int x = 0, y = 1, z = 1;
        for (int i = 0; i < n; i++) {
            x = y;
            y = z;
            z = x + y;
        }
        return x;
    }
}
