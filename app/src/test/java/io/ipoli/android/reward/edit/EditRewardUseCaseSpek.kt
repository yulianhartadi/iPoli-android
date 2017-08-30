package io.ipoli.android.reward.edit

import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek
import org.junit.jupiter.api.Assertions

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/29/17.
 */
object EditRewardUseCaseSpek : SubjectSpek<EditRewardUseCase>({
    subject { EditRewardUseCase() }

    given("new use case") {
        on("execute") {
            it("should run fine") {
                Assertions.assertEquals(0, 0)
            }
        }
    }
})