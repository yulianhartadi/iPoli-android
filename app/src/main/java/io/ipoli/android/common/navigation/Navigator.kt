package io.ipoli.android.common.navigation

import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler
import com.google.firebase.auth.FirebaseAuth
import io.ipoli.android.Constants
import io.ipoli.android.achievement.list.AchievementListViewController
import io.ipoli.android.challenge.QuestPickerViewController
import io.ipoli.android.challenge.add.AddChallengeViewController
import io.ipoli.android.challenge.add.MaintainAverageValuePickerDialogController
import io.ipoli.android.challenge.add.TargetValuePickerDialogController
import io.ipoli.android.challenge.edit.ChallengeMotivationsDialogController
import io.ipoli.android.challenge.edit.EditChallengeViewController
import io.ipoli.android.challenge.edit.LogValueDialogController
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.picker.ChallengePickerDialogController
import io.ipoli.android.challenge.preset.PresetChallenge
import io.ipoli.android.challenge.preset.PresetChallengeViewController
import io.ipoli.android.challenge.preset.category.ChallengeCategoryListViewController
import io.ipoli.android.challenge.preset.category.list.ChallengeListForCategoryViewController
import io.ipoli.android.challenge.preset.picker.PhysicalCharacteristicsPickerDialogController
import io.ipoli.android.challenge.show.ChallengeViewController
import io.ipoli.android.challenge.usecase.CreateChallengeFromPresetUseCase
import io.ipoli.android.common.ConfirmationDialogViewController
import io.ipoli.android.common.HelpDialogViewController
import io.ipoli.android.common.ShareAppDialogController
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Minute
import io.ipoli.android.common.feedback.WebUrlViewController
import io.ipoli.android.common.home.HomeViewController
import io.ipoli.android.common.migration.MigrationViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.dailychallenge.DailyChallengeViewController
import io.ipoli.android.event.calendar.picker.CalendarPickerDialogController
import io.ipoli.android.friends.ReactionHistoryDialogViewController
import io.ipoli.android.friends.feed.data.Post
import io.ipoli.android.friends.feed.picker.PostItemPickerViewController
import io.ipoli.android.friends.feed.post.AddPostDialogController
import io.ipoli.android.friends.feed.post.PostViewController
import io.ipoli.android.friends.invite.AcceptFriendshipDialogController
import io.ipoli.android.friends.invite.InviteFriendsDialogController
import io.ipoli.android.friends.share.SharePostDialogController
import io.ipoli.android.habit.edit.EditHabitViewController
import io.ipoli.android.habit.predefined.PredefinedHabitListViewController
import io.ipoli.android.habit.show.HabitViewController
import io.ipoli.android.note.NotePickerDialogController
import io.ipoli.android.onboarding.OnboardData
import io.ipoli.android.onboarding.OnboardViewController
import io.ipoli.android.onboarding.scenes.PickOnboardItemsViewController
import io.ipoli.android.pet.PetViewController
import io.ipoli.android.pet.store.PetStoreViewController
import io.ipoli.android.planday.PlanDayViewController
import io.ipoli.android.planday.RescheduleDialogController
import io.ipoli.android.player.attribute.AttributeListViewController
import io.ipoli.android.player.auth.AuthViewController
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.profile.EditProfileDialogController
import io.ipoli.android.player.profile.FriendProfileViewController
import io.ipoli.android.player.profile.ProfileViewController
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.CompletedQuestViewController
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.edit.EditQuestViewController
import io.ipoli.android.quest.reminder.picker.ReminderPickerDialogController
import io.ipoli.android.quest.reminder.picker.ReminderViewModel
import io.ipoli.android.quest.schedule.addquest.AddQuestViewController
import io.ipoli.android.quest.schedule.agenda.view.AgendaViewController
import io.ipoli.android.quest.schedule.calendar.CalendarViewController
import io.ipoli.android.quest.schedule.summary.ScheduleSummaryViewController
import io.ipoli.android.quest.show.QuestViewController
import io.ipoli.android.repeatingquest.add.AddRepeatingQuestViewController
import io.ipoli.android.repeatingquest.edit.EditRepeatingQuestViewController
import io.ipoli.android.repeatingquest.edit.picker.RepeatPatternPickerDialogController
import io.ipoli.android.repeatingquest.entity.RepeatPattern
import io.ipoli.android.repeatingquest.show.RepeatingQuestViewController
import io.ipoli.android.settings.view.DaysPickerDialogController
import io.ipoli.android.settings.view.NotificationStylePickerDialogController
import io.ipoli.android.settings.view.TemperatureUnitPickerDialogController
import io.ipoli.android.settings.view.TimeFormatPickerDialogController
import io.ipoli.android.store.avatar.AvatarStoreViewController
import io.ipoli.android.store.gem.GemStoreViewController
import io.ipoli.android.store.membership.MembershipViewController
import io.ipoli.android.store.powerup.PowerUp
import io.ipoli.android.store.powerup.PowerUpStoreViewController
import io.ipoli.android.store.powerup.buy.BuyPowerUpDialogController
import io.ipoli.android.store.theme.ThemeStoreViewController
import io.ipoli.android.tag.Tag
import io.ipoli.android.tag.dialog.TagPickerDialogController
import io.ipoli.android.tag.edit.EditTagViewController
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 05/27/2018.
 */

