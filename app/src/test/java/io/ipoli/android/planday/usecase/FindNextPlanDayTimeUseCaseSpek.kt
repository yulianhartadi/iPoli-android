package io.ipoli.android.planday.usecase

import io.ipoli.android.TestUtil
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.player.Player
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 05/18/2018.
 */
class FindNextPlanDayTimeUseCaseSpek : Spek({

    describe("FindNextPlanDayTimeUseCase") {

        fun executeUseCase(player: Player? = TestUtil.player()) =
            FindNextPlanDayTimeUseCase(TestUtil.playerRepoMock(player)).execute()

        fun executeUseCase(
            days: Set<DayOfWeek>,
            time: Time,
            params: FindNextPlanDayTimeUseCase.Params
        ) =
            FindNextPlanDayTimeUseCase(
                TestUtil.playerRepoMock(
                    TestUtil.player().copy(
                        preferences = TestUtil.player().preferences.copy(
                            planDays = days,
                            planDayTime = time
                        )
                    )
                )
            ).execute(params)

        it("should require existing Player") {
            val exec = { executeUseCase(null) }
            exec shouldThrow IllegalArgumentException::class
        }

        it("should return null when Player hasn't set plan days") {
            executeUseCase(
                setOf(),
                Time.now(),
                FindNextPlanDayTimeUseCase.Params()
            ).`should be null`()
        }

        it("should schedule for now") {
            val today = LocalDate.now().with(DayOfWeek.MONDAY)
            val time = Time.atHours(10)

            val params = FindNextPlanDayTimeUseCase.Params(today, time)
            val actual = executeUseCase(setOf(DayOfWeek.MONDAY), time, params)

            val expected = LocalDateTime.of(
                today,
                LocalTime.of(time.hours, time.getMinutes())
            )

            actual.`should equal`(expected)
        }

        it("should schedule in a minute") {
            val today = LocalDate.now().with(DayOfWeek.MONDAY)
            val time = Time.atHours(10)

            val params = FindNextPlanDayTimeUseCase.Params(today, time)
            val actual = executeUseCase(setOf(DayOfWeek.MONDAY), time.plus(1), params)

            val expected = LocalDateTime.of(
                today,
                LocalTime.of(time.hours, time.getMinutes() + 1)
            )

            actual.`should equal`(expected)
        }

        it("should schedule for Friday") {
            val today = LocalDate.now().with(DayOfWeek.MONDAY)
            val time = Time.atHours(10)

            val params = FindNextPlanDayTimeUseCase.Params(today, time)
            val actual = executeUseCase(setOf(DayOfWeek.FRIDAY), time, params)

            val expected = LocalDateTime.of(
                today.with(DayOfWeek.FRIDAY),
                LocalTime.of(time.hours, time.getMinutes())
            )

            actual.`should equal`(expected)
        }

        it("should schedule for Monday") {
            val today = LocalDate.now().with(DayOfWeek.TUESDAY)
            val time = Time.atHours(10)

            val params = FindNextPlanDayTimeUseCase.Params(today, time)
            val actual = executeUseCase(setOf(DayOfWeek.MONDAY), time, params)

            val expected = LocalDateTime.of(
                today.plusDays(6),
                LocalTime.of(time.hours, time.getMinutes())
            )

            actual.`should equal`(expected)
        }

        it("should schedule for next date when today time has passed") {
            val today = LocalDate.now().with(DayOfWeek.MONDAY)
            val time = Time.atHours(9)

            val params = FindNextPlanDayTimeUseCase.Params(today, Time.atHours(10))
            val actual = executeUseCase(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), time, params)

            val expected = LocalDateTime.of(
                today.plusDays(1),
                LocalTime.of(time.hours, time.getMinutes())
            )

            actual.`should equal`(expected)
        }

    }
})