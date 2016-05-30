package io.ipoli.android;

import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;

import io.ipoli.android.player.ExperienceForLevelGenerator;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/30/16.
 */
public class ExperienceForLevelGeneratorTest {
    private static ExperienceForLevelGenerator generator;

    @BeforeClass
    public static void setUp() {
        generator = new ExperienceForLevelGenerator();
    }

    @Test
    public void xpForLevel0() {
        assertEquals(generator.forLevel(0), BigInteger.ZERO);
    }

    @Test
    public void xpForLevel1() {
        assertEquals(generator.forLevel(1), new BigInteger("10"));
    }

    @Test
    public void xpForLevel2() {
        assertEquals(generator.forLevel(2), new BigInteger("30"));
    }

    @Test
    public void xpForLevel3() {
        assertEquals(generator.forLevel(3), new BigInteger("60"));
    }
}