class Navigator(private val router: Router) {

    fun setOnboard() {
        setController({ OnboardViewController() })
    }

    fun setMigration(playerId: String, playerSchemaVersion: Int) {
        setController({ MigrationViewController(playerId, playerSchemaVersion) })
    }

    fun setHome() {
        setController({ HomeViewController() })
    }

    fun toProfile() {
        pushController({ ProfileViewController() }, VerticalChangeHandler())
    }

    fun toProfile(friendId: String) {
        val profileViewController =
            if (FirebaseAuth.getInstance().currentUser?.uid == friendId)
                ProfileViewController()
            else FriendProfileViewController(friendId)

        val changeHandler = HorizontalChangeHandler()
        router.pushController(
            RouterTransaction
                .with(profileViewController)
                .pushChangeHandler(changeHandler)
                .popChangeHandler(changeHandler)
        )
    }

    fun setPlanDay() {
        setController({ PlanDayViewController() })
    }

    fun toPlanDay() {
        pushController({ PlanDayViewController() }, VerticalChangeHandler())
    }

    fun toScheduleSummary(currentDate: LocalDate) {
        pushController({ ScheduleSummaryViewController(currentDate) }, VerticalChangeHandler())
    }

    fun toAttributes(attribute: Player.AttributeType? = null) {
        pushController({
            if (attribute == null) AttributeListViewController()
            else AttributeListViewController(attribute)
        }, VerticalChangeHandler())
    }

    fun setAddQuest(
        closeListener: () -> Unit,
        currentDate: LocalDate?,
        isFullscreen: Boolean = false,
        changeHandler: ControllerChangeHandler? = null
    ) {
        setController(
            { AddQuestViewController(closeListener, currentDate, isFullscreen) },
            changeHandler
        )
    }

    fun setPet(showBackButton: Boolean) {
        setController({ PetViewController(showBackButton = showBackButton) })
    }

    fun toPet(changeHandler: ControllerChangeHandler? = null) {
        pushController({ PetViewController() }, changeHandler)
    }

    fun toAchievementList(changeHandler: ControllerChangeHandler? = null) {
        pushController({ AchievementListViewController() }, changeHandler)
    }

    fun toAuth(onboardData: OnboardData?, changeHandler: ControllerChangeHandler? = null) {
        pushController({ AuthViewController(onboardData) }, changeHandler)
    }

    fun toDailyChallenge() {
        pushController({ DailyChallengeViewController() }, VerticalChangeHandler())
    }

    fun setAuth(onboardData: OnboardData? = null, changeHandler: ControllerChangeHandler? = null) {
        setController({ AuthViewController(onboardData) }, changeHandler)
    }

