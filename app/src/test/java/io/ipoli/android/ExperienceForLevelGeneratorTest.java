package io.ipoli.android;

import org.junit.Test;

import java.math.BigInteger;

import io.ipoli.android.player.ExperienceForLevelGenerator;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/30/16.
 */
public class ExperienceForLevelGeneratorTest {

    @Test
    public void xpForLevel0() {
        assertEquals(ExperienceForLevelGenerator.forLevel(0), BigInteger.ZERO);
    }

    @Test
    public void xpForLevel1() {
        assertEquals(ExperienceForLevelGenerator.forLevel(1), BigInteger.ZERO);
    }

    @Test
    public void xpForLevel2() {
        assertEquals(ExperienceForLevelGenerator.forLevel(2), new BigInteger("20"));
    }

    @Test
    public void xpForLevel3() {
        assertEquals(ExperienceForLevelGenerator.forLevel(3), new BigInteger("50"));
    }
}