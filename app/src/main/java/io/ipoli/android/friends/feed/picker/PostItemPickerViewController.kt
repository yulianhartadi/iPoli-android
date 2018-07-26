package io.ipoli.android.friends.feed.picker

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.TextView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.ionicons_typeface_library.Ionicons
import io.ipoli.android.R
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.recyclerview.MultiViewRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.RecyclerViewViewModel
import io.ipoli.android.friends.feed.picker.ItemToSharePickerViewState.StateType.DATA_CHANGED
import kotlinx.android.synthetic.main.controller_item_to_share_picker.view.*
import kotlinx.android.synthetic.main.item_challenge_share_picker.view.*
import kotlinx.android.synthetic.main.item_quest_share_picker.view.*
import kotlinx.android.synthetic.main.view_default_toolbar.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 7/16/18.
 */
class PostItemPickerViewController(args: Bundle? = null) :
    ReduxViewController<PostItemPickerAction, ItemToSharePickerViewState, PostItemPickerReducer>(
        args
    ) {
    override val reducer = PostItemPickerReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_item_to_share_picker, container, false)
        setToolbar(view.toolbar)
        toolbarTitle = "Share with friends"

        view.itemToShareList.layoutManager = LinearLayoutManager(view.context)
        view.itemToShareList.adapter = ItemAdapter()

        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()
    }

    override fun onCreateLoadAction() = PostItemPickerAction.Load

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.items_to_share_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home ->
                router.handleBack()

            R.id.actionShare -> {
                dispatch(PostItemPickerAction.Share)
                router.handleBack()
            }

            else -> super.onOptionsItemSelected(item)
        }

    override fun render(state: ItemToSharePickerViewState, view: View) {
        when (state.type) {
            DATA_CHANGED -> {
                (view.itemToShareList.adapter as ItemAdapter).updateAll(state.viewModels)
            }

            else -> {
            }
        }
    }

    enum class ItemType {
        HEADER, QUEST, CHALLENGE
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
            val isSelected: Boolean
        ) : ItemViewModel()

        data class ChallengeViewModel(
            override val id: String,
            val name: String,
            val color: Int,
            val icon: IIcon,
            val isSelected: Boolean
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

                view.questCheck.setOnCheckedChangeListener(null)
                view.questCheck.isChecked = vm.isSelected
                view.questCheck.setOnCheckedChangeListener { _, isChecked ->
                    dispatch(
                        if (isChecked) PostItemPickerAction.SelectQuest(vm.id)
                        else PostItemPickerAction.DeselectQuest(vm.id)
                    )
                }
                view.onDebounceClick {
                    dispatch(
                        if (!vm.isSelected) PostItemPickerAction.SelectQuest(vm.id)
                        else PostItemPickerAction.DeselectQuest(vm.id)
                    )
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

                view.challengeCheck.setOnCheckedChangeListener(null)
                view.challengeCheck.isChecked = vm.isSelected
                view.challengeCheck.setOnCheckedChangeListener { _, isChecked ->
                    dispatch(
                        if (isChecked) PostItemPickerAction.SelectChallenge(vm.id)
                        else PostItemPickerAction.DeselectChallenge(vm.id)
                    )
                }
                view.onDebounceClick {
                    dispatch(
                        if (!vm.isSelected) PostItemPickerAction.SelectChallenge(vm.id)
                        else PostItemPickerAction.DeselectChallenge(vm.id)
                    )
                }
            }
        }
    }

    private val ItemToSharePickerViewState.viewModels: List<ItemViewModel>
        get() {
            val vms = mutableListOf<ItemViewModel>()
            vms.add(
                ItemViewModel.HeaderViewModel("Today completed quests", "Today completed quests")
            )
            vms.addAll(quests!!.map {
                ItemViewModel.QuestViewModel(
                    id = it.id,
                    name = it.name,
                    color = it.color.androidColor.color500,
                    icon = it.icon?.androidIcon?.icon ?: Ionicons.Icon.ion_android_clipboard,
                    isSelected = selectedQuestIds.contains(it.id)
                )
            })
            vms.add(ItemViewModel.HeaderViewModel("Challenges", "Challenges"))

            vms.addAll(challenges!!.map {
                ItemViewModel.ChallengeViewModel(
                    id = it.id,
                    name = it.name,
                    color = it.color.androidColor.color500,
                    icon = it.icon?.androidIcon?.icon ?: Ionicons.Icon.ion_android_clipboard,
                    isSelected = selectedChallengeIds.contains(it.id)
                )
            })

            return vms
        }

}