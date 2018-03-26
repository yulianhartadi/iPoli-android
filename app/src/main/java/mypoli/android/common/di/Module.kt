package mypoli.android.common.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import mypoli.android.challenge.persistence.ChallengeRepository
import mypoli.android.challenge.persistence.FirestoreChallengeRepository
import mypoli.android.challenge.predefined.usecase.SchedulePredefinedChallengeUseCase
import mypoli.android.challenge.sideeffect.ChallengeSideEffectHandler
import mypoli.android.challenge.usecase.*
import mypoli.android.common.*
import mypoli.android.common.analytics.EventLogger
import mypoli.android.common.analytics.FirebaseEventLogger
import mypoli.android.common.middleware.LogEventsMiddleWare
import mypoli.android.common.rate.AndroidRatePopupScheduler
import mypoli.android.common.rate.RatePopupScheduler
import mypoli.android.common.rate.RatePresenter
import mypoli.android.common.redux.CoroutineSideEffectHandlerExecutor
import mypoli.android.common.redux.StateStore
import mypoli.android.common.text.CalendarFormatter
import mypoli.android.common.view.ColorPickerPresenter
import mypoli.android.common.view.CurrencyConverterPresenter
import mypoli.android.common.view.IconPickerDialogPresenter
import mypoli.android.common.view.PetMessagePresenter
import mypoli.android.event.persistence.AndroidCalendarEventRepository
import mypoli.android.event.persistence.AndroidCalendarRepository
import mypoli.android.event.persistence.CalendarRepository
import mypoli.android.event.persistence.EventRepository
import mypoli.android.event.sideeffect.CalendarSideEffectHandler
import mypoli.android.pet.AndroidJobLowerPetStatsScheduler
import mypoli.android.pet.LowerPetStatsScheduler
import mypoli.android.pet.PetDialogPresenter
import mypoli.android.pet.PetPresenter
import mypoli.android.pet.usecase.*
import mypoli.android.player.AndroidLevelDownScheduler
import mypoli.android.player.AndroidLevelUpScheduler
import mypoli.android.player.LevelDownScheduler
import mypoli.android.player.LevelUpScheduler
import mypoli.android.player.auth.saga.AuthSideEffectHandler
import mypoli.android.player.persistence.FirestorePlayerRepository
import mypoli.android.player.persistence.PlayerRepository
import mypoli.android.player.usecase.*
import mypoli.android.player.view.LevelUpPresenter
import mypoli.android.quest.CompletedQuestPresenter
import mypoli.android.quest.data.persistence.FirestoreQuestRepository
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.quest.job.AndroidJobQuestCompleteScheduler
import mypoli.android.quest.job.AndroidJobReminderScheduler
import mypoli.android.quest.job.QuestCompleteScheduler
import mypoli.android.quest.job.ReminderScheduler
import mypoli.android.quest.reminder.formatter.ReminderTimeFormatter
import mypoli.android.quest.reminder.formatter.TimeUnitFormatter
import mypoli.android.quest.reminder.picker.ReminderPickerDialogPresenter
import mypoli.android.quest.schedule.agenda.sideeffect.AgendaSideEffectHandler
import mypoli.android.quest.schedule.agenda.usecase.CreateAgendaItemsUseCase
import mypoli.android.quest.schedule.agenda.usecase.FindAgendaDatesUseCase
import mypoli.android.quest.timer.job.AndroidJobTimerCompleteScheduler
import mypoli.android.quest.timer.job.TimerCompleteScheduler
import mypoli.android.quest.timer.sideeffect.TimerSideEffectHandler
import mypoli.android.quest.timer.usecase.*
import mypoli.android.quest.usecase.*
import mypoli.android.quest.view.QuestCompletePresenter
import mypoli.android.repeatingquest.AndroidSaveQuestsForRepeatingQuestScheduler
import mypoli.android.repeatingquest.SaveQuestsForRepeatingQuestScheduler
import mypoli.android.repeatingquest.persistence.FirestoreRepeatingQuestRepository
import mypoli.android.repeatingquest.persistence.RepeatingQuestRepository
import mypoli.android.repeatingquest.sideeffect.RepeatingQuestSideEffectHandler
import mypoli.android.repeatingquest.usecase.*
import mypoli.android.store.avatar.sideeffect.AvatarSideEffectHandler
import mypoli.android.store.avatar.usecase.BuyAvatarUseCase
import mypoli.android.store.avatar.usecase.ChangeAvatarUseCase
import mypoli.android.store.gem.GemStorePresenter
import mypoli.android.store.membership.MembershipSideEffectHandler
import mypoli.android.store.membership.job.AndroidCheckMembershipStatusScheduler
import mypoli.android.store.membership.job.CheckMembershipStatusScheduler
import mypoli.android.store.membership.usecase.CalculateMembershipPlanPriceUseCase
import mypoli.android.store.membership.usecase.RemoveMembershipUseCase
import mypoli.android.store.membership.usecase.UpdatePlayerMembershipUseCase
import mypoli.android.store.powerup.job.AndroidRemoveExpiredPowerUpsScheduler
import mypoli.android.store.powerup.job.RemoveExpiredPowerUpsScheduler
import mypoli.android.store.powerup.middleware.CheckEnabledPowerUpMiddleWare
import mypoli.android.store.powerup.sideeffect.PowerUpSideEffectHandler
import mypoli.android.store.powerup.usecase.BuyPowerUpUseCase
import mypoli.android.store.powerup.usecase.EnableAllPowerUpsUseCase
import mypoli.android.store.powerup.usecase.RemoveExpiredPowerUpsUseCase
import mypoli.android.store.theme.sideeffect.ThemeSideEffectHandler
import mypoli.android.store.theme.usecase.BuyThemeUseCase
import mypoli.android.store.theme.usecase.ChangeThemeUseCase
import mypoli.android.store.usecase.PurchaseGemPackUseCase
import space.traversal.kapsule.HasModules
import space.traversal.kapsule.Injects
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/10/17.
 */
