package io.ipoli.android.common.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import com.bluelinelabs.conductor.Router
import io.ipoli.android.common.navigation.Navigator
import io.ipoli.android.quest.calendar.dayview.DayViewPresenter
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.data.persistence.RealmQuestRepository
import io.ipoli.android.quest.usecase.AddQuestUseCase
import io.ipoli.android.quest.usecase.LoadScheduleForDateUseCase
import io.ipoli.android.reminder.ui.formatter.ReminderTimeFormatter
import io.ipoli.android.reminder.ui.formatter.TimeUnitFormatter
import io.ipoli.android.reminder.ui.picker.ReminderPickerDialogPresenter
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

    val reminderTimeFormatter: ReminderTimeFormatter

    val timeUnitFormatter: TimeUnitFormatter
}

class MainAndroidModule(private val context: Context, private val router: Router) : AndroidModule {

    override val layoutInflater: LayoutInflater get() = LayoutInflater.from(context)

    override val sharedPreferences: SharedPreferences get() = PreferenceManager.getDefaultSharedPreferences(context)

    override val navigator get() = Navigator(router)

    override val reminderTimeFormatter get() = ReminderTimeFormatter(context)

    override val timeUnitFormatter get() = TimeUnitFormatter(context)

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
    val reminderPickerPresenter: ReminderPickerDialogPresenter
}

class AndroidPresenterModule : PresenterModule, Injects<Module> {
    private val loadScheduleForDateUseCase by required { loadScheduleForDateUseCase }
    private val addQuestUseCase by required { addQuestUseCase }
    private val reminderTimeFormatter by required { reminderTimeFormatter }
    private val timeUnitFormatter by required { timeUnitFormatter }
    override val dayViewPresenter get() = DayViewPresenter(loadScheduleForDateUseCase, addQuestUseCase)
    override val reminderPickerPresenter get() = ReminderPickerDialogPresenter(reminderTimeFormatter, timeUnitFormatter)
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