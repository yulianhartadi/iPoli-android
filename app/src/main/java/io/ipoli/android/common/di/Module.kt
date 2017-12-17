package io.ipoli.android.common.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import com.bluelinelabs.conductor.Router
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import io.ipoli.android.common.navigation.Navigator
import io.ipoli.android.common.text.CalendarFormatter
import io.ipoli.android.common.view.ColorPickerPresenter
import io.ipoli.android.common.view.IconPickerDialogPresenter
import io.ipoli.android.common.view.PetMessagePresenter
import io.ipoli.android.home.HomePresenter
import io.ipoli.android.pet.AndroidJobLowerPetStatsScheduler
import io.ipoli.android.pet.LowerPetStatsScheduler
import io.ipoli.android.pet.PetDialogPresenter
import io.ipoli.android.pet.PetPresenter
import io.ipoli.android.pet.store.PetStorePresenter
import io.ipoli.android.pet.usecase.*
import io.ipoli.android.player.AndroidLevelDownScheduler
import io.ipoli.android.player.AndroidLevelUpScheduler
import io.ipoli.android.player.LevelDownScheduler
import io.ipoli.android.player.LevelUpScheduler
import io.ipoli.android.player.persistence.CouchbasePlayerRepository
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.usecase.*
import io.ipoli.android.player.view.LevelUpPresenter
import io.ipoli.android.quest.AndroidJobQuestCompleteScheduler
import io.ipoli.android.quest.QuestCompleteScheduler
import io.ipoli.android.quest.calendar.CalendarPresenter
import io.ipoli.android.quest.calendar.addquest.AddQuestPresenter
import io.ipoli.android.quest.calendar.dayview.DayViewPresenter
import io.ipoli.android.quest.data.persistence.CouchbaseQuestRepository
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.usecase.*
import io.ipoli.android.quest.view.QuestCompletePresenter
import io.ipoli.android.reminder.AndroidJobReminderScheduler
import io.ipoli.android.reminder.ReminderScheduler
import io.ipoli.android.reminder.view.formatter.ReminderTimeFormatter
import io.ipoli.android.reminder.view.formatter.TimeUnitFormatter
import io.ipoli.android.reminder.view.picker.ReminderPickerDialogPresenter
import io.ipoli.android.theme.ThemeStorePresenter
import io.ipoli.android.theme.usecase.BuyThemeUseCase
import io.ipoli.android.theme.usecase.ChangeThemeUseCase
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import space.traversal.kapsule.HasModules
import space.traversal.kapsule.Injects
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/10/17.
 */
interface RepositoryModule {
    val questRepository: QuestRepository
    val playerRepository: PlayerRepository
}

class CouchbaseRepositoryModule : RepositoryModule, Injects<ControllerModule> {
    private val database by required { database }
    private val job by required { job }
    override val questRepository get() = CouchbaseQuestRepository(database, job + CommonPool)
    override val playerRepository get() = CouchbasePlayerRepository(database, job + CommonPool)
}

class CouchbaseJobRepositoryModule : RepositoryModule, Injects<SimpleModule> {
    private val database by required { database }
    private val job by required { job }
    override val questRepository get() = CouchbaseQuestRepository(database, job + CommonPool)
    override val playerRepository get() = CouchbasePlayerRepository(database, job + CommonPool)
}

interface AndroidModule {
    val layoutInflater: LayoutInflater

    val sharedPreferences: SharedPreferences

    val reminderTimeFormatter: ReminderTimeFormatter

    val timeUnitFormatter: TimeUnitFormatter

    val calendarFormatter: CalendarFormatter

    val database: Database

    val reminderScheduler: ReminderScheduler

    val questCompleteScheduler: QuestCompleteScheduler

    val levelUpScheduler: LevelUpScheduler

    val levelDownScheduler: LevelDownScheduler

    val lowerPetStatsScheduler: LowerPetStatsScheduler

    val job: Job
}

interface NavigationModule {
    val navigator: Navigator
}

