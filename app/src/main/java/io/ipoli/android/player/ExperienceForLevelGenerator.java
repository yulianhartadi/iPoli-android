package io.ipoli.android.player;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/30/16.
 */
public class ExperienceForLevelGenerator {

    private static BigDecimal fibonacci(int num) {
        BigDecimal phi = new BigDecimal((1 + Math.sqrt(5)) / 2);
        return phi.pow(num).subtract(BigDecimal.ONE.subtract(phi).pow(num)).divide(new BigDecimal(Math.sqrt(5)), BigDecimal.ROUND_HALF_UP);
    }

    public static BigInteger forLevel(int level) {
        if (level <= 1) {
            return BigInteger.ZERO;
        }
        return (fibonacci(level).add(BigDecimal.ONE)).toBigInteger().multiply(new BigInteger("10")).add(forLevel(level - 1));
    }
}
