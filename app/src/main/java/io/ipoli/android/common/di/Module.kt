package io.ipoli.android.common.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import io.ipoli.android.quest.calendar.DayViewPresenter
import io.ipoli.android.quest.persistence.QuestRepository
import io.ipoli.android.quest.persistence.RealmQuestRepository
import io.ipoli.android.quest.usecase.LoadScheduleForDateUseCase
import space.traversal.kapsule.HasModules
import space.traversal.kapsule.Injects
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/17.
 */
interface RepositoryModule {
    val questRepository: QuestRepository
}

class RealmRepositoryModule : RepositoryModule {
    override val questRepository = RealmQuestRepository()
}

interface AndroidModule {
    val layoutInflater: LayoutInflater

    val sharedPreferences: SharedPreferences
}

class MainAndroidModule(private val context: Context) : AndroidModule {

    override val layoutInflater: LayoutInflater get() = LayoutInflater.from(context)

    override val sharedPreferences: SharedPreferences get() = PreferenceManager.getDefaultSharedPreferences(context)
}

class MainUseCaseModule : UseCaseModule, Injects<Module> {
    private val questRepository by required { questRepository }
    override val loadScheduleForDateUseCase get() = LoadScheduleForDateUseCase(questRepository)
}

interface UseCaseModule {
    val loadScheduleForDateUseCase: LoadScheduleForDateUseCase
}

interface PresenterModule {
    val dayViewPresenter: DayViewPresenter
}

class AndroidPresenterModule : PresenterModule, Injects<Module> {
    private val loadScheduleForDateUseCase by required { loadScheduleForDateUseCase }
    override val dayViewPresenter get() = DayViewPresenter(loadScheduleForDateUseCase)
}

class Module(androidModule: AndroidModule,
             repositoryModule: RepositoryModule,
             useCaseModule: UseCaseModule,
             presenterModule: PresenterModule) :
    AndroidModule by androidModule,
    RepositoryModule by repositoryModule,
    UseCaseModule by useCaseModule,
    PresenterModule by presenterModule,
    HasModules {
    override val modules = setOf(androidModule, repositoryModule, useCaseModule, presenterModule)
}