interface RepositoryModule {
    val questRepository: QuestRepository
    val playerRepository: PlayerRepository
    val repeatingQuestRepository: RepeatingQuestRepository
    val challengeRepository: ChallengeRepository
    val eventRepository: EventRepository
    val calendarRepository: CalendarRepository
}

class FirestoreRepositoryModule : RepositoryModule, Injects<Module> {

    override val questRepository by required {
        FirestoreQuestRepository(
            database,
            job + CommonPool,
            sharedPreferences
        )
    }

    override val playerRepository
        by required {
            FirestorePlayerRepository(
                database,
                job + CommonPool,
                sharedPreferences
            )
        }

    override val repeatingQuestRepository
        by required {
            FirestoreRepeatingQuestRepository(
                database,
                job + CommonPool,
                sharedPreferences
            )
        }

    override val challengeRepository by required {
        FirestoreChallengeRepository(
            database,
            job + CommonPool,
            sharedPreferences
        )
    }

    override val eventRepository by required {
        AndroidCalendarEventRepository()
    }

    override val calendarRepository by required {
        AndroidCalendarRepository()
    }

}

interface AndroidModule {
    val layoutInflater: LayoutInflater

    val sharedPreferences: SharedPreferences

    val reminderTimeFormatter: ReminderTimeFormatter

    val timeUnitFormatter: TimeUnitFormatter

    val calendarFormatter: CalendarFormatter

    val database: FirebaseFirestore

    val eventLogger: EventLogger

    val reminderScheduler: ReminderScheduler

    val timerCompleteScheduler: TimerCompleteScheduler

    val questCompleteScheduler: QuestCompleteScheduler

    val levelUpScheduler: LevelUpScheduler

    val levelDownScheduler: LevelDownScheduler

    val lowerPetStatsScheduler: LowerPetStatsScheduler

    val saveQuestsForRepeatingQuestScheduler: SaveQuestsForRepeatingQuestScheduler

    val removeExpiredPowerUpsScheduler: RemoveExpiredPowerUpsScheduler

    val checkMembershipStatusScheduler: CheckMembershipStatusScheduler

    val ratePopupScheduler: RatePopupScheduler

    val job: Job
}

class MainAndroidModule(
    private val context: Context,
    firestore: FirebaseFirestore,
    firebaseAnalytics: FirebaseAnalytics
) : AndroidModule {
    override val layoutInflater: LayoutInflater get() = LayoutInflater.from(context)

    override val sharedPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(context)

    override val reminderTimeFormatter get() = ReminderTimeFormatter(context)

    override val timeUnitFormatter get() = TimeUnitFormatter(context)

    override val calendarFormatter get() = CalendarFormatter(context)

    override val reminderScheduler get() = AndroidJobReminderScheduler()

    override val timerCompleteScheduler get() = AndroidJobTimerCompleteScheduler()

    override val questCompleteScheduler get() = AndroidJobQuestCompleteScheduler()

    override val levelUpScheduler get() = AndroidLevelUpScheduler()

    override val levelDownScheduler get() = AndroidLevelDownScheduler()

    override val lowerPetStatsScheduler get() = AndroidJobLowerPetStatsScheduler()

    override val saveQuestsForRepeatingQuestScheduler get() = AndroidSaveQuestsForRepeatingQuestScheduler()

    override val removeExpiredPowerUpsScheduler get() = AndroidRemoveExpiredPowerUpsScheduler()

    override val checkMembershipStatusScheduler
        get() = AndroidCheckMembershipStatusScheduler()

    override val ratePopupScheduler get() = AndroidRatePopupScheduler()

    override val database = firestore

    override val eventLogger = FirebaseEventLogger(firebaseAnalytics)

    override val job get() = Job()
}

