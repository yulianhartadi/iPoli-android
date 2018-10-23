package io.ipoli.android.friends.feed.post

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.ipoli.android.R
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.datetime.minutes
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.common.showImagePicker
import io.ipoli.android.common.text.DurationFormatter
import io.ipoli.android.common.view.*
import io.ipoli.android.friends.feed.lightenText
import io.ipoli.android.friends.feed.post.AddPostViewState.StateType.*
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.player.data.AndroidAvatar
import io.ipoli.android.quest.Quest
import kotlinx.android.synthetic.main.item_feed.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import java.io.ByteArrayOutputStream

sealed class AddPostAction : Action {
    data class ImagePicked(val imagePath: Uri) : AddPostAction()

    data class Load(val questId: String?, val habitId: String?, val challengeId: String?) :
        AddPostAction()

    data class Save(
        val playerMessage: String?,
        val imageData: ByteArray?
    ) : AddPostAction()
}

object AddPostReducer : BaseViewStateReducer<AddPostViewState>() {
    override val stateKey = key<AddPostViewState>()

    override fun reduce(
        state: AppState,
        subState: AddPostViewState,
        action: Action
    ) =
        when (action) {

            is AddPostAction.Load ->
                subState.copy(type = LOADING)

            is DataLoadedAction.AddPostDataChanged -> {
                val player = action.player
                subState.copy(
                    type = DATA_CHANGED,
                    quest = action.quest,
                    habit = action.habit,
                    challenge = action.challenge,
                    petAvatar = AndroidPetAvatar.valueOf(player.pet.avatar.name),
                    playerAvatar = AndroidAvatar.valueOf(player.avatar.name),
                    playerName = player.displayName,
                    playerUsername = player.username,
                    playerLevel = player.level
                )
            }

            is AddPostAction.ImagePicked ->
                subState.copy(
                    type = IMAGE_CHANGED,
                    imagePath = action.imagePath
                )

            else -> subState
        }

    override fun defaultState() = AddPostViewState(
        type = LOADING,
        petAvatar = null,
        playerAvatar = null,
        playerName = null,
        playerUsername = null,
        playerLevel = null,
        imagePath = null,
        quest = null,
        habit = null,
        challenge = null
    )

}

data class AddPostViewState(
    val type: StateType,
    val petAvatar: AndroidPetAvatar?,
    val playerAvatar: AndroidAvatar?,
    val playerName: String?,
    val playerUsername: String?,
    val playerLevel: Int?,
    val imagePath: Uri?,
    val quest: Quest?,
    val habit: Habit?,
    val challenge: Challenge?
) : BaseViewState() {

    enum class StateType {
        LOADING,
        DATA_CHANGED,
        IMAGE_CHANGED
    }
}

class AddPostDialogController(args: Bundle? = null) :
    ReduxDialogController<AddPostAction, AddPostViewState, AddPostReducer>(args) {

    override val reducer = AddPostReducer

    companion object {
        const val PICK_IMAGE_REQUEST_CODE = 42
    }

    private var questId: String? = null
    private var habitId: String? = null
    private var challengeId: String? = null
    private var postListener: (() -> Unit)? = null

    constructor(
        questId: String?,
        habitId: String?,
        challengeId: String?,
        postListener: (() -> Unit)? = null
    ) : this() {
        this.questId = questId
        this.habitId = habitId
        this.challengeId = challengeId
        this.postListener = postListener
    }

    @SuppressLint("InflateParams")
    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_add_post, null)
        view.feedItemContainer.background = null

        registerForActivityResult(PICK_IMAGE_REQUEST_CODE)
        view.pickPostImage.onDebounceClick {
            activity?.showImagePicker(PICK_IMAGE_REQUEST_CODE)
        }

        view.divider.gone()
        view.comment.gone()
        view.share.gone()
        view.reactionGroup.goneViews()
        return view
    }

    override fun onCreateLoadAction() =
        AddPostAction.Load(
            questId = questId,
            habitId = habitId,
            challengeId = challengeId
        )

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.add_post_title)
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setPositiveButton(R.string.dialog_ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == PICK_IMAGE_REQUEST_CODE)
            && resultCode == RESULT_OK && data != null && data.data != null
        ) {
            dispatch(AddPostAction.ImagePicked(data.data))
        }
    }

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            setPositiveButtonListener {

                contentView.postImage.isDrawingCacheEnabled = true
                contentView.postImage.buildDrawingCache()

                val imageData = contentView.postImage.drawable?.let {
                    val bm = (contentView.postImage.drawable as BitmapDrawable).bitmap
                    val baos = ByteArrayOutputStream()
                    bm.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    baos.toByteArray()
                }

                dispatch(
                    AddPostAction.Save(
                        contentView.playerMessageEdit.text.toString(),
                        imageData
                    )
                )
                dismiss()
                postListener?.invoke()
            }
        }
    }

    override fun render(state: AddPostViewState, view: View) {
        when (state.type) {

            DATA_CHANGED -> {
                changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
                renderPlayer(view, state)
                view.postedTime.text = "Now"
                renderMessage(view, state)

                view.pickPostImage.visible()
                if (state.imagePath == null) {
                    view.postImage.gone()
                } else {
                    view.postImage.loadFromFile(state.imagePath)
                    view.postImage.visible()
                }

            }

            else -> {
            }
        }
    }

    private fun renderMessage(
        view: View,
        state: AddPostViewState
    ) {
        view.messageGroup.visible()
        (view.complexGroup.views() + view.feedBigPicture + view.feedAchievement).map { it.gone() }
        view.challengeGroup.gone()

        val vm = state.postViewModel
        view.feedMessage.text = vm.text
        view.feedMessageIcon.setImageResource(vm.icon)


        if (vm is PostViewModel.QuestViewModel && vm.challengeName != null) {
            view.feedChallengeTitle.text = vm.challengeName
            view.challengeGroup.visible()
            val challengeGradientDrawable =
                view.feedChallengeIconBackground.background as GradientDrawable
            challengeGradientDrawable.mutate()
            challengeGradientDrawable.setColor(colorRes(R.color.md_blue_500))
        }
    }

    private fun renderPlayer(
        view: View,
        state: AddPostViewState
    ) {
        Glide.with(view.context).load(state.playerAvatar!!.image)
            .apply(RequestOptions.circleCropTransform())
            .into(view.playerAvatar)

        val gradientDrawable = view.playerAvatar.background as GradientDrawable
        gradientDrawable.mutate()
        gradientDrawable.setColor(colorRes(state.playerAvatar.backgroundColor))

        view.playerName.text =
            if (state.playerName.isNullOrBlank()) stringRes(R.string.unknown_hero) else state.playerName
        @SuppressLint("SetTextI18n")
        view.playerUsername.text = "@${state.playerUsername}"
        view.playerLevel.text = "Level ${state.playerLevel}"
    }

    sealed class PostViewModel {
        abstract val text: SpannableString
        abstract val icon: Int

        data class QuestViewModel(
            override val text: SpannableString,
            override val icon: Int,
            val challengeName: String? = null
        ) : PostViewModel()

        data class HabitViewModel(
            override val text: SpannableString,
            override val icon: Int,
            val challengeName: String? = null
        ) : PostViewModel()

        data class ChallengeViewModel(
            override val text: SpannableString,
            override val icon: Int
        ) : PostViewModel()
    }

    private val AddPostViewState.postViewModel: PostViewModel
        get() = when {
            quest != null && !quest.isFromChallenge && !quest.hasPomodoroTimer -> {
                val completedQuest = stringRes(R.string.feed_completed_quest)
                val message = "$completedQuest ${quest.name}"

                val trackedDuration =
                    if (quest.hasTimer) quest.actualDuration.asMinutes else 0.minutes
                val finalMessage = if (trackedDuration.intValue > 0) {
                    val duration = stringRes(
                        R.string.feed_timer, DurationFormatter.formatShort(
                            activity!!,
                            trackedDuration.intValue
                        )
                    )
                    activity!!.lightenText(message + duration, completedQuest, duration)
                } else
                    activity!!.lightenText(message, completedQuest)

                PostViewModel.QuestViewModel(finalMessage, R.drawable.ic_post_done)
            }

            quest != null && quest.isFromChallenge && !quest.hasPomodoroTimer -> {
                val completedQuest = stringRes(R.string.feed_completed_quest)
                val message = "$completedQuest ${quest.name}"
                val durationTracked =
                    if (quest.hasTimer) quest.actualDuration.asMinutes else 0.minutes

                val finalMessage = if (durationTracked.intValue > 0) {
                    val duration = stringRes(
                        R.string.feed_timer, DurationFormatter.formatShort(
                            activity!!,
                            durationTracked.intValue
                        )
                    )
                    activity!!.lightenText(message + duration, completedQuest, duration)
                } else
                    activity!!.lightenText(message, completedQuest)

                PostViewModel.QuestViewModel(
                    finalMessage,
                    R.drawable.ic_post_done,
                    challenge!!.name
                )
            }

            quest != null && !quest.isFromChallenge -> {
                val completedQuest = stringRes(R.string.feed_completed_quest)
                val pomodoros = stringRes(R.string.feed_pomodoro_count, quest.totalPomodoros!!)
                val m = "$completedQuest ${quest.name} $pomodoros"
                PostViewModel.QuestViewModel(
                    activity!!.lightenText(m, completedQuest, pomodoros),
                    R.drawable.ic_post_done
                )
            }

            quest != null && quest.isFromChallenge -> {
                val completedQuest = stringRes(R.string.feed_completed_quest)
                val pomodoros = stringRes(R.string.feed_pomodoro_count, quest.totalPomodoros!!)
                val m = "$completedQuest ${quest.name} $pomodoros"
                PostViewModel.QuestViewModel(
                    activity!!.lightenText(m, completedQuest, pomodoros),
                    R.drawable.ic_post_done,
                    challenge!!.name
                )
            }

            habit != null -> {
                val streak = habit.streak.current
                val bestStreak = habit.streak.best
                val message = when {
                    streak == 1 -> {
                        val completedHabit = stringRes(R.string.feed_completed_habit)
                        val m = "$completedHabit ${habit.name}"
                        activity!!.lightenText(m, completedHabit)
                    }
                    streak < bestStreak -> {
                        val completedHabit = stringRes(R.string.feed_completed_habit)
                        val timesInARow = stringRes(R.string.feed_times_in_a_row)
                        val m =
                            "$completedHabit ${habit.name} ${streak} $timesInARow"
                        activity!!.lightenText(m, completedHabit, timesInARow)
                    }
                    else -> {
                        val newBestStreak = stringRes(R.string.feed_best_streak_habit)
                        val timesInARow = stringRes(R.string.feed_times_in_a_row)
                        val m =
                            "$newBestStreak ${habit.name}: ${bestStreak} $timesInARow"
                        activity!!.lightenText(m, newBestStreak, timesInARow)
                    }
                }
                PostViewModel.HabitViewModel(
                    message,
                    R.drawable.ic_post_habit,
                    if (habit.isFromChallenge) challenge!!.name else null
                )
            }

            challenge != null && !challenge.isCompleted -> {
                val accepted = stringRes(R.string.feed_accepted_challenge)
                val m = "$accepted ${challenge.name}"
                PostViewModel.ChallengeViewModel(
                    activity!!.lightenText(m, accepted),
                    R.drawable.ic_post_challenge
                )
            }

            challenge != null && challenge.isCompleted -> {
                val completed = stringRes(R.string.feed_completed_challenge)
                val m = "$completed ${challenge.name}"
                PostViewModel.ChallengeViewModel(
                    activity!!.lightenText(m, completed),
                    R.drawable.ic_post_challenge
                )
            }

            else -> throw IllegalArgumentException()
        }
}