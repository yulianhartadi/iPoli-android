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
import io.ipoli.android.home.HomePresenter
import io.ipoli.android.pet.AndroidJobLowerPetStatsScheduler
import io.ipoli.android.pet.LowerPetStatsScheduler
import io.ipoli.android.pet.PetPresenter
import io.ipoli.android.pet.usecase.FeedPetUseCase
import io.ipoli.android.pet.usecase.ListenForPetChangesUseCase
import io.ipoli.android.pet.usecase.LowerPetStatsUseCase
import io.ipoli.android.player.AndroidLevelDownScheduler
import io.ipoli.android.player.AndroidLevelUpScheduler
import io.ipoli.android.player.LevelDownScheduler
import io.ipoli.android.player.LevelUpScheduler
import io.ipoli.android.player.persistence.CouchbasePlayerRepository
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.usecase.FindPlayerLevelUseCase
import io.ipoli.android.player.usecase.ListenForPlayerChangesUseCase
import io.ipoli.android.player.usecase.RemoveRewardFromPlayerUseCase
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import io.ipoli.android.quest.AndroidJobQuestCompleteScheduler
import io.ipoli.android.quest.QuestCompleteScheduler
import io.ipoli.android.quest.calendar.CalendarPresenter
import io.ipoli.android.quest.calendar.addquest.AddQuestPresenter
import io.ipoli.android.quest.calendar.dayview.DayViewPresenter
import io.ipoli.android.quest.data.persistence.CouchbaseQuestRepository
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.usecase.*
import io.ipoli.android.reminder.AndroidJobReminderScheduler
import io.ipoli.android.reminder.ReminderScheduler
import io.ipoli.android.reminder.view.formatter.ReminderTimeFormatter
import io.ipoli.android.reminder.view.formatter.TimeUnitFormatter
import io.ipoli.android.reminder.view.picker.ReminderPickerDialogPresenter
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

class CouchbaseJobRepositoryModule : RepositoryModule, Injects<JobModule> {
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
    private val job by required { job }
    override val loadScheduleForDateUseCase
        get() = LoadScheduleForDateUseCase(questRepository, job + CommonPool)
    override val saveQuestUseCase get() = SaveQuestUseCase(questRepository, reminderScheduler)
    override val removeQuestUseCase get() = RemoveQuestUseCase(questRepository, reminderScheduler)
    override val undoRemoveQuestUseCase get() = UndoRemovedQuestUseCase(questRepository)
    override val findQuestToRemindUseCase get() = FindQuestsToRemindUseCase(questRepository)
    override val snoozeQuestUseCase get() = SnoozeQuestUseCase(questRepository, reminderScheduler)
    override val completeQuestUseCase get() = CompleteQuestUseCase(questRepository, playerRepository, reminderScheduler, questCompleteScheduler, rewardPlayerUseCase)
    override val undoCompletedQuestUseCase get() = UndoCompletedQuestUseCase(questRepository, reminderScheduler, removeRewardFromPlayerUseCase)
    override val listenForPlayerChangesUseCase get() = ListenForPlayerChangesUseCase(playerRepository, job + CommonPool)
    override val listenForPetChangesUseCase get() = ListenForPetChangesUseCase(playerRepository, job + CommonPool)
    override val rewardPlayerUseCase get() = RewardPlayerUseCase(playerRepository, levelUpScheduler)
    override val removeRewardFromPlayerUseCase get() = RemoveRewardFromPlayerUseCase(playerRepository, levelDownScheduler)
    override val feedPetUseCase get() = FeedPetUseCase(playerRepository)

}

interface JobUseCaseModule {
    val findQuestToRemindUseCase: FindQuestsToRemindUseCase
    val snoozeQuestUseCase: SnoozeQuestUseCase
    val completeQuestUseCase: CompleteQuestUseCase
    val findCompletedQuestUseCase: FindCompletedQuestUseCase
    val findPlayerLevelUseCase: FindPlayerLevelUseCase
    val rewardPlayerUseCase: RewardPlayerUseCase
    val lowerPetStatsUseCase: LowerPetStatsUseCase
}

class AndroidJobUseCaseModule : JobUseCaseModule, Injects<JobModule> {
    private val questRepository by required { questRepository }
    private val playerRepository by required { playerRepository }
    private val reminderScheduler by required { reminderScheduler }
    private val questCompleteScheduler by required { questCompleteScheduler }
    private val levelUpScheduler by required { levelUpScheduler }
    override val findQuestToRemindUseCase get() = FindQuestsToRemindUseCase(questRepository)
    override val snoozeQuestUseCase get() = SnoozeQuestUseCase(questRepository, reminderScheduler)
    override val completeQuestUseCase get() = CompleteQuestUseCase(questRepository, playerRepository, reminderScheduler, questCompleteScheduler, rewardPlayerUseCase)
    override val findCompletedQuestUseCase get() = FindCompletedQuestUseCase(questRepository)
    override val findPlayerLevelUseCase get() = FindPlayerLevelUseCase(playerRepository)
    override val rewardPlayerUseCase get() = RewardPlayerUseCase(playerRepository, levelUpScheduler)
    override val lowerPetStatsUseCase get() = LowerPetStatsUseCase(questRepository, playerRepository)
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
    val listenForPetChangesUseCase: ListenForPetChangesUseCase
    val rewardPlayerUseCase: RewardPlayerUseCase
    val removeRewardFromPlayerUseCase: RemoveRewardFromPlayerUseCase
    val feedPetUseCase: FeedPetUseCase
}

interface PresenterModule {
    val homePresenter: HomePresenter
    val calendarPresenter: CalendarPresenter
    val dayViewPresenter: DayViewPresenter
    val reminderPickerPresenter: ReminderPickerDialogPresenter
    val addQuestPresenter: AddQuestPresenter
    val petPresenter: PetPresenter
}

class AndroidPresenterModule : PresenterModule, Injects<ControllerModule> {
    private val loadScheduleForDateUseCase by required { loadScheduleForDateUseCase }
    private val saveQuestUseCase by required { saveQuestUseCase }
    private val removeQuestUseCase by required { removeQuestUseCase }
    private val undoRemoveQuestUseCase by required { undoRemoveQuestUseCase }
    private val completeQuestUseCase by required { completeQuestUseCase }
    private val undoCompleteQuestUseCase by required { undoCompletedQuestUseCase }
    private val listenForPlayerChangesUseCase by required { listenForPlayerChangesUseCase }
    private val listenForPetChangesUseCase by required { listenForPetChangesUseCase }
    private val feedPetUseCase by required { feedPetUseCase }
    private val navigator by required { navigator }
    private val reminderTimeFormatter by required { reminderTimeFormatter }
    private val timeUnitFormatter by required { timeUnitFormatter }
    private val calendarFormatter by required { calendarFormatter }
    private val job by required { job }
    override val homePresenter get() = HomePresenter(job)
    override val dayViewPresenter get() = DayViewPresenter(loadScheduleForDateUseCase, saveQuestUseCase, removeQuestUseCase, undoRemoveQuestUseCase, completeQuestUseCase, undoCompleteQuestUseCase, job)
    override val reminderPickerPresenter get() = ReminderPickerDialogPresenter(reminderTimeFormatter, timeUnitFormatter, job)
    override val calendarPresenter get() = CalendarPresenter(listenForPlayerChangesUseCase, calendarFormatter, job)
    override val addQuestPresenter get() = AddQuestPresenter(saveQuestUseCase, job)
    override val petPresenter get() = PetPresenter(listenForPetChangesUseCase, feedPetUseCase, job)
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

class JobModule(androidModule: AndroidModule,
                repositoryModule: RepositoryModule,
                useCaseModule: JobUseCaseModule) :
    AndroidModule by androidModule,
    RepositoryModule by repositoryModule,
    JobUseCaseModule by useCaseModule,
    HasModules {
    override val modules = setOf(androidModule, repositoryModule, useCaseModule)
}