class MainUseCaseModule : UseCaseModule, Injects<Module> {
    private val questRepository by required { questRepository }
    private val repeatingQuestRepository by required { repeatingQuestRepository }
    private val playerRepository by required { playerRepository }
    private val challengeRepository by required { challengeRepository }
    private val reminderScheduler by required { reminderScheduler }
    private val questCompleteScheduler by required { questCompleteScheduler }
    private val levelUpScheduler by required { levelUpScheduler }
    private val levelDownScheduler by required { levelDownScheduler }
    private val rateDialogScheduler by required { ratePopupScheduler }
    private val timerCompleteScheduler by required { timerCompleteScheduler }

    override val loadScheduleForDateUseCase
        get() = LoadScheduleForDateUseCase()
    override val saveQuestUseCase
        get() = SaveQuestUseCase(
            questRepository,
            reminderScheduler
        )
    override val removeQuestUseCase
        get() = RemoveQuestUseCase(
            questRepository,
            timerCompleteScheduler,
            reminderScheduler
        )
    override val undoRemoveQuestUseCase get() = UndoRemovedQuestUseCase(questRepository)
    override val findQuestToRemindUseCase get() = FindQuestsToRemindUseCase(questRepository)
    override val snoozeQuestUseCase get() = SnoozeQuestUseCase(questRepository, reminderScheduler)
    override val completeQuestUseCase
        get() = CompleteQuestUseCase(
            questRepository,
            playerRepository,
            reminderScheduler,
            questCompleteScheduler,
            rateDialogScheduler,
            rewardPlayerUseCase
        )
    override val undoCompletedQuestUseCase
        get() = UndoCompletedQuestUseCase(
            questRepository,
            reminderScheduler,
            removeRewardFromPlayerUseCase
        )
    override val listenForPlayerChangesUseCase
        get() = ListenForPlayerChangesUseCase(
            playerRepository
        )
    override val rewardPlayerUseCase get() = RewardPlayerUseCase(playerRepository, levelUpScheduler)
    override val removeRewardFromPlayerUseCase
        get() = RemoveRewardFromPlayerUseCase(
            playerRepository,
            levelDownScheduler
        )
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
    override val convertCoinsToGemsUseCase get() = ConvertCoinsToGemsUseCase(playerRepository)
    override val comparePetItemsUseCase get() = ComparePetItemsUseCase()
    override val buyPetItemUseCase get() = BuyPetItemUseCase(playerRepository)
    override val equipPetItemUseCase get() = EquipPetItemUseCase(playerRepository)
    override val takeOffPetItemUseCase get() = TakeOffPetItemUseCase(playerRepository)
    override val purchaseGemPackUseCase get() = PurchaseGemPackUseCase(playerRepository)
    override val schedulePredefinedChallengeUseCase get() = SchedulePredefinedChallengeUseCase()
    override val buyChallengeUseCase get() = BuyChallengeUseCase(playerRepository)
    override val splitDurationForPomodoroTimerUseCase get() = SplitDurationForPomodoroTimerUseCase()
    override val listenForQuestChangeUseCase get() = ListenForQuestChangeUseCase(questRepository)
    override val completeTimeRangeUseCase
        get() = CompleteTimeRangeUseCase(
            questRepository,
            splitDurationForPomodoroTimerUseCase,
            completeQuestUseCase,
            timerCompleteScheduler
        )
    override val cancelTimerUseCase
        get() = CancelTimerUseCase(
            questRepository,
            timerCompleteScheduler
        )
    override val addPomodoroUseCase
        get() = AddPomodoroUseCase(
            questRepository,
            splitDurationForPomodoroTimerUseCase
        )
    override val removePomodoroUseCase
        get() = RemovePomodoroUseCase(
            questRepository,
            splitDurationForPomodoroTimerUseCase
        )
    override val addTimerToQuestUseCase: AddTimerToQuestUseCase
        get() = AddTimerToQuestUseCase(
            questRepository,
            cancelTimerUseCase,
            timerCompleteScheduler
        )