class AndroidNavigationModule(private val router: Router?) : NavigationModule {
    override val navigator get() = Navigator(router)
}

class MainAndroidModule(private val context: Context) : AndroidModule {
    override val layoutInflater: LayoutInflater get() = LayoutInflater.from(context)

    override val sharedPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(context)

    override val reminderTimeFormatter get() = ReminderTimeFormatter(context)

    override val timeUnitFormatter get() = TimeUnitFormatter(context)

    override val calendarFormatter get() = CalendarFormatter(context)

    override val reminderScheduler get() = AndroidJobReminderScheduler()

    override val questCompleteScheduler get() = AndroidJobQuestCompleteScheduler()

    override val levelUpScheduler get() = AndroidLevelUpScheduler()

    override val levelDownScheduler get() = AndroidLevelDownScheduler()

    override val lowerPetStatsScheduler get() = AndroidJobLowerPetStatsScheduler()

    override val database: Database
        get() = Database("iPoli", DatabaseConfiguration(context.applicationContext))

    override val job get() = Job()
}

class MainUseCaseModule : UseCaseModule, Injects<ControllerModule> {
    private val questRepository by required { questRepository }
    private val playerRepository by required { playerRepository }
    private val reminderScheduler by required { reminderScheduler }
    private val questCompleteScheduler by required { questCompleteScheduler }
    private val levelUpScheduler by required { levelUpScheduler }
    private val levelDownScheduler by required { levelDownScheduler }
    override val loadScheduleForDateUseCase
        get() = LoadScheduleForDateUseCase(questRepository)
    override val saveQuestUseCase get() = SaveQuestUseCase(questRepository, reminderScheduler)
    override val removeQuestUseCase get() = RemoveQuestUseCase(questRepository, reminderScheduler)
    override val undoRemoveQuestUseCase get() = UndoRemovedQuestUseCase(questRepository)
    override val findQuestToRemindUseCase get() = FindQuestsToRemindUseCase(questRepository)
    override val snoozeQuestUseCase get() = SnoozeQuestUseCase(questRepository, reminderScheduler)
    override val completeQuestUseCase get() = CompleteQuestUseCase(questRepository, playerRepository, reminderScheduler, questCompleteScheduler, rewardPlayerUseCase)
    override val undoCompletedQuestUseCase get() = UndoCompletedQuestUseCase(questRepository, reminderScheduler, removeRewardFromPlayerUseCase)
    override val listenForPlayerChangesUseCase get() = ListenForPlayerChangesUseCase(playerRepository)
    override val rewardPlayerUseCase get() = RewardPlayerUseCase(playerRepository, levelUpScheduler)
    override val removeRewardFromPlayerUseCase get() = RemoveRewardFromPlayerUseCase(playerRepository, levelDownScheduler)
    override val feedPetUseCase get() = FeedPetUseCase(playerRepository)
    override val revivePetUseCase get() = RevivePetUseCase(playerRepository)
    override val buyPetUseCase get() = BuyPetUseCase(playerRepository)
    override val changePetUseCase get() = ChangePetUseCase(playerRepository)
    override val findPetUseCase get() = FindPetUseCase(playerRepository)
    override val changeThemeUseCase get() = ChangeThemeUseCase(playerRepository)
    override val buyThemeUseCase get() = BuyThemeUseCase(playerRepository)
    override val renamePetUseCase get() = RenamePetUseCase(playerRepository)
    override val buyIconPackUseCase get() = BuyIconPackUseCase(playerRepository)
    override val buyColorPackUseCase get() = BuyColorPackUseCase(playerRepository)
}

interface PopupUseCaseModule {
    val findQuestToRemindUseCase: FindQuestsToRemindUseCase
    val snoozeQuestUseCase: SnoozeQuestUseCase
    val completeQuestUseCase: CompleteQuestUseCase
    val findPlayerLevelUseCase: FindPlayerLevelUseCase
    val rewardPlayerUseCase: RewardPlayerUseCase
    val lowerPetStatsUseCase: LowerPetStatsUseCase
    val findPetUseCase: FindPetUseCase
    val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase
}

