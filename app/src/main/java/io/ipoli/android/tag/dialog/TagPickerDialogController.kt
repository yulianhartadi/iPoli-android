package io.ipoli.android.tag.dialog

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import io.ipoli.android.R
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.view.ReduxDialogController
import io.ipoli.android.common.view.normalIcon
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.tag.Tag
import io.ipoli.android.tag.dialog.TagPickerViewState.StateType.*
import kotlinx.android.synthetic.main.dialog_tag_picker.view.*
import kotlinx.android.synthetic.main.item_tag_picker_favourite.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/1/18.
 */
sealed class TagPickerAction : Action {
    object Load : TagPickerAction()
    object Switch : TagPickerAction()
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
                tags = state.dataState.tags.filter { it.isFavorite }
            )
        }

        is TagPickerAction.Switch -> {
            subState.copy(
                type = SWITCH,
                showAll = !subState.showAll
            )
        }
        else -> subState
    }

    override fun defaultState() = TagPickerViewState(
        type = LOADING,
        tags = emptyList(),
        showAll = false
    )


}

data class TagPickerViewState(
    val type: StateType,
    val tags: List<Tag>,
    val showAll: Boolean
) : ViewState {
    enum class StateType {
        LOADING, DATA_LOADED, SWITCH
    }
}

class TagPickerDialogController(args: Bundle? = null) :
    ReduxDialogController<TagPickerAction, TagPickerViewState, TagPickerReducer>(args) {
    override val reducer = TagPickerReducer

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_tag_picker, null)
        view.favouriteTagList.layoutManager = LinearLayoutManager(activity!!)
        view.favouriteTagList.adapter = FavouriteTagAdapter()
        return view
    }

    override fun onCreateLoadAction() = TagPickerAction.Load

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
                dismiss()
            }

            setNeutralButtonListener {
                dispatch(TagPickerAction.Switch)
            }
        }
    }

    override fun render(state: TagPickerViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                (view.favouriteTagList.adapter as FavouriteTagAdapter).updateAll(state.favouriteViewModels)
            }

            SWITCH -> {
                view.container.showNext()
                if (state.showAll) {
                    changeNeutralButtonText(R.string.favorite)
                } else {
                    changeNeutralButtonText(R.string.all)
                }
            }
        }
    }

    data class FavouriteTagViewModel(
        val name: String,
        val icon: IIcon,
        val color: Int,
        val tag: Tag
    )

    inner class FavouriteTagAdapter :
        BaseRecyclerViewAdapter<FavouriteTagViewModel>(R.layout.item_tag_picker_favourite) {
        override fun onBindViewModel(
            vm: FavouriteTagViewModel,
            view: View,
            holder: SimpleViewHolder
        ) {
            view.tagName.text = vm.name
            view.tagName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                IconicsDrawable(view.context)
                    .normalIcon(
                        vm.icon,
                        vm.color
                    ).respectFontBounds(true),
                null, null, null
            )
        }

    }

    private val TagPickerViewState.favouriteViewModels: List<FavouriteTagViewModel>
        get() = tags.map {
            FavouriteTagViewModel(
                name = it.name,
                icon = it.icon?.androidIcon?.icon ?: MaterialDesignIconic.Icon.gmi_label,
                color = it.color.androidColor.color500,
                tag = it
            )
        }


//    private val TagPickerViewState.petHeadImage
//        get() = AndroidPetAvatar.valueOf(petAvatar.name).headImage

}