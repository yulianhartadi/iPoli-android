package io.ipoli.android.common.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.R
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.Player
import io.ipoli.android.player.usecase.ListenForPlayerChangesUseCase
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.IconPack
import kotlinx.android.synthetic.main.dialog_icon_picker.view.*
import kotlinx.android.synthetic.main.item_icon_picker.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Injects
import space.traversal.kapsule.required
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */

sealed class IconPickerIntent : Intent {
    data class LoadData(val selectedIcon: Icon? = null) : IconPickerIntent()
    data class ChangePlayer(val player: Player) : IconPickerIntent()
}


data class IconPickerViewState(
    val type: Type,
    val petAvatar: PetAvatar? = null,
    val selectedIcon: Icon? = null,
    val viewModels: List<IconPickerDialogController.IconViewModel> = listOf()
) : ViewState {
    enum class Type {
        LOADING,
        DATA_LOADED,
        PLAYER_CHANGED
    }
}

class IconPickerDialogPresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    coroutineContext: CoroutineContext) :
    BaseMviPresenter<ViewStateRenderer<IconPickerViewState>, IconPickerViewState, IconPickerIntent>(
        IconPickerViewState(IconPickerViewState.Type.LOADING),
        coroutineContext
    ) {
    override fun reduceState(intent: IconPickerIntent, state: IconPickerViewState): IconPickerViewState {
        return when (intent) {
            is IconPickerIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(IconPickerIntent.ChangePlayer(it))
                    }
                }
                state.copy(
                    selectedIcon = intent.selectedIcon
                )
            }

            is IconPickerIntent.ChangePlayer -> {
                val player = intent.player
                val type = if (state.petAvatar == null) {
                    IconPickerViewState.Type.DATA_LOADED
                } else {
                    IconPickerViewState.Type.PLAYER_CHANGED
                }

                state.copy(
                    type = type,
                    petAvatar = player.pet.avatar,
                    viewModels = createViewModels(state, player.inventory.iconPacks)
                )
            }
        }
    }

    private fun createViewModels(state: IconPickerViewState, iconPacks: Set<IconPack>): List<IconPickerDialogController.IconViewModel> {
        return Icon.values().map {
            val isSelected = if (state.selectedIcon == null) false else state.selectedIcon == it
            IconPickerDialogController.IconViewModel(it, isSelected, !iconPacks.contains(it.pack))
        }
    }
}

class IconPickerDialogController : MviDialogController<IconPickerViewState, IconPickerDialogController, IconPickerDialogPresenter, IconPickerIntent>
    , ViewStateRenderer<IconPickerViewState>, Injects<ControllerModule> {

    private var listener: (Icon?) -> Unit = {}
    private var selectedIcon: AndroidIcon? = null

    private val presenter by required { iconPickerPresenter }

    constructor(listener: (Icon?) -> Unit, selectedIcon: AndroidIcon? = null) : this() {
        this.listener = listener
        this.selectedIcon = selectedIcon
    }

    constructor(args: Bundle? = null) : super(args)

    override fun createPresenter() = presenter

    override fun onCreateDialog(savedViewState: Bundle?): DialogView {

        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_icon_picker, null)
        contentView.iconGrid.layoutManager = GridLayoutManager(activity!!, 4)

        val dialog = AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle("Choose icon")
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton("No icon", { _, _ ->
                listener(null)
            })
            .create()

        return DialogView(dialog, contentView)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        val icon = selectedIcon?.let {
            Icon.valueOf(it.name)
        }
        send(IconPickerIntent.LoadData(icon))
    }

    override fun render(state: IconPickerViewState, view: View) {

        when (state.type) {
            IconPickerViewState.Type.LOADING -> {
                val viewModels = Icon.values().map {
                    IconViewModel(it, it == selectedIcon ?: false, false)
                }
                view.iconGrid.adapter = IconAdapter(viewModels)
            }

            IconPickerViewState.Type.DATA_LOADED -> {
                changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
                (view.iconGrid.adapter as IconAdapter).updateAll(state.viewModels)
            }

            IconPickerViewState.Type.PLAYER_CHANGED -> {

            }
        }
    }

    data class IconViewModel(val icon: Icon, val isSelected: Boolean, val isLocked: Boolean)

    inner class IconAdapter(private var icons: List<IconViewModel>) : RecyclerView.Adapter<IconAdapter.ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = icons[holder.adapterPosition]
            val view = holder.itemView
            val iconView = view.icon

            val androidIcon = AndroidIcon.valueOf(vm.icon.name)

            iconView.setImageDrawable(
                IconicsDrawable(iconView.context)
                    .icon(androidIcon.icon)
                    .colorRes(if (vm.isSelected) R.color.md_white else androidIcon.color)
                    .paddingDp(8)
                    .sizeDp(48)
            )

            if (vm.isSelected) {
                iconView.setBackgroundResource(R.drawable.selected_icon_background)
                iconView.backgroundTintList = ColorStateList.valueOf(attr(R.attr.colorAccent))
                view.lockedIcon.visibility = View.GONE
            } else if (vm.isLocked) {
                view.lockedIcon.visibility = View.VISIBLE
                view.lockedIcon.imageTintList = ColorStateList.valueOf(attr(R.attr.colorAccent))
            } else {
                iconView.background = null
                view.lockedIcon.visibility = View.GONE
            }

            if (!vm.isLocked) {
                view.setOnClickListener {
                    listener(vm.icon)
                    dismissDialog()
                }
            }
        }

        override fun getItemCount() = icons.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_icon_picker, parent, false))

        fun updateAll(viewModels: List<IconViewModel>) {
            this.icons = viewModels
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    }
}