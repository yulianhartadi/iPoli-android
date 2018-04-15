package io.ipoli.android.challenge.usecase

import io.ipoli.android.TestUtil
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import org.amshove.kluent.mock
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/09/2018.
 */
class SaveChallengeUseCaseSpek : Spek({

    describe("SaveChallengeUseCase") {

        it("should not accept Challenge without name") {
            val exec =
                {
                    SaveChallengeUseCase(
                        TestUtil.challengeRepoMock(),
                        mock()
                    ).execute(
                        SaveChallengeUseCase.Params.WithExistingQuests(
                            name = "",
                            tags = emptyList(),
                            color = Color.BLUE,
                            icon = Icon.STAR,
                            difficulty = Challenge.Difficulty.NORMAL,
                            end = LocalDate.now(),
                            allQuests = listOf(),
                            selectedQuestIds = setOf(),
                            motivations = listOf()
                        )
                    )
                }
            exec shouldThrow IllegalArgumentException::class
        }
    }
})