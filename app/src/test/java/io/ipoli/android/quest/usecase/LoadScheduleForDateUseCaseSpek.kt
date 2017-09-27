package io.ipoli.android.quest.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.quest.persistence.QuestRepository
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldBeEmpty
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/27/17.
 */
object LoadScheduleForDateUseCaseSpek : Spek({
    setupRxJava()

    val today = LocalDate.now()

    it("should give empty schedule") {
        val repo = mock<QuestRepository> {
            on { listenForDate(any()) } doReturn Observable.just(listOf())
        }

        val schedule = scheduleFor(repo, today)

        schedule.scheduled.shouldBeEmpty()
        schedule.unscheduled.shouldBeEmpty()
    }

    it("should give schedule with 1 scheduled & 1 unscheduled quest") {

        val repo = mock<QuestRepository> {
            val quest = Quest("name", today)
            quest.startMinute = 10
            on { listenForDate(any()) } doReturn Observable.just(
                listOf(
                    quest,
                    Quest("unscheduled", today)
                )
            )
        }

        val schedule = scheduleFor(repo, today)

        schedule.scheduled.size `should be` 1
        schedule.unscheduled.size `should be` 1
    }
})

private fun scheduleFor(repo: QuestRepository, today: LocalDate) =
    LoadScheduleForDateUseCase(repo).execute(today).blockingIterable().first()

private fun setupRxJava() {
    RxAndroidPlugins.reset()
    RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

    RxJavaPlugins.reset()
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
    RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
}