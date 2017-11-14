package io.ipoli.android.player

import org.amshove.kluent.`should equal to`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/14/17.
 */
class ExperienceForLevelGeneratorSpek : Spek({
    describe("An ExperienceForLevelGenerator") {
        it("should return 0 experience required for level 1") {
            ExperienceForLevelGenerator.forLevel(1) `should equal to` 0
        }

        it("should return 50 experience required for level 2") {
            ExperienceForLevelGenerator.forLevel(2) `should equal to` 50
        }

        it("should return 360 experience required for level 6") {
            xpFromLastLevel(6) `should equal to` 360
        }

        it("should return 480 experience required for level 7") {
            xpFromLastLevel(7) `should equal to` 480
        }
    }
})

fun xpFromLastLevel(level: Int) =
    ExperienceForLevelGenerator.forLevel(level) - ExperienceForLevelGenerator.forLevel(level - 1)