    fun toEditQuest(
        questId: String?,
        params: EditQuestViewController.Params? = null,
        changeHandler: ControllerChangeHandler? = null
    ) {
        pushController({ EditQuestViewController(questId, params) }, changeHandler)
    }

    fun toCompletedQuest(questId: String, changeHandler: ControllerChangeHandler? = null) {
        pushController({ CompletedQuestViewController(questId) }, changeHandler)
    }

    fun toRepeatingQuest(repeatingQuestId: String, changeHandler: ControllerChangeHandler? = null) {
        pushController({ RepeatingQuestViewController(repeatingQuestId) }, changeHandler)
    }

    fun toAddRepeatingQuest() {
        pushController({ AddRepeatingQuestViewController() }, VerticalChangeHandler())
    }

    fun toEditRepeatingQuest(repeatingQuestId: String) {
        pushController(
            { EditRepeatingQuestViewController(repeatingQuestId) },
            HorizontalChangeHandler()
        )
    }

    fun toAddHabit(params: EditHabitViewController.Params? = null) {
        pushController({ EditHabitViewController("", params) }, VerticalChangeHandler())
    }

    fun toEditHabit(habitId: String, changeHandler: ControllerChangeHandler? = null) {
        pushController({ EditHabitViewController(habitId) }, changeHandler)
    }

    fun toAddChallenge() {
        pushController({ AddChallengeViewController() }, VerticalChangeHandler())
    }

    fun toQuestPicker(challengeId: String) {
        pushController({ QuestPickerViewController(challengeId) }, HorizontalChangeHandler())
    }

    fun toPostItemPicker() {
        pushController({ PostItemPickerViewController() }, VerticalChangeHandler())
    }

    fun toPredefinedHabits() {
        pushController({ PredefinedHabitListViewController() }, VerticalChangeHandler())
    }

    fun toEditTag(tagId: String? = null, changeHandler: ControllerChangeHandler?) {
        pushController({ EditTagViewController(tagId) }, changeHandler)
    }

    fun toEditChallenge(challengeId: String) {
        pushController({ EditChallengeViewController(challengeId) }, HorizontalChangeHandler())
    }

    fun toPhysicalCharacteristicsPicker(
        listener: (CreateChallengeFromPresetUseCase.PhysicalCharacteristics?) -> Unit
    ) {
        pushDialog {
            PhysicalCharacteristicsPickerDialogController(
                listener
            )
        }
    }

    fun toTargetValuePicker(
        targetValueSelectedListener: (Challenge.TrackedValue.Target) -> Unit,
        cancelListener: () -> Unit = {},
        trackedValue: Challenge.TrackedValue.Target? = null
    ) {
        pushDialog {
            TargetValuePickerDialogController(
                targetValueSelectedListener,
                cancelListener,
                trackedValue
            )
        }
    }

    fun toMaintainAverageValuePicker(
        trackedValueSelectedListener: (Challenge.TrackedValue.Average) -> Unit,
        cancelListener: () -> Unit = {},
        trackedValue: Challenge.TrackedValue.Average? = null
    ) {
        pushDialog {
            MaintainAverageValuePickerDialogController(
                trackedValueSelectedListener,
                cancelListener,
                trackedValue
            )
        }
    }

    fun toLogValue(
        valueName: String,
        valueUnits: String,
        showAccumulateValueHint: Boolean,
        logValueListener: (Challenge.TrackedValue.Log) -> Unit,
        cancelListener: () -> Unit = {}
    ) {
        pushDialog {
            LogValueDialogController(
                valueName,
                valueUnits,
                showAccumulateValueHint,
                logValueListener,
                cancelListener
            )
        }
    }

    fun toChallenge(challengeId: String) {
        pushController({ ChallengeViewController(challengeId) }, VerticalChangeHandler())
    }

    fun toHabit(habitId: String) {
        pushController({ HabitViewController(habitId) }, VerticalChangeHandler())
    }

    fun setQuest(questId: String) {
        setController({ QuestViewController(questId) })
    }

    fun toQuest(questId: String) {
        toQuest(questId, VerticalChangeHandler())
    }

    fun toQuest(questId: String, changeHandler: ControllerChangeHandler? = null) {
        pushController({ QuestViewController(questId) }, changeHandler)
    }

    fun toGemStore(changeHandler: ControllerChangeHandler? = null) {
        pushController({ GemStoreViewController() }, changeHandler)
    }

    fun toMembership(changeHandler: ControllerChangeHandler? = null) {
        pushController({ MembershipViewController() }, changeHandler)
    }

    fun toPowerUpStore(changeHandler: ControllerChangeHandler? = null) {
        pushController({ PowerUpStoreViewController() }, changeHandler)
    }

    fun toAvatarStore(changeHandler: ControllerChangeHandler? = null) {
        pushController({ AvatarStoreViewController() }, changeHandler)
    }

    fun toPetStore(changeHandler: ControllerChangeHandler? = null) {
        pushController({ PetStoreViewController() }, changeHandler)
    }

    fun toThemeStore(changeHandler: ControllerChangeHandler? = null) {
        pushController({ ThemeStoreViewController() }, changeHandler)
    }

    fun toPost(postId: String) {
        pushController({ PostViewController(postId) }, HorizontalChangeHandler())
    }

    fun toCurrencyConverted() {
        pushDialog { CurrencyConverterDialogController() }
    }

    fun toColorPicker(listener: (Color) -> Unit = {}, selectedColor: Color? = null) {
        pushDialog { ColorPickerDialogController(listener, selectedColor) }
    }

    fun toIconPicker(listener: (Icon?) -> Unit = {}, selectedIcon: Icon? = null) {
        pushDialog { IconPickerDialogController(listener, selectedIcon) }
    }

    fun toBuyPowerUp(powerUp: PowerUp.Type, listener: (BuyPowerUpDialogController.Result) -> Unit) {
        pushDialog { BuyPowerUpDialogController(powerUp, listener) }
    }

    fun toCalendarPicker(listener: (Set<Player.Preferences.SyncCalendar>) -> Unit) {
        pushDialog { CalendarPickerDialogController(listener) }
    }

    fun toReactionHistory(reactions: List<Post.Reaction>) {
        pushDialog { ReactionHistoryDialogViewController(reactions) }
    }

    fun toConfirmation(title: String, message: String, listener: () -> Unit) {
        pushDialog { ConfirmationDialogViewController(title, message, listener) }
    }

    fun toHelp(title: String, message: String) {
        pushDialog { HelpDialogViewController(title, message) }
    }

    fun toChallengeMotivations(
        motivation1: String,
        motivation2: String,
        motivation3: String,
        listener: (String, String, String) -> Unit
    ) {
        pushDialog {
            ChallengeMotivationsDialogController(
                motivation1 = motivation1,
                motivation2 = motivation2,
                motivation3 = motivation3,
                listener = listener
            )
        }
    }

    fun toChallengePicker(challenge: Challenge? = null, listener: (Challenge?) -> Unit) {
        pushDialog { ChallengePickerDialogController(challenge, listener) }
    }

    fun toDaysPicker(
        selectedDays: Set<DayOfWeek>,
        listener: (Set<DayOfWeek>) -> Unit
    ) {
        pushDialog { DaysPickerDialogController(selectedDays, listener) }
    }

    fun toDurationPicker(
        selectedDuration: Duration<Minute>? = null,
        listener: (Duration<Minute>) -> Unit
    ) {
        pushDialog { DurationPickerDialogController(selectedDuration, listener) }
    }

    fun toCommunity() {
        pushController(
            { WebUrlViewController(Constants.COMMUNITY_LINK, "Community") },
            VerticalChangeHandler()
        )
    }

    fun toBugReport() {
        pushController(
            { WebUrlViewController(Constants.BUG_REPORT_LINK, "ReportBug") },
            VerticalChangeHandler()
        )
    }

    fun toHelpPage() {
        pushController(
            { WebUrlViewController(Constants.HELP_LINK, "HelpPage") },
            VerticalChangeHandler()
        )
    }

    fun toFAQ() {
        pushController(
            { WebUrlViewController(Constants.FAQ_LINK, "FAQPage") },
            VerticalChangeHandler()
        )
    }

    fun toPresetChallengeCategory() {
        pushController(
            { ChallengeCategoryListViewController() },
            VerticalChangeHandler()
        )
    }

    fun toPresetChallenge(challenge: PresetChallenge) {
        pushController(
            { PresetChallengeViewController(challenge) },
            SimpleSwapChangeHandler()
        )
    }

    fun toChallengeListForCategory(category: PresetChallenge.Category) {
        pushController(
            { ChallengeListForCategoryViewController(category) },
            HorizontalChangeHandler()
        )
    }

    fun toFeedback(listener: FeedbackDialogController.FeedbackListener) {
        pushDialog { FeedbackDialogController(listener) }
    }

    fun toSuggestIdea() {
        pushController(
            { WebUrlViewController(Constants.SUGGEST_IDEA_LINK, "SuggestIdea") },
            VerticalChangeHandler()
        )
    }

    fun toPickOnboardItems() {
        pushController(
            { PickOnboardItemsViewController() },
            HorizontalChangeHandler()
        )
    }

    fun toShareApp(dialogTitle: String, message: String) {
        pushDialog { ShareAppDialogController(dialogTitle, message) }
    }

    fun toSharePost(message: String) {
        pushDialog { SharePostDialogController(message) }
    }

    fun toInviteFriends() {
        pushDialog { InviteFriendsDialogController() }
    }

    fun toEditProfile() {
        pushDialog { EditProfileDialogController() }
    }

    fun toAcceptFriendship(invitePlayerId: String) {
        pushDialog { AcceptFriendshipDialogController(invitePlayerId) }
    }

    fun toReminderPicker(
        listener: ReminderPickerDialogController.ReminderPickedListener,
        selectedReminder: ReminderViewModel? = null
    ) {
        pushDialog { ReminderPickerDialogController(listener, selectedReminder) }
    }

    fun toRepeatPatternPicker(
        repeatPattern: RepeatPattern? = null,
        resultListener: (RepeatPattern) -> Unit,
        cancelListener: (() -> Unit)? = null
    ) {
        pushDialog {
            RepeatPatternPickerDialogController(
                repeatPattern,
                resultListener,
                cancelListener
            )
        }
    }

    fun toReschedule(
        includeToday: Boolean,
        listener: (LocalDate?) -> Unit,
        cancelListener: () -> Unit = {}
    ) {
        pushDialog { RescheduleDialogController(includeToday, listener, cancelListener) }
    }

    fun toTagPicker(selectedTags: Set<Tag> = emptySet(), listener: (Set<Tag>) -> Unit) {
        pushDialog { TagPickerDialogController(selectedTags, listener) }
    }

    fun toTemperatureUnitPicker(
        selectedTemperatureUnit: Player.Preferences.TemperatureUnit,
        listener: (Player.Preferences.TemperatureUnit) -> Unit
    ) {
        pushDialog { TemperatureUnitPickerDialogController(selectedTemperatureUnit, listener) }
    }

    fun toNotificationStylePicker(
        selectedNotificationStyle: Player.Preferences.NotificationStyle,
        listener: (Player.Preferences.NotificationStyle) -> Unit
    ) {
        pushDialog { NotificationStylePickerDialogController(selectedNotificationStyle, listener) }
    }

    fun toTextPicker(
        listener: (String) -> Unit,
        title: String,
        text: String = "",
        hint: String = ""
    ) {
        pushDialog { TextPickerDialogController(listener, title, text, hint) }
    }

    fun toTimeFormatPicker(
        selectedTimeFormat: Player.Preferences.TimeFormat,
        listener: (Player.Preferences.TimeFormat) -> Unit
    ) {
        pushDialog { TimeFormatPickerDialogController(selectedTimeFormat, listener) }
    }

    fun toNotePicker(
        note: String,
        resultListener: (String) -> Unit
    ) {
        pushDialog { NotePickerDialogController(note, resultListener) }
    }

    fun toAddPost(
        questId: String?,
        habitId: String?,
        challengeId: String?,
        postListener: (() -> Unit)? = null
    ) {
        pushDialog { AddPostDialogController(questId, habitId, challengeId, postListener) }
    }

    private inline fun <reified C : Controller> pushDialog(createDialogController: () -> C) {
        val t = tag<C>()
        val c = router.getControllerWithTag(t)
        val changeHandler = SimpleSwapChangeHandler(false)
        if (c == null || c.isBeingDestroyed || c.isDestroyed) {
            router.pushController(createTransaction(createDialogController(), changeHandler, t))
        }
    }

    private inline fun <reified C : Controller> setController(
        createController: () -> C,
        changeHandler: ControllerChangeHandler? = null
    ) {
        val tag = tag<C>()
        val c = router.getControllerWithTag(tag)
        if (c == null || c.isBeingDestroyed || c.isDestroyed) {
            router.setRoot(createTransaction(createController(), changeHandler, tag))
        } else {
            router.setRoot(createTransaction(c, changeHandler, tag))
        }
    }

    private inline fun <reified C : Controller> pushController(
        createController: () -> C,
        changeHandler: ControllerChangeHandler?
    ) {
        pushController(createController, changeHandler, changeHandler)
    }

    private inline fun <reified C : Controller> pushController(
        createController: () -> C,
        pushChangeHandler: ControllerChangeHandler?,
        popChangeHandler: ControllerChangeHandler?
    ) {
        val tag = tag<C>()
        val c = router.getControllerWithTag(tag)
        if (c == null || c.isBeingDestroyed || c.isDestroyed) {
            router.pushController(
                createTransaction(
                    createController(),
                    pushChangeHandler,
                    popChangeHandler,
                    tag
                )
            )
        } else {
            router.pushController(createTransaction(c, pushChangeHandler, popChangeHandler, tag))
        }
    }

    private inline fun <reified C : Controller> replaceTopController(
        createController: () -> C,
        changeHandler: ControllerChangeHandler?
    ) {
        val tag = tag<C>()
        val c = router.getControllerWithTag(tag)
        if (c == null || c.isBeingDestroyed || c.isDestroyed) {
            router.replaceTopController(createTransaction(createController(), changeHandler, tag))
        }
    }

    private fun createTransaction(
        controller: Controller,
        changeHandler: ControllerChangeHandler?,
        tag: String
    ) = createTransaction(controller, changeHandler, changeHandler, tag)

    private fun createTransaction(
        controller: Controller,
        pushChangeHandler: ControllerChangeHandler?,
        popChangeHandler: ControllerChangeHandler?,
        tag: String
    ) =
        RouterTransaction.with(controller).apply {
            pushChangeHandler?.let {
                pushChangeHandler(it)
            }
            popChangeHandler?.let {
                popChangeHandler(it)
            }
            tag(tag)
        }

    fun replaceWithCompletedQuest(questId: String, changeHandler: ControllerChangeHandler? = null) {
        replaceTopController({ CompletedQuestViewController(questId) }, changeHandler)
    }

    fun replaceWithCalendar(currentDate: LocalDate) {
        replaceTopController({ CalendarViewController(currentDate) }, FadeChangeHandler())
    }

    fun replaceWithAgenda(currentDate: LocalDate) {
        replaceTopController({
            AgendaViewController(
                currentDate
            )
        }, FadeChangeHandler())
    }

    fun replaceWithHome(changeHandler: ControllerChangeHandler? = null) {
        replaceTopController({ HomeViewController() }, changeHandler)
    }

    companion object {
        inline fun <reified C : Controller> tag(): String = C::class.java.simpleName
    }

}