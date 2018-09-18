package io.ipoli.android.player.attribute

import io.ipoli.android.player.data.Player
import org.amshove.kluent.`should be`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 09/18/2018.
 */
class AttributeRankSpek : Spek({

    describe("AttributeRank") {

        it("should be Novice when level is 1 and Player is Novice") {
            AttributeRank.of(1, Player.Rank.NOVICE).`should be`(Player.Rank.NOVICE)
        }

        it("should be Novice when level is 1 and Player is Apprentice") {
            AttributeRank.of(1, Player.Rank.APPRENTICE).`should be`(Player.Rank.NOVICE)
        }

        it("should be Apprentice when level is 10 and Player is Apprentice") {
            AttributeRank.of(10, Player.Rank.APPRENTICE).`should be`(Player.Rank.APPRENTICE)
        }

        it("should be Apprentice when level is 30 and Player is Apprentice") {
            AttributeRank.of(30, Player.Rank.APPRENTICE).`should be`(Player.Rank.ADEPT)
        }

        it("should be Adept when level is 20 and Player is Apprentice") {
            AttributeRank.of(20, Player.Rank.APPRENTICE).`should be`(Player.Rank.ADEPT)
        }

        it("should be Divinity when level is 100 and Player is Titan") {
            AttributeRank.of(100, Player.Rank.TITAN).`should be`(Player.Rank.DIVINITY)
        }

        it("should be Divinity when level is 100 and Player is Divinity") {
            AttributeRank.of(100, Player.Rank.DIVINITY).`should be`(Player.Rank.DIVINITY)
        }
    }
})