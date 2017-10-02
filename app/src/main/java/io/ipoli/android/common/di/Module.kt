package io.ipoli.android.common.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import com.bluelinelabs.conductor.Router
import io.ipoli.android.common.navigation.Navigator
import io.ipoli.android.quest.calendar.DayViewPresenter
import io.ipoli.android.quest.persistence.QuestRepository
import io.ipoli.android.quest.persistence.RealmQuestRepository
import io.ipoli.android.quest.usecase.AddQuestUseCase
import io.ipoli.android.quest.usecase.LoadScheduleForDateUseCase
import space.traversal.kapsule.HasModules
import space.traversal.kapsule.Injects
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
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

    val navigator: Navigator
}

class MainAndroidModule(private val context: Context, private val router: Router) : AndroidModule {

    override val layoutInflater: LayoutInflater get() = LayoutInflater.from(context)

    override val sharedPreferences: SharedPreferences get() = PreferenceManager.getDefaultSharedPreferences(context)

    override val navigator: Navigator get() = Navigator(router)
}

class MainUseCaseModule : UseCaseModule, Injects<Module> {
    private val questRepository by required { questRepository }
    override val loadScheduleForDateUseCase get() = LoadScheduleForDateUseCase(questRepository)
    override val addQuestUseCase get() = AddQuestUseCase(questRepository)
}

interface UseCaseModule {
    val loadScheduleForDateUseCase: LoadScheduleForDateUseCase
    val addQuestUseCase: AddQuestUseCase
}

interface PresenterModule {
    val dayViewPresenter: DayViewPresenter
}

class AndroidPresenterModule : PresenterModule, Injects<Module> {
    private val loadScheduleForDateUseCase by required { loadScheduleForDateUseCase }
    private val addQuestUseCase by required { addQuestUseCase }
    override val dayViewPresenter get() = DayViewPresenter(loadScheduleForDateUseCase, addQuestUseCase)
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