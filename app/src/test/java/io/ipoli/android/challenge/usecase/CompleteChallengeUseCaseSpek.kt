package io.ipoli.android.challenge.usecase

import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.TestUtil
import io.ipoli.android.player.attribute.usecase.CheckForOneTimeBoostUseCase
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import org.amshove.kluent.`should be greater than`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.shouldNotBeNull
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/14/18.
 */
class CompleteChallengeUseCaseSpek : Spek({
    describe("CompleteChallengeUseCase") {

        fun executeUseCase(challengeId: String) =
            CompleteChallengeUseCase(
                TestUtil.challengeRepoMock(TestUtil.challenge),
                RewardPlayerUseCase(
                    TestUtil.playerRepoMock(TestUtil.player),
                    mock(),
                    mock(),
                    CheckForOneTimeBoostUseCase(mock()),
                    mock()
                ),
                TestUtil.playerRepoMock(TestUtil.player),
                mock()
            ).execute(
                CompleteChallengeUseCase.Params(
                    challengeId
                )
            )

        it("should fail when challengeId is empty") {

            val exec = { executeUseCase("") }
            exec `should throw` IllegalArgumentException::class
        }

        it("should mark challenge as completed") {
            val challenge = executeUseCase(TestUtil.challenge.id)
            challenge.completedAtTime.shouldNotBeNull()
            challenge.completedAtDate.shouldNotBeNull()
        }

        it("should have reward") {
            val challenge = executeUseCase(TestUtil.challenge.id)
            challenge.reward.shouldNotBeNull()
            challenge.reward!!.experience `should be greater than` 0
            challenge.reward!!.coins `should be greater than` 0
        }
    }
})