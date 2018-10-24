package io.ipoli.android.friends.feed.picker

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.ionicons_typeface_library.Ionicons
import io.ipoli.android.R
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.recyclerview.MultiViewRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.RecyclerViewViewModel
import io.ipoli.android.friends.feed.picker.PostItemPickerViewState.StateType.DATA_CHANGED
import io.ipoli.android.friends.feed.picker.PostItemPickerViewState.StateType.LOADING
import kotlinx.android.synthetic.main.controller_post_item_picker.view.*
import kotlinx.android.synthetic.main.item_challenge_share_picker.view.*
import kotlinx.android.synthetic.main.item_empty_share_picker.view.*
import kotlinx.android.synthetic.main.item_habit_share_picker.view.*
import kotlinx.android.synthetic.main.item_quest_share_picker.view.*
import kotlinx.android.synthetic.main.view_default_toolbar.view.*
import kotlinx.android.synthetic.main.view_loader.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 7/16/18.
 */
class PostItemPickerViewController(args: Bundle? = null) :
    ReduxViewController<PostItemPickerAction, PostItemPickerViewState, PostItemPickerReducer>(
        args
    ) {
    override val reducer = PostItemPickerReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = container.inflate(R.layout.controller_post_item_picker)
        setToolbar(view.toolbar)
        toolbarTitle = "Share your achievements"

        view.itemToShareList.layoutManager = LinearLayoutManager(view.context)
        view.itemToShareList.adapter = ItemAdapter()

        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()
    }

    override fun onCreateLoadAction() = PostItemPickerAction.Load


    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home ->
                router.handleBack()

            else -> super.onOptionsItemSelected(item)
        }

    override fun render(state: PostItemPickerViewState, view: View) {
        when (state.type) {

            LOADING -> {
                view.loader.visible()
                view.itemToShareContainer.gone()
            }

            DATA_CHANGED -> {
                view.loader.gone()
                view.itemToShareContainer.visible()
                (view.itemToShareList.adapter as ItemAdapter).updateAll(state.viewModels)
            }
        }
    }

    enum class ItemType {
        HEADER, QUEST, CHALLENGE, HABIT, EMPTY
    }

    sealed class ItemViewModel : RecyclerViewViewModel {
        data class HeaderViewModel(
            override val id: String,
            val title: String
        ) : ItemViewModel()

        data class QuestViewModel(
            override val id: String,
            val name: String,
            val color: Int,
            val icon: IIcon,
            val challengeId: String?
        ) : ItemViewModel()

        data class HabitViewModel(
            override val id: String,
            val name: String,
            val color: Int,
            val icon: IIcon,
            val streak: String,
            val challengeId: String?
        ) : ItemViewModel()

        data class ChallengeViewModel(
            override val id: String,
            val name: String,
            val color: Int,
            val icon: IIcon
        ) : ItemViewModel()

        data class EmptyViewModel(
            override val id: String,
            val title: String
        ) : ItemViewModel()
    }

    inner class ItemAdapter : MultiViewRecyclerViewAdapter<ItemViewModel>() {

        override fun onRegisterItemBinders() {
            registerBinder<ItemViewModel.HeaderViewModel>(
                ItemType.HEADER.ordinal,
                R.layout.item_list_section
            ) { vm, view, _ ->
                (view as TextView).text = vm.title
            }

            registerBinder<ItemViewModel.QuestViewModel>(
                ItemType.QUEST.ordinal,
                R.layout.item_quest_share_picker
            ) { vm, view, _ ->
                view.questName.text = vm.name
                view.questIcon.backgroundTintList =
                    ColorStateList.valueOf(colorRes(vm.color))

                val icon = IconicsDrawable(view.context).listItemIcon(vm.icon)

                view.questIcon.setImageDrawable(icon)

                view.onDebounceClick {
                    navigateFromRoot().toAddPost(
                        questId = vm.id,
                        habitId = null,
                        challengeId = vm.challengeId
                    ) {
                        router.handleBack()
                    }
                }
            }

            registerBinder<ItemViewModel.HabitViewModel>(
                ItemType.HABIT.ordinal,
                R.layout.item_habit_share_picker
            ) { vm, view, _ ->
                view.habitName.text = vm.name
                view.habitIcon.backgroundTintList =
                    ColorStateList.valueOf(colorRes(vm.color))
                val icon = IconicsDrawable(view.context).listItemIcon(vm.icon)
                view.habitIcon.setImageDrawable(icon)

                view.habitStreak.text = vm.streak

                view.onDebounceClick {
                    navigateFromRoot().toAddPost(
                        questId = null,
                        habitId = vm.id,
                        challengeId = vm.challengeId
                    ) {
                        router.handleBack()
                    }
                }
            }

            registerBinder<ItemViewModel.ChallengeViewModel>(
                ItemType.CHALLENGE.ordinal,
                R.layout.item_challenge_share_picker
            ) { vm, view, _ ->
                view.challengeName.text = vm.name
                view.challengeIcon.backgroundTintList =
                    ColorStateList.valueOf(colorRes(vm.color))

                val icon = IconicsDrawable(view.context).listItemIcon(vm.icon)

                view.challengeIcon.setImageDrawable(icon)

                view.onDebounceClick {
                    navigateFromRoot().toAddPost(
                        questId = null,
                        habitId = null,
                        challengeId = vm.id
                    ) {
                        router.handleBack()
                    }
                }
            }

            registerBinder<ItemViewModel.EmptyViewModel>(
                ItemType.EMPTY.ordinal,
                R.layout.item_empty_share_picker
            ) { vm, view, _ ->
                view.postItemsEmpty.text = vm.title
            }

        }
    }

    private val PostItemPickerViewState.viewModels: List<ItemViewModel>
        get() {
            val vms = mutableListOf<ItemViewModel>()
            vms.add(
                ItemViewModel.HeaderViewModel("Today completed Quests", "Today completed Quests")
            )
            if(quests!!.isEmpty()) {
                vms.add(
                    ItemViewModel.EmptyViewModel(
                        id = "Complete some Quests to share",
                        title = "Complete some Quests to share"
                    )
                )
            } else {
                vms.addAll(quests.map {
                    ItemViewModel.QuestViewModel(
                        id = it.id,
                        name = it.name,
                        color = it.color.androidColor.color500,
                        icon = it.icon?.androidIcon?.icon ?: Ionicons.Icon.ion_checkmark,
                        challengeId = it.challengeId
                    )
                })
            }

            vms.add(
                ItemViewModel.HeaderViewModel(
                    "Today completed Habits",
                    "Today completed Habits"
                )
            )
            if(habits!!.isEmpty()) {
                vms.add(
                    ItemViewModel.EmptyViewModel(
                        id = "Complete some Habits to share",
                        title = "Complete some Habits to share"
                    )
                )
            } else {
                vms.addAll(habits.map {
                    ItemViewModel.HabitViewModel(
                        id = it.id,
                        name = it.name,
                        color = it.color.androidColor.color500,
                        icon = it.icon.androidIcon.icon,
                        streak = "streak ${it.streak.current}",
                        challengeId = it.challengeId
                    )
                })
            }


            vms.add(ItemViewModel.HeaderViewModel("Challenges", "Challenges"))

            if(challenges!!.isEmpty()) {
                vms.add(
                    ItemViewModel.EmptyViewModel(
                        id = "No Challenges to share",
                        title = "No Challenges to share"
                    )
                )
            } else {
                vms.addAll(challenges.map {
                    ItemViewModel.ChallengeViewModel(
                        id = it.id,
                        name = it.name,
                        color = it.color.androidColor.color500,
                        icon = it.icon?.androidIcon?.icon ?: Ionicons.Icon.ion_checkmark
                    )
                })
            }
            return vms
        }

}