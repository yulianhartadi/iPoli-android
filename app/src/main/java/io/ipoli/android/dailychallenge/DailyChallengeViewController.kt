package io.ipoli.android.dailychallenge

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.*
import android.widget.TextView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.ionicons_typeface_library.Ionicons
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.text.QuestStartTimeFormatter
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.RecyclerViewViewModel
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.dailychallenge.DailyChallengeViewState.StateType.DATA_CHANGED
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.PetState
import io.ipoli.android.quest.schedule.addquest.AddQuestAnimationHelper
import kotlinx.android.synthetic.main.controller_daily_challenge.view.*
import kotlinx.android.synthetic.main.item_plan_today_quest.view.*
import kotlinx.android.synthetic.main.view_loader.view.*
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/24/18.
 */
class DailyChallengeViewController(args: Bundle? = null) :
    ReduxViewController<DailyChallengeAction, DailyChallengeViewState, DailyChallengeReducer>(args) {
    override val reducer = DailyChallengeReducer

    private lateinit var addQuestAnimationHelper: AddQuestAnimationHelper

    private var isComplete = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_daily_challenge, container, false)
        setToolbar(view.toolbar)
        val collapsingToolbar = view.collapsingToolbarContainer
        collapsingToolbar.isTitleEnabled = false
        view.toolbar.title = stringRes(R.string.daily_challenge_title)

        view.selectedQuests.layoutManager = LinearLayoutManager(
            container.context,
            LinearLayoutManager.VERTICAL,
            false
        )
        view.selectedQuests.adapter = QuestAdapter()

        view.todayQuests.layoutManager = LinearLayoutManager(
            container.context,
            LinearLayoutManager.VERTICAL,
            false
        )
        view.todayQuests.adapter = QuestAdapter()

        initAddQuest(view)

        view.descriptionIcon.setImageDrawable(
            IconicsDrawable(activity!!)
                .icon(GoogleMaterial.Icon.gmd_info_outline)
                .color(attrData(R.attr.colorAccent))
                .sizeDp(24)
        )

        return view
    }

    private fun initAddQuest(view: View) {
        addQuestAnimationHelper = AddQuestAnimationHelper(
            controller = this,
            addContainer = view.addContainer,
            fab = view.addQuest,
            background = view.addContainerBackground
        )

        view.addContainerBackground.setOnClickListener {
            getChildRouter(view.addContainer, "add-quest").popCurrentController()
            ViewUtils.hideKeyboard(view)
            addQuestAnimationHelper.closeAddContainer()
        }

        view.addQuest.setOnClickListener {
            addQuestAnimationHelper.openAddContainer(LocalDate.now())
        }
    }

    override fun onCreateLoadAction() = DailyChallengeAction.Load

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.daily_challenge_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.actionSave).isVisible = !isComplete
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                return router.handleBack()

            R.id.actionSave -> {
                dispatch(DailyChallengeAction.Save)
                return router.handleBack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun render(state: DailyChallengeViewState, view: View) {
        when (state.type) {

            DATA_CHANGED -> {
                isComplete = state.isCompleted
                if (isComplete) {
                    view.addQuest.invisible()
                } else {
                    view.addQuest.visible()
                }
                view.loader.gone()
                view.selectedQuestsContainer.visible()
                view.todayQuestsContainer.visible()

                if (state.isCompleted) {
                    view.todayQuestsContainer.gone()
                    view.firstAndSecondQuestConnection.setBackgroundColor(attrData(R.attr.colorAccent))
                    view.secondAndThirdQuestConnection.setBackgroundColor(attrData(R.attr.colorAccent))
                } else {
                    (view.todayQuests.adapter as QuestAdapter).updateAll(state.todayViewModels)
                    if (state.todayQuests!!.isEmpty()) {
                        view.emptyDailyQuests.visible()
                    } else {
                        view.emptyDailyQuests.gone()
                    }
                }

                (view.selectedQuests.adapter as QuestAdapter).updateAll(state.selectedViewModels)
                if (state.selectedQuests!!.isEmpty()) {
                    view.emptySelectedQuests.visible()
                } else {
                    view.emptySelectedQuests.gone()
                }

                renderPet(view, state)
                activity?.invalidateOptionsMenu()
            }

            else -> {
            }
        }
    }

    private fun renderPet(view: View, state: DailyChallengeViewState) {
        view.dailyChallengePet.setImageResource(state.petAvatarImage)
        view.dailyChallengePetState.setImageResource(state.petAvatarStateImage)
        view.dailyChallengePet.visible()
        view.dailyChallengePetState.visible()
        view.firstQuest.setBackgroundResource(state.dailyChallengeQuest1Indicator)
        view.secondQuest.setBackgroundResource(state.dailyChallengeQuest2Indicator)
        view.thirdQuest.setBackgroundResource(state.dailyChallengeQuest3Indicator)
        view.selectedQuestsCount.text = state.selectedCountText
        view.selectedQuestsCount.setTextColor(state.selectedCountTextColor)
    }

    data class TagViewModel(val name: String, @ColorRes val color: Int)

    data class QuestItem(
        override val id: String,
        val name: String,
        val startTime: String,
        @ColorRes val color: Int,
        val tags: List<TagViewModel>,
        val icon: IIcon,
        val isRepeating: Boolean,
        val isFromChallenge: Boolean,
        val isForDailyChallenge: Boolean,
        val isCompleted: Boolean,
        val isSelectable: Boolean
    ) : RecyclerViewViewModel

    inner class QuestAdapter :
        BaseRecyclerViewAdapter<QuestItem>(R.layout.item_daily_challenge_quest) {
        override fun onBindViewModel(vm: QuestItem, view: View, holder: SimpleViewHolder) {
            if (vm.isCompleted) {
                val span = SpannableString(vm.name)
                span.setSpan(StrikethroughSpan(), 0, vm.name.length, 0)
                view.questName.text = span
            } else {
                view.questName.text = vm.name
            }

            view.questIcon.backgroundTintList =
                ColorStateList.valueOf(colorRes(vm.color))
            view.questIcon.setImageDrawable(listItemIcon(vm.icon))

            if (vm.tags.isNotEmpty()) {
                view.questTagName.visible()
                renderTag(view.questTagName, vm.tags.first())
            } else {
                view.questTagName.gone()
            }

            view.questStartTime.text = vm.startTime

            view.questRepeatIndicator.visibility = if (vm.isRepeating) View.VISIBLE else View.GONE
            view.questChallengeIndicator.visibility =
                if (vm.isFromChallenge) View.VISIBLE else View.GONE

            if (vm.isSelectable) {
                view.questStar.visible()
                if (vm.isForDailyChallenge) {
                    view.questStar.setImageResource(R.drawable.ic_star_accent_24dp)
                    view.questStar.onDebounceClick {
                        dispatch(DailyChallengeAction.RemoveQuest(vm.id))
                    }
                } else {
                    view.questStar.setImageResource(R.drawable.ic_star_border_black_24dp)
                    view.questStar.onDebounceClick {
                        dispatch(DailyChallengeAction.AddQuest(vm.id))
                    }
                }
            } else {
                view.questStar.gone()
            }

        }
    }

    private fun renderTag(tagNameView: TextView, tag: TagViewModel) {
        tagNameView.visible()
        tagNameView.text = tag.name
        TextViewCompat.setTextAppearance(
            tagNameView,
            R.style.TextAppearance_AppCompat_Caption
        )

        val indicator = tagNameView.compoundDrawablesRelative[0] as GradientDrawable
        indicator.mutate()
        val size = ViewUtils.dpToPx(8f, tagNameView.context).toInt()
        indicator.setSize(size, size)
        indicator.setColor(colorRes(tag.color))
        tagNameView.setCompoundDrawablesRelativeWithIntrinsicBounds(
            indicator,
            null,
            null,
            null
        )
    }

    private val DailyChallengeViewState.todayViewModels: List<QuestItem>
        get() =
            todayQuests!!.map {
                QuestItem(
                    id = it.id,
                    name = it.name,
                    tags = it.tags.map {
                        TagViewModel(
                            it.name,
                            AndroidColor.valueOf(it.color.name).color500
                        )
                    },
                    startTime = QuestStartTimeFormatter.formatWithDuration(
                        it,
                        activity!!,
                        shouldUse24HourFormat
                    ),
                    color = if (it.isCompleted) R.color.md_grey_500 else it.color.androidColor.color500,
                    icon = it.icon?.androidIcon?.icon
                        ?: Ionicons.Icon.ion_android_clipboard,
                    isRepeating = it.isFromRepeatingQuest,
                    isFromChallenge = it.isFromChallenge,
                    isForDailyChallenge = false,
                    isCompleted = it.isCompleted,
                    isSelectable = true
                )
            }

    private val DailyChallengeViewState.selectedViewModels: List<QuestItem>
        get() =
            selectedQuests!!.map {
                QuestItem(
                    id = it.id,
                    name = it.name,
                    tags = it.tags.map {
                        TagViewModel(
                            it.name,
                            AndroidColor.valueOf(it.color.name).color500
                        )
                    },
                    startTime = QuestStartTimeFormatter.formatWithDuration(
                        it,
                        activity!!,
                        shouldUse24HourFormat
                    ),
                    color = if (it.isCompleted) R.color.md_grey_500 else it.color.androidColor.color500,
                    icon = it.icon?.androidIcon?.icon
                        ?: Ionicons.Icon.ion_android_clipboard,
                    isRepeating = it.isFromRepeatingQuest,
                    isFromChallenge = it.isFromChallenge,
                    isForDailyChallenge = true,
                    isCompleted = it.isCompleted,
                    isSelectable = !isCompleted
                )
            }


    private val DailyChallengeViewState.petAvatarImage: Int
        get() = AndroidPetAvatar.valueOf(petAvatar!!.name).image

    private val DailyChallengeViewState.petAvatarStateImage: Int
        get() {
            val stateImage = AndroidPetAvatar.valueOf(petAvatar!!.name).stateImage
            return when (completedQuests) {
                3 -> stateImage[PetState.AWESOME]!!
                2 -> stateImage[PetState.HAPPY]!!
                1 -> stateImage[PetState.GOOD]!!
                0 -> stateImage[PetState.SAD]!!
                else -> throw IllegalStateException("Unexpected daily challenge quests count $completedQuests")
            }
        }

    private val DailyChallengeViewState.completedQuests: Int
        get() = if (isCompleted) {
            Constants.DAILY_CHALLENGE_QUEST_COUNT
        } else {
            selectedQuests!!.filter { it.isCompleted }.size
        }

    private val DailyChallengeViewState.dailyChallengeQuest1Indicator: Int
        get() =
            when {
                isCompleted -> R.drawable.circle_daily_challenge_progress_complete
                completedQuests > 0 -> R.drawable.circle_daily_challenge_progress_filled
                else -> R.drawable.circle_daily_challenge_progress_empty
            }

    private val DailyChallengeViewState.dailyChallengeQuest2Indicator: Int
        get() =
            when {
                isCompleted -> R.drawable.circle_daily_challenge_progress_complete
                completedQuests > 1 -> R.drawable.circle_daily_challenge_progress_filled
                else -> R.drawable.circle_daily_challenge_progress_empty
            }

    private val DailyChallengeViewState.dailyChallengeQuest3Indicator: Int
        get() =
            when {
                isCompleted -> R.drawable.circle_daily_challenge_progress_complete
                completedQuests > 2 -> R.drawable.circle_daily_challenge_progress_filled
                else -> R.drawable.circle_daily_challenge_progress_empty
            }

    private val DailyChallengeViewState.selectedCountTextColor: Int
        get() {
            val count = selectedQuests!!.size
            return when {
                isCompleted -> attrData(R.attr.colorAccent)
                count == Constants.DAILY_CHALLENGE_QUEST_COUNT -> attrData(R.attr.colorPrimary)
                else -> colorRes(R.color.md_dark_text_54)
            }
        }

    private val DailyChallengeViewState.selectedCountText: String
        get() {
            val count = selectedQuests!!.size
            return when {
                isCompleted -> stringRes(R.string.daily_challenge_complete)
                count == Constants.DAILY_CHALLENGE_QUEST_COUNT -> stringRes(R.string.daily_challenge_active)
                else -> stringRes(
                    R.string.selected_daily_challenge_count,
                    count,
                    Constants.DAILY_CHALLENGE_QUEST_COUNT
                )
            }
        }
}