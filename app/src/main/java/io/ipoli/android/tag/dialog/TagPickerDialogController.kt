package io.ipoli.android.tag.dialog

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.tag.Tag
import io.ipoli.android.tag.dialog.TagPickerViewState.StateType.*
import io.ipoli.android.tag.widget.EditItemAutocompleteTagAdapter
import io.ipoli.android.tag.widget.EditItemTagAdapter
import kotlinx.android.synthetic.main.dialog_tag_picker.view.*
import kotlinx.android.synthetic.main.item_tag_picker_favourite.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import timber.log.Timber

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/1/18.
 */
sealed class TagPickerAction : Action {
    data class AddFavouriteTag(val tag: Tag) : TagPickerAction()
    data class RemoveFavouriteTag(val tag: Tag) : TagPickerAction()
    data class RemoveTag(val tag: Tag) : TagPickerAction()
    data class AddTag(val tagName: String) : TagPickerAction()

    data class Load(val selectedTags: Set<Tag>) : TagPickerAction()
    object Switch : TagPickerAction()
    object Close : TagPickerAction()
}

object TagPickerReducer : BaseViewStateReducer<TagPickerViewState>() {
    override val stateKey = key<TagPickerViewState>()

    override fun reduce(
        state: AppState,
        subState: TagPickerViewState,
        action: Action
    ) = when (action) {
        is TagPickerAction.Load -> {
            subState.copy(
                type = DATA_LOADED,
                petAvatar = state.dataState.player!!.pet.avatar,
                tags = state.dataState.tags,
                favouriteTags = state.dataState.tags.filter { it.isFavorite },
                selectedTags = action.selectedTags
            )
        }

        is TagPickerAction.Switch -> {
            subState.copy(
                type = SWITCH,
                showAll = !subState.showAll
            )
        }

        is TagPickerAction.AddFavouriteTag -> {
            if (subState.maxTagsReached) {
                subState.copy(
                    type = SHOW_MAX_TAGS
                )
            } else {
                val selectedTags = subState.selectedTags + action.tag
                subState.copy(
                    type = TAGS_CHANGED,
                    selectedTags = selectedTags,
                    maxTagsReached = selectedTags.size >= Constants.MAX_TAGS_PER_ITEM
                )
            }

        }

        is TagPickerAction.RemoveFavouriteTag -> {
            val selectedTags = subState.selectedTags - action.tag
            subState.copy(
                type = TAGS_CHANGED,
                selectedTags = selectedTags,
                maxTagsReached = selectedTags.size >= Constants.MAX_TAGS_PER_ITEM
            )
        }

        is TagPickerAction.RemoveTag -> {
            val selectedTags = subState.selectedTags - action.tag
            subState.copy(
                type = TAGS_CHANGED,
                selectedTags = selectedTags,
                maxTagsReached = selectedTags.size >= Constants.MAX_TAGS_PER_ITEM
            )
        }

        is TagPickerAction.AddTag -> {
            val tag = subState.tags.first { it.name == action.tagName }
            val selectedTags = subState.selectedTags + tag
            subState.copy(
                type = TAGS_CHANGED,
                selectedTags = selectedTags,
                maxTagsReached = selectedTags.size >= Constants.MAX_TAGS_PER_ITEM
            )
        }

        is TagPickerAction.Close -> {
            subState.copy(
                type = CLOSE
            )
        }

        else -> subState
    }

    override fun defaultState() = TagPickerViewState(
        type = LOADING,
        petAvatar = PetAvatar.ELEPHANT,
        tags = emptyList(),
        favouriteTags = emptyList(),
        selectedTags = emptySet(),
        showAll = false,
        maxTagsReached = false
    )
}

data class TagPickerViewState(
    val type: StateType,
    val petAvatar: PetAvatar,
    val tags: List<Tag>,
    val favouriteTags: List<Tag>,
    val selectedTags: Set<Tag>,
    val showAll: Boolean,
    val maxTagsReached: Boolean
) : ViewState {

    enum class StateType {
        LOADING,
        DATA_LOADED,
        SWITCH,
        TAGS_CHANGED,
        SHOW_MAX_TAGS,
        CLOSE
    }
}

class TagPickerDialogController(args: Bundle? = null) :
    ReduxDialogController<TagPickerAction, TagPickerViewState, TagPickerReducer>(args) {
    override val reducer = TagPickerReducer

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_tag_picker, null)
        view.favouriteTagList.layoutManager = LinearLayoutManager(activity!!)
        view.favouriteTagList.adapter = FavouriteTagAdapter()

        view.allTagList.layoutManager = LinearLayoutManager(activity!!)
        view.allTagList.adapter = EditItemTagAdapter(removeTagCallback = {
            dispatch(TagPickerAction.RemoveTag(it))
        }, useWhiteTheme = false)

        return view
    }

    private lateinit var listener: (Set<Tag>) -> Unit

    private lateinit var selectedTags: Set<Tag>

    constructor(selectedTags: Set<Tag> = emptySet(), listener: (Set<Tag>) -> Unit) : this() {
        this.selectedTags = selectedTags
        this.listener = listener
    }


    override fun onCreateLoadAction() = TagPickerAction.Load(selectedTags)

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.tag_picker_title)
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog {
        return dialogBuilder
            .setPositiveButton(R.string.dialog_ok, null)
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.all, null)
            .create()
    }


    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            setPositiveButtonListener {
                dispatch(TagPickerAction.Close)
            }

            setNeutralButtonListener {
                dispatch(TagPickerAction.Switch)
            }
        }
    }

    override fun render(state: TagPickerViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                changeIcon(state.petHeadImage)
                renderFavouriteTags(view, state)
                renderAllTags(view, state)
            }

            SWITCH -> {
                view.container.showNext()
                if (state.showAll) {
                    changeNeutralButtonText(R.string.favorite)
                } else {
                    changeNeutralButtonText(R.string.all)
                }
            }

            TAGS_CHANGED -> {
                renderAllTags(view, state)
//                if (state.showAll) {
                    renderFavouriteTags(view, state)
//                }
            }

            SHOW_MAX_TAGS -> {
                renderFavouriteTags(view, state)
                showShortToast(R.string.max_tags_message)
            }

            CLOSE -> {
                listener(state.selectedTags)
                dismiss()
            }
        }
    }

    private fun renderFavouriteTags(
        view: View,
        state: TagPickerViewState
    ) {
        (view.favouriteTagList.adapter as FavouriteTagAdapter).updateAll(state.favouriteViewModels)
        Timber.d("AAAA ${(view.favouriteTagList.adapter as FavouriteTagAdapter).items}")
    }

    private fun renderAllTags(
        view: View,
        state: TagPickerViewState
    ) {
        (view.allTagList.adapter as EditItemTagAdapter).updateAll(state.tagViewModels)
        val add = view.addTag
        if (state.maxTagsReached) {
            add.gone()
//            view.maxTagsMessage.visible()
        } else {
            add.visible()
//            view.maxTagsMessage.gone()

            val adapter =
                EditItemAutocompleteTagAdapter(state.tags - state.selectedTags, activity!!)
            add.setAdapter(adapter)
            add.setOnItemClickListener { _, _, position, _ ->
                dispatch(TagPickerAction.AddTag(adapter.getItem(position).name))
                add.setText("")
            }
            add.threshold = 0
            add.setOnTouchListener { _, _ ->
                add.showDropDown()
                false
            }
        }
    }

    data class FavouriteTagViewModel(
        val name: String,
        val icon: Icon?,
        val color: Color,
        val isChecked: Boolean,
        val tag: Tag
    )

    inner class FavouriteTagAdapter :
        BaseRecyclerViewAdapter<FavouriteTagViewModel>(R.layout.item_tag_picker_favourite, object : DiffUtil.ItemCallback<FavouriteTagViewModel>() {
            override fun areItemsTheSame(
                oldItem: FavouriteTagViewModel?,
                newItem: FavouriteTagViewModel?
            ): Boolean {
                return oldItem?.tag?.id == newItem?.tag?.id
            }

            override fun areContentsTheSame(
                oldItem: FavouriteTagViewModel?,
                newItem: FavouriteTagViewModel?
            ): Boolean {
                return oldItem == newItem
            }

        }) {
        override fun onBindViewModel(
            vm: FavouriteTagViewModel,
            view: View,
            holder: SimpleViewHolder
        ) {
            Timber.d("AAA tag ${vm}")
            view.tagName.text = vm.name
            view.tagName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                IconicsDrawable(view.context)
                    .normalIcon(
                        vm.icon?.androidIcon?.icon ?: MaterialDesignIconic.Icon.gmi_label,
                        vm.color.androidColor.color500
                    ).respectFontBounds(true),
                null, null, null
            )
            view.tagCheckBox.setOnCheckedChangeListener(null)
            view.tagCheckBox.isChecked = vm.isChecked
            view.setOnClickListener {
                //                view.tagCheckBox.isChecked = !vm.isChecked
//                dispatch(TagPickerAction.ToggleFavouriteTagSelection(vm.tag, !vm.isChecked))
            }
            view.tagCheckBox.setOnCheckedChangeListener { _, isChecked ->
                Timber.d("AAAA selected ${vm.tag}")
                if (isChecked) {
                    dispatch(TagPickerAction.AddFavouriteTag(vm.tag))
                } else {
                    dispatch(TagPickerAction.RemoveFavouriteTag(vm.tag))
                }
            }

        }

    }

    private val TagPickerViewState.favouriteViewModels: List<FavouriteTagViewModel>
        get() = favouriteTags.map {
            Timber.d("AAA favourite ${selectedTags.contains(it)}" )
            FavouriteTagViewModel(
                name = it.name,
                icon = it.icon,
                color = it.color,
                isChecked = selectedTags.contains(it),
                tag = it
            )
        }

    private val TagPickerViewState.tagViewModels: List<EditItemTagAdapter.TagViewModel>
        get() = selectedTags.map {
            EditItemTagAdapter.TagViewModel(
                name = it.name,
                icon = it.icon?.androidIcon?.icon ?: MaterialDesignIconic.Icon.gmi_label,
                tag = it
            )
        }


    private val TagPickerViewState.petHeadImage
        get() = AndroidPetAvatar.valueOf(petAvatar.name).headImage

}