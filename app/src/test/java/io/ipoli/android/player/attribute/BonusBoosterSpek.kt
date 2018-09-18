package io.ipoli.android.player.attribute

import io.ipoli.android.TestUtil
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.player.data.Player
import io.ipoli.android.quest.TimeRange
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

class BonusBoosterSpek : Spek({

    fun playerWithAttributes(
        op: (MutableMap<Player.AttributeType, Player.Attribute>) -> Unit
    ): Player {
        val attrs = TestUtil.player.attributes.toMutableMap()
        op(attrs)
        return TestUtil.player.copy(
            attributes = attrs
        )
    }

    describe("IntelligenceBooster") {

        it("should reward 20% bonus XP & Coins when Player has apprentice status & intelligence is at level 10") {

            val q = TestUtil.quest.copy(
                scheduledDate = LocalDate.now(),
                startTime = Time.now(),
                completedAtDate = LocalDate.now(),
                completedAtTime = Time.now()
            )

            val p = playerWithAttributes {
                it[Player.AttributeType.INTELLIGENCE] =
                    it[Player.AttributeType.INTELLIGENCE]!!.copy(
                        level = 10
                    )
            }

            val booster = IntelligenceBooster().forQuest(q, p, Player.Rank.APPRENTICE)
            booster.experiencePercentage.`should be`(20)
            booster.coinsPercentage.`should be`(20)
        }

        it("should not reward 20% bonus XP & Coins when Quest is not scheduled") {

            val q = TestUtil.quest.copy(
                completedAtDate = LocalDate.now(),
                completedAtTime = Time.now()
            )

            val p = playerWithAttributes {
                it[Player.AttributeType.INTELLIGENCE] =
                    it[Player.AttributeType.INTELLIGENCE]!!.copy(
                        level = 10
                    )
            }

            val booster = IntelligenceBooster().forQuest(q, p, Player.Rank.APPRENTICE)
            booster.experiencePercentage.`should be`(0)
            booster.coinsPercentage.`should be`(0)
        }

        it("should not reward 20% bonus XP & Coins when Quest is not complete within 2 hours of end time") {

            val q = TestUtil.quest.copy(
                scheduledDate = LocalDate.now(),
                startTime = Time.atHours(9),
                duration = 60,
                completedAtDate = LocalDate.now(),
                completedAtTime = Time.atHours(13)
            )

            val p = playerWithAttributes {
                it[Player.AttributeType.INTELLIGENCE] =
                    it[Player.AttributeType.INTELLIGENCE]!!.copy(
                        level = 10
                    )
            }

            val booster = IntelligenceBooster().forQuest(q, p, Player.Rank.APPRENTICE)
            booster.experiencePercentage.`should be`(0)
            booster.coinsPercentage.`should be`(0)
        }

        it("should reward 20% bonus XP & Coins when Player has novice status & intelligence is at level 10") {

            val q = TestUtil.quest.copy(
                scheduledDate = LocalDate.now(),
                startTime = Time.now(),
                completedAtDate = LocalDate.now(),
                completedAtTime = Time.now()
            )

            val p = playerWithAttributes {
                it[Player.AttributeType.INTELLIGENCE] =
                    it[Player.AttributeType.INTELLIGENCE]!!.copy(
                        level = 10
                    )
            }

            val booster = IntelligenceBooster().forQuest(q, p, Player.Rank.NOVICE)
            booster.experiencePercentage.`should be`(20)
            booster.coinsPercentage.`should be`(20)
        }

        it("should reward 120% XP bonus when timer is used") {
            val q = TestUtil.quest.copy(
                scheduledDate = LocalDate.now(),
                startTime = Time.atHours(9),
                duration = 60,
                timeRanges = listOf(TimeRange(TimeRange.Type.POMODORO_WORK, 30)),
                completedAtDate = LocalDate.now(),
                completedAtTime = Time.atHours(12)
            )

            val p = playerWithAttributes {
                it[Player.AttributeType.INTELLIGENCE] =
                    it[Player.AttributeType.INTELLIGENCE]!!.copy(
                        level = 20
                    )
            }

            val booster = IntelligenceBooster().forQuest(q, p, Player.Rank.ADEPT)
            booster.experiencePercentage.`should be`(120)
        }

        it("should reward 100% XP bonus when timer is used and Quest is not completed within 2 hours of end time") {
            val q = TestUtil.quest.copy(
                scheduledDate = LocalDate.now(),
                startTime = Time.atHours(9),
                duration = 60,
                timeRanges = listOf(TimeRange(TimeRange.Type.POMODORO_WORK, 30)),
                completedAtDate = LocalDate.now(),
                completedAtTime = Time.atHours(15)
            )

            val p = playerWithAttributes {
                it[Player.AttributeType.INTELLIGENCE] =
                    it[Player.AttributeType.INTELLIGENCE]!!.copy(
                        level = 20
                    )
            }

            val booster = IntelligenceBooster().forQuest(q, p, Player.Rank.ADEPT)
            booster.experiencePercentage.`should be`(100)
        }

        it("should not reward adept level bonus when Intelligence is level 19") {
            val q = TestUtil.quest.copy(
                scheduledDate = LocalDate.now(),
                startTime = Time.now(),
                timeRanges = listOf(TimeRange(TimeRange.Type.POMODORO_WORK, 30)),
                completedAtDate = LocalDate.now(),
                completedAtTime = Time.now()
            )

            val p = playerWithAttributes {
                it[Player.AttributeType.INTELLIGENCE] =
                    it[Player.AttributeType.INTELLIGENCE]!!.copy(
                        level = 19
                    )
            }

            val booster = IntelligenceBooster().forQuest(q, p, Player.Rank.APPRENTICE)
            booster.experiencePercentage.`should be`(20)
        }

        it("should reward 20% XP bonus when timer is not used") {
            val q = TestUtil.quest.copy(
                scheduledDate = LocalDate.now(),
                startTime = Time.atHours(9),
                duration = 60,
                completedAtDate = LocalDate.now(),
                completedAtTime = Time.atHours(12)
            )

            val p = playerWithAttributes {
                it[Player.AttributeType.INTELLIGENCE] =
                    it[Player.AttributeType.INTELLIGENCE]!!.copy(
                        level = 20
                    )
            }

            val booster = IntelligenceBooster().forQuest(q, p, Player.Rank.ADEPT)
            booster.experiencePercentage.`should be`(20)
        }
    }

    describe("ExpertiseBooster") {
        it("should give 20% bonus XP & Coins when completing DC") {
            val p = playerWithAttributes {
                it[Player.AttributeType.EXPERTISE] =
                    it[Player.AttributeType.EXPERTISE]!!.copy(
                        level = 20
                    )
            }

            val booster = ExpertiseBooster().forDailyChallenge(p, Player.Rank.ADEPT)
            booster.experiencePercentage.`should be`(20)
            booster.coinsPercentage.`should be`(20)
        }

        it("should give 50% bonus XP & Coins when completing DC") {
            val p = playerWithAttributes {
                it[Player.AttributeType.EXPERTISE] =
                    it[Player.AttributeType.EXPERTISE]!!.copy(
                        level = 30
                    )
            }

            val booster = ExpertiseBooster().forDailyChallenge(p, Player.Rank.SPECIALIST)
            booster.experiencePercentage.`should be`(50)
            booster.coinsPercentage.`should be`(50)
        }
    }

    describe("WellBeingBooster") {
        it("should not give bonus when it is level 10") {
            val p = playerWithAttributes {
                it[Player.AttributeType.WELL_BEING] =
                    it[Player.AttributeType.WELL_BEING]!!.copy(
                        level = 10
                    )
            }

            val booster = WellBeingBooster().forQuest(TestUtil.quest, p, Player.Rank.NOVICE)
            booster.experiencePercentage.`should be`(0)
            booster.coinsPercentage.`should be`(0)
        }

        it("should give 5% bonus to all attributes") {
            val p = playerWithAttributes {
                it[Player.AttributeType.WELL_BEING] =
                    it[Player.AttributeType.WELL_BEING]!!.copy(
                        level = 10
                    )
            }

            val booster = WellBeingBooster().forQuest(TestUtil.quest, p, Player.Rank.APPRENTICE)
            booster.`should equal`(Booster.ofAllAttributeBonus(5))
        }

        it("should give 15% bonus to all attributes") {
            val p = playerWithAttributes {
                it[Player.AttributeType.WELL_BEING] =
                    it[Player.AttributeType.WELL_BEING]!!.copy(
                        level = 20
                    )
            }

            val booster = WellBeingBooster().forQuest(TestUtil.quest, p, Player.Rank.ADEPT)
            booster.`should equal`(Booster.ofAllAttributeBonus(15))
        }
    }

    describe("WillpowerBooster") {

        it("should not give bonus for completing Quest without RQ or Challenge") {

            val p = playerWithAttributes {
                it[Player.AttributeType.WILLPOWER] =
                    it[Player.AttributeType.WILLPOWER]!!.copy(
                        level = 10
                    )
            }

            val booster = WillpowerBooster().forQuest(TestUtil.quest, p, Player.Rank.NOVICE)
            booster.experiencePercentage.`should be`(0)
            booster.coinsPercentage.`should be`(0)
        }

        it("should give 5% bonus XP & Coins for completing RQ Quest") {
            val q = TestUtil.quest.copy(
                repeatingQuestId = "123"
            )

            val p = playerWithAttributes {
                it[Player.AttributeType.WILLPOWER] =
                    it[Player.AttributeType.WILLPOWER]!!.copy(
                        level = 10
                    )
            }

            val booster = WillpowerBooster().forQuest(q, p, Player.Rank.NOVICE)
            booster.experiencePercentage.`should be`(5)
            booster.coinsPercentage.`should be`(5)
        }

        it("should give 10% bonus XP & Coins for completing Challenge Quest") {
            val q = TestUtil.quest.copy(
                challengeId = "123"
            )

            val p = playerWithAttributes {
                it[Player.AttributeType.WILLPOWER] =
                    it[Player.AttributeType.WILLPOWER]!!.copy(
                        level = 10
                    )
            }

            val booster = WillpowerBooster().forQuest(q, p, Player.Rank.NOVICE)
            booster.experiencePercentage.`should be`(10)
            booster.coinsPercentage.`should be`(10)
        }
    }
})