class AndroidPopupUseCaseModule : PopupUseCaseModule, Injects<SimpleModule> {
    private val questRepository by required { questRepository }
    private val playerRepository by required { playerRepository }
    private val reminderScheduler by required { reminderScheduler }
    private val questCompleteScheduler by required { questCompleteScheduler }
    private val levelUpScheduler by required { levelUpScheduler }
    override val findQuestToRemindUseCase get() = FindQuestsToRemindUseCase(questRepository)
    override val snoozeQuestUseCase get() = SnoozeQuestUseCase(questRepository, reminderScheduler)
    override val completeQuestUseCase get() = CompleteQuestUseCase(questRepository, playerRepository, reminderScheduler, questCompleteScheduler, rewardPlayerUseCase)
    override val findPlayerLevelUseCase get() = FindPlayerLevelUseCase(playerRepository)
    override val rewardPlayerUseCase get() = RewardPlayerUseCase(playerRepository, levelUpScheduler)
    override val lowerPetStatsUseCase get() = LowerPetStatsUseCase(questRepository, playerRepository)
    override val findPetUseCase get() = FindPetUseCase(playerRepository)
    override val listenForPlayerChangesUseCase get() = ListenForPlayerChangesUseCase(playerRepository)
}

interface UseCaseModule {
    val loadScheduleForDateUseCase: LoadScheduleForDateUseCase
    val saveQuestUseCase: SaveQuestUseCase
    val removeQuestUseCase: RemoveQuestUseCase
    val undoRemoveQuestUseCase: UndoRemovedQuestUseCase
    val findQuestToRemindUseCase: FindQuestsToRemindUseCase
    val snoozeQuestUseCase: SnoozeQuestUseCase
    val completeQuestUseCase: CompleteQuestUseCase
    val undoCompletedQuestUseCase: UndoCompletedQuestUseCase
    val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase
    val rewardPlayerUseCase: RewardPlayerUseCase
    val removeRewardFromPlayerUseCase: RemoveRewardFromPlayerUseCase
    val feedPetUseCase: FeedPetUseCase
    val revivePetUseCase: RevivePetUseCase
    val buyPetUseCase: BuyPetUseCase
    val changePetUseCase: ChangePetUseCase
    val findPetUseCase: FindPetUseCase
    val changeThemeUseCase: ChangeThemeUseCase
    val buyThemeUseCase: BuyThemeUseCase
    val renamePetUseCase: RenamePetUseCase
    val buyIconPackUseCase: BuyIconPackUseCase
    val buyColorPackUseCase: BuyColorPackUseCase
}

interface PresenterModule {
    val homePresenter: HomePresenter
    val calendarPresenter: CalendarPresenter
    val dayViewPresenter: DayViewPresenter
    val reminderPickerPresenter: ReminderPickerDialogPresenter
    val addQuestPresenter: AddQuestPresenter
    val petPresenter: PetPresenter
    val petStorePresenter: PetStorePresenter
    val petDialogPresenter: PetDialogPresenter
    val themeStorePresenter: ThemeStorePresenter
    val colorPickerPresenter: ColorPickerPresenter
    val iconPickerPresenter: IconPickerDialogPresenter

}

class AndroidPresenterModule : PresenterModule, Injects<ControllerModule> {
    private val loadScheduleForDateUseCase by required { loadScheduleForDateUseCase }
    private val saveQuestUseCase by required { saveQuestUseCase }
    private val removeQuestUseCase by required { removeQuestUseCase }
    private val undoRemoveQuestUseCase by required { undoRemoveQuestUseCase }
    private val completeQuestUseCase by required { completeQuestUseCase }
    private val undoCompleteQuestUseCase by required { undoCompletedQuestUseCase }
    private val listenForPlayerChangesUseCase by required { listenForPlayerChangesUseCase }
    private val revivePetUseCase by required { revivePetUseCase }
    private val feedPetUseCase by required { feedPetUseCase }
    private val buyPetUseCase by required { buyPetUseCase }
    private val changePetUseCase by required { changePetUseCase }
    private val findPetUseCase by required { findPetUseCase }
    private val renamePetUseCase by required { renamePetUseCase }
    private val changeThemeUseCase by required { changeThemeUseCase }
    private val buyThemeUseCase by required { buyThemeUseCase }
    private val navigator by required { navigator }
    private val reminderTimeFormatter by required { reminderTimeFormatter }
    private val timeUnitFormatter by required { timeUnitFormatter }
    private val calendarFormatter by required { calendarFormatter }
    private val buyIconPackUseCase by required { buyIconPackUseCase }
    private val buyColorPackUseCase by required { buyColorPackUseCase }
    private val job by required { job }
    override val homePresenter get() = HomePresenter(job)
    override val dayViewPresenter get() = DayViewPresenter(loadScheduleForDateUseCase, saveQuestUseCase, removeQuestUseCase, undoRemoveQuestUseCase, completeQuestUseCase, undoCompleteQuestUseCase, job)
    override val reminderPickerPresenter get() = ReminderPickerDialogPresenter(reminderTimeFormatter, timeUnitFormatter, findPetUseCase, job)
    override val calendarPresenter get() = CalendarPresenter(listenForPlayerChangesUseCase, calendarFormatter, job)
    override val addQuestPresenter get() = AddQuestPresenter(saveQuestUseCase, job)
    override val petPresenter get() = PetPresenter(listenForPlayerChangesUseCase, feedPetUseCase, renamePetUseCase, revivePetUseCase, job)
    override val petStorePresenter get() = PetStorePresenter(listenForPlayerChangesUseCase, buyPetUseCase, changePetUseCase, job)
    override val petDialogPresenter get() = PetDialogPresenter(findPetUseCase, job)
    override val themeStorePresenter get() = ThemeStorePresenter(listenForPlayerChangesUseCase, changeThemeUseCase, buyThemeUseCase, job)
    override val colorPickerPresenter get() = ColorPickerPresenter(listenForPlayerChangesUseCase, buyColorPackUseCase, job)
    override val iconPickerPresenter get() = IconPickerDialogPresenter(listenForPlayerChangesUseCase, buyIconPackUseCase, job)
}

interface PopupPresenterModule {
    val petMessagePresenter: PetMessagePresenter
    val levelUpPresenter: LevelUpPresenter
    val questCompletePresenter: QuestCompletePresenter
}

class AndroidPopupPresenterModule : PopupPresenterModule, Injects<SimpleModule> {
    private val listenForPlayerChangesUseCase by required { listenForPlayerChangesUseCase }
    private val job by required { job }

    override val petMessagePresenter get() = PetMessagePresenter(listenForPlayerChangesUseCase, job)
    override val levelUpPresenter get() = LevelUpPresenter(listenForPlayerChangesUseCase, job)
    override val questCompletePresenter get() = QuestCompletePresenter(listenForPlayerChangesUseCase, job)
}

class ControllerModule(androidModule: AndroidModule,
                       navigationModule: NavigationModule,
                       repositoryModule: RepositoryModule,
                       useCaseModule: UseCaseModule,
                       presenterModule: PresenterModule) :
    AndroidModule by androidModule,
    NavigationModule by navigationModule,
    RepositoryModule by repositoryModule,
    UseCaseModule by useCaseModule,
    PresenterModule by presenterModule,
    HasModules {
    override val modules = setOf(androidModule, navigationModule, repositoryModule, useCaseModule, presenterModule)
}

class SimpleModule(androidModule: AndroidModule,
                   repositoryModule: RepositoryModule,
                   useCaseModule: PopupUseCaseModule,
                   presenterModule: PopupPresenterModule) :
    AndroidModule by androidModule,
    RepositoryModule by repositoryModule,
    PopupUseCaseModule by useCaseModule,
    PopupPresenterModule by presenterModule,
    HasModules {
    override val modules = setOf(androidModule, repositoryModule, useCaseModule, presenterModule)
}