    override val findAgendaDatesUseCase get() = FindAgendaDatesUseCase(questRepository)
    override val createAgendaItemsUseCase get() = CreateAgendaItemsUseCase()

    override val findPlayerLevelUseCase
        get() = FindPlayerLevelUseCase(playerRepository)

    override val lowerPetStatsUseCase
        get() = LowerPetStatsUseCase(
            questRepository,
            playerRepository
        )

    override val saveRepeatingQuestUseCase
        get() = SaveRepeatingQuestUseCase(
            questRepository,
            repeatingQuestRepository,
            saveQuestsForRepeatingQuestUseCase
        )

    override val findNextDateForRepeatingQuestUseCase
        get() = FindNextDateForRepeatingQuestUseCase(
            questRepository
        )

    override val findPeriodProgressForRepeatingQuestUseCase
        get() = FindPeriodProgressForRepeatingQuestUseCase(
            questRepository
        )

    override val saveQuestsForRepeatingQuestUseCase
        get() = SaveQuestsForRepeatingQuestUseCase(
            questRepository
        )
    override val removeRepeatingQuestUseCase
        get() = RemoveRepeatingQuestUseCase(
            questRepository,
            repeatingQuestRepository
        )
    override val createRepeatingQuestHistoryUseCase
        get() = CreateRepeatingQuestHistoryUseCase(
            questRepository,
            repeatingQuestRepository
        )
    override val createPlaceholderQuestsForRepeatingQuestsUseCase
        get() = CreatePlaceholderQuestsForRepeatingQuestsUseCase(
            questRepository,
            repeatingQuestRepository
        )

    override val saveQuestsForChallengeUseCase
        get() = SaveQuestsForChallengeUseCase(
            questRepository,
            repeatingQuestRepository,
            saveRepeatingQuestUseCase
        )

    override val removeQuestFromChallengeUseCase
        get() = RemoveQuestFromChallengeUseCase(questRepository, repeatingQuestRepository)

    override val saveChallengeUseCase
        get() = SaveChallengeUseCase(challengeRepository, saveQuestsForChallengeUseCase)

    override val removeChallengeUseCase
        get() = RemoveChallengeUseCase(
            challengeRepository,
            questRepository,
            repeatingQuestRepository
        )

    override val loadQuestPickerQuestsUseCase
        get() = LoadQuestPickerQuestsUseCase(
            questRepository,
            repeatingQuestRepository
        )

    override val findQuestsForChallengeUseCase
        get() = FindQuestsForChallengeUseCase(
            questRepository,
            repeatingQuestRepository
        )

    override val findNextDateForChallengeUseCase
        get() = FindNextDateForChallengeUseCase()

    override val findChallengeProgressUseCase
        get() = FindChallengeProgressUseCase()

    override val completeChallengeUseCase
        get() = CompleteChallengeUseCase(
            challengeRepository,
            playerRepository
        )

    override val buyPowerUpUseCase
        get() = BuyPowerUpUseCase(playerRepository)

    override val removeExpiredPowerUpsUseCase
        get() = RemoveExpiredPowerUpsUseCase(playerRepository)

    override val enableAllPowerUpsUseCase
        get() = EnableAllPowerUpsUseCase(playerRepository)

    override val updatePlayerMembershipUseCase
        get() = UpdatePlayerMembershipUseCase(
            playerRepository,
            enableAllPowerUpsUseCase
        )

    override val calculateMembershipPlanPriceUseCase
        get() = CalculateMembershipPlanPriceUseCase()

    override val removeMembershipUseCase
        get() = RemoveMembershipUseCase(playerRepository)

    override val buyAvatarUseCase
        get() = BuyAvatarUseCase(playerRepository)

    override val changeAvatarUseCase
        get() = ChangeAvatarUseCase(playerRepository)
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
    val listenForQuestChangeUseCase: ListenForQuestChangeUseCase
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
    val convertCoinsToGemsUseCase: ConvertCoinsToGemsUseCase
    val comparePetItemsUseCase: ComparePetItemsUseCase
    val buyPetItemUseCase: BuyPetItemUseCase
    val equipPetItemUseCase: EquipPetItemUseCase
    val takeOffPetItemUseCase: TakeOffPetItemUseCase
    val purchaseGemPackUseCase: PurchaseGemPackUseCase
    val schedulePredefinedChallengeUseCase: SchedulePredefinedChallengeUseCase
    val buyChallengeUseCase: BuyChallengeUseCase
    val splitDurationForPomodoroTimerUseCase: SplitDurationForPomodoroTimerUseCase
    val findPlayerLevelUseCase: FindPlayerLevelUseCase
    val lowerPetStatsUseCase: LowerPetStatsUseCase
    val completeTimeRangeUseCase: CompleteTimeRangeUseCase
    val cancelTimerUseCase: CancelTimerUseCase
    val addPomodoroUseCase: AddPomodoroUseCase
    val removePomodoroUseCase: RemovePomodoroUseCase
    val addTimerToQuestUseCase: AddTimerToQuestUseCase
    val findAgendaDatesUseCase: FindAgendaDatesUseCase
    val createAgendaItemsUseCase: CreateAgendaItemsUseCase
    val saveRepeatingQuestUseCase: SaveRepeatingQuestUseCase
    val findNextDateForRepeatingQuestUseCase: FindNextDateForRepeatingQuestUseCase
    val findPeriodProgressForRepeatingQuestUseCase: FindPeriodProgressForRepeatingQuestUseCase
    val saveQuestsForRepeatingQuestUseCase: SaveQuestsForRepeatingQuestUseCase
    val removeRepeatingQuestUseCase: RemoveRepeatingQuestUseCase
    val createRepeatingQuestHistoryUseCase: CreateRepeatingQuestHistoryUseCase
    val createPlaceholderQuestsForRepeatingQuestsUseCase: CreatePlaceholderQuestsForRepeatingQuestsUseCase
    val saveChallengeUseCase: SaveChallengeUseCase
    val saveQuestsForChallengeUseCase: SaveQuestsForChallengeUseCase
    val removeQuestFromChallengeUseCase: RemoveQuestFromChallengeUseCase
    val removeChallengeUseCase: RemoveChallengeUseCase
    val loadQuestPickerQuestsUseCase: LoadQuestPickerQuestsUseCase
    val findQuestsForChallengeUseCase: FindQuestsForChallengeUseCase
    val findNextDateForChallengeUseCase: FindNextDateForChallengeUseCase
    val findChallengeProgressUseCase: FindChallengeProgressUseCase
    val completeChallengeUseCase: CompleteChallengeUseCase
    val buyPowerUpUseCase: BuyPowerUpUseCase
    val removeExpiredPowerUpsUseCase: RemoveExpiredPowerUpsUseCase
    val enableAllPowerUpsUseCase: EnableAllPowerUpsUseCase
    val updatePlayerMembershipUseCase: UpdatePlayerMembershipUseCase
    val calculateMembershipPlanPriceUseCase: CalculateMembershipPlanPriceUseCase
    val removeMembershipUseCase: RemoveMembershipUseCase
    val buyAvatarUseCase: BuyAvatarUseCase
    val changeAvatarUseCase: ChangeAvatarUseCase
}

interface PresenterModule {
    val reminderPickerPresenter: ReminderPickerDialogPresenter
    val petPresenter: PetPresenter
    val petDialogPresenter: PetDialogPresenter
    val colorPickerPresenter: ColorPickerPresenter
    val iconPickerPresenter: IconPickerDialogPresenter
    val currencyConverterPresenter: CurrencyConverterPresenter
    val gemStorePresenter: GemStorePresenter
    val petMessagePresenter: PetMessagePresenter
    val levelUpPresenter: LevelUpPresenter
    val questCompletePresenter: QuestCompletePresenter
    val ratePresenter: RatePresenter
    val completedQuestPresenter: CompletedQuestPresenter
}

class AndroidPresenterModule : PresenterModule, Injects<Module> {

    private val questRepository by required { questRepository }
    private val playerRepository by required { playerRepository }
    private val listenForPlayerChangesUseCase by required { listenForPlayerChangesUseCase }
    private val revivePetUseCase by required { revivePetUseCase }
    private val feedPetUseCase by required { feedPetUseCase }
    private val findPetUseCase by required { findPetUseCase }
    private val renamePetUseCase by required { renamePetUseCase }
    private val reminderTimeFormatter by required { reminderTimeFormatter }
    private val timeUnitFormatter by required { timeUnitFormatter }
    private val buyIconPackUseCase by required { buyIconPackUseCase }
    private val buyColorPackUseCase by required { buyColorPackUseCase }
    private val convertCoinsToGemsUseCase by required { convertCoinsToGemsUseCase }
    private val comparePetItemsUseCase by required { comparePetItemsUseCase }
    private val buyPetItemUseCase by required { buyPetItemUseCase }
    private val equipPetItemUseCase by required { equipPetItemUseCase }
    private val takeOffPetItemUseCase by required { takeOffPetItemUseCase }
    private val purchaseGemPackUseCase by required { purchaseGemPackUseCase }
    private val splitDurationForPomodoroTimerUseCase by required { splitDurationForPomodoroTimerUseCase }
    private val job by required { job }
    override val reminderPickerPresenter
        get() = ReminderPickerDialogPresenter(
            reminderTimeFormatter,
            timeUnitFormatter,
            findPetUseCase,
            job
        )

    override val petPresenter
        get() = PetPresenter(
            listenForPlayerChangesUseCase,
            feedPetUseCase,
            renamePetUseCase,
            revivePetUseCase,
            comparePetItemsUseCase,
            buyPetItemUseCase,
            equipPetItemUseCase,
            takeOffPetItemUseCase,
            job
        )
    override val petDialogPresenter get() = PetDialogPresenter(findPetUseCase, job)
    override val colorPickerPresenter
        get() = ColorPickerPresenter(
            listenForPlayerChangesUseCase,
            buyColorPackUseCase,
            job
        )
    override val iconPickerPresenter
        get() = IconPickerDialogPresenter(
            listenForPlayerChangesUseCase,
            buyIconPackUseCase,
            job
        )
    override val currencyConverterPresenter
        get() = CurrencyConverterPresenter(
            listenForPlayerChangesUseCase,
            convertCoinsToGemsUseCase,
            job
        )
    override val gemStorePresenter
        get() = GemStorePresenter(
            purchaseGemPackUseCase,
            listenForPlayerChangesUseCase,
            job
        )

    override val completedQuestPresenter: CompletedQuestPresenter
        get() = CompletedQuestPresenter(
            questRepository,
            playerRepository,
            splitDurationForPomodoroTimerUseCase,
            job
        )

    override val petMessagePresenter get() = PetMessagePresenter(listenForPlayerChangesUseCase, job)
    override val levelUpPresenter get() = LevelUpPresenter(listenForPlayerChangesUseCase, job)
    override val questCompletePresenter
        get() = QuestCompletePresenter(
            listenForPlayerChangesUseCase,
            job
        )
    override val ratePresenter get() = RatePresenter(listenForPlayerChangesUseCase, job)
}

interface StateStoreModule {
    val stateStore: StateStore<AppState>
}

class AndroidStateStoreModule : StateStoreModule, Injects<Module> {

    override val stateStore by required {
        StateStore(
            initialState = AppState(
                data = mapOf(
                    AppDataState::class.java.simpleName to AppDataReducer.defaultState()
                )
            ),
            reducers = setOf(
                AppDataReducer
            ),
            sideEffectHandlers = setOf(
                LoadAllDataSideEffectHandler(),
                AuthSideEffectHandler(),
                AgendaSideEffectHandler(),
                BuyPredefinedChallengeSideEffectHandler(),
                ChangePetSideEffectHandler(),
                BuyPetSideEffectHandler(),
                DayViewSideEffectHandler(),
                RepeatingQuestSideEffectHandler(),
                AddQuestSideEffectHandler(),
                ChallengeSideEffectHandler(),
                CalendarSideEffectHandler(),
                MembershipSideEffectHandler(),
                PowerUpSideEffectHandler(),
                TimerSideEffectHandler(),
                AvatarSideEffectHandler(),
                ThemeSideEffectHandler()
            ),
            sideEffectHandlerExecutor = CoroutineSideEffectHandlerExecutor(job + CommonPool),
            middleware = setOf(
                LogEventsMiddleWare(),
                CheckEnabledPowerUpMiddleWare()
            )
        )
    }
}

class Module(
    androidModule: AndroidModule,
    repositoryModule: RepositoryModule,
    useCaseModule: UseCaseModule,
    presenterModule: PresenterModule,
    stateStoreModule: StateStoreModule
) :
    AndroidModule by androidModule,
    RepositoryModule by repositoryModule,
    UseCaseModule by useCaseModule,
    PresenterModule by presenterModule,
    StateStoreModule by stateStoreModule,
    HasModules {
    override val modules =
        setOf(
            androidModule,
            repositoryModule,
            useCaseModule,
            presenterModule,
            stateStoreModule
        )
}