package io.ipoli.android.challenge.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.TestUtil
import io.ipoli.android.challenge.persistence.ChallengeRepository
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

        fun createChallengeRepository(): ChallengeRepository = mock {
            on { findById(any()) } doReturn
                TestUtil.challenge

        }

        fun executeUseCase(challengeId : String) =
            CompleteChallengeUseCase(
                createChallengeRepository(),
                TestUtil.playerRepoMock(TestUtil.player())
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
            val challenge = executeUseCase("123")
            challenge.completedAtTime.shouldNotBeNull()
            challenge.completedAtDate.shouldNotBeNull()
        }

        it("should have XP") {
            val challenge = executeUseCase("123")
            challenge.experience.shouldNotBeNull()
            challenge.experience!! `should be greater than` 0
        }

        it("should have coins") {
            val challenge = executeUseCase("123")
            challenge.coins.shouldNotBeNull()
            challenge.coins!! `should be greater than` 0
        }
    }
})