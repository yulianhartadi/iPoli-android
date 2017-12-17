package io.ipoli.android.common.view

import android.animation.*
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.R
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.view.IconPickerViewState.Type.*
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.Player
import io.ipoli.android.player.usecase.BuyIconPackUseCase
import io.ipoli.android.player.usecase.ListenForPlayerChangesUseCase
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.IconPack
import kotlinx.android.synthetic.main.dialog_icon_picker.view.*
import kotlinx.android.synthetic.main.item_icon_picker.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.required
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */

sealed class IconPickerIntent : Intent {
    data class LoadData(val selectedIcon: Icon? = null) : IconPickerIntent()
    data class ChangePlayer(val player: Player) : IconPickerIntent()
    object UnlockItem : IconPickerIntent()
    object ShowIcons : IconPickerIntent()
    data class BuyIconPack(val iconPack: IconPack) : IconPickerIntent()
}

data class IconPickerViewState(
    val type: Type,
    val petAvatar: PetAvatar? = null,
    val selectedIcon: Icon? = null,
    val viewModels: List<IconPickerDialogController.IconViewModel> = listOf()
) : ViewState {
    enum class Type {
        LOADING,
        DATA_CHANGED,
        SHOW_UNLOCK,
        SHOW_ICONS,
        ICON_PACK_TOO_EXPENSIVE
    }
}

class IconPickerDialogPresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    private val buyIconPackUseCase: BuyIconPackUseCase,
    coroutineContext: CoroutineContext) :
    BaseMviPresenter<ViewStateRenderer<IconPickerViewState>, IconPickerViewState, IconPickerIntent>(
        IconPickerViewState(LOADING),
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

                state.copy(
                    type = DATA_CHANGED,
                    petAvatar = player.pet.avatar,
                    viewModels = createViewModels(state, player.inventory.iconPacks)
                )
            }

            is IconPickerIntent.UnlockItem -> {
                state.copy(
                    type = SHOW_UNLOCK
                )
            }

            is IconPickerIntent.ShowIcons -> {
                state.copy(
                    type = SHOW_ICONS
                )
            }

            is IconPickerIntent.BuyIconPack -> {
                val result = buyIconPackUseCase.execute(BuyIconPackUseCase.Params(intent.iconPack))
                val type = when (result) {
                    is BuyIconPackUseCase.Result.IconPackBought -> IconPickerViewState.Type.SHOW_ICONS
                    is BuyIconPackUseCase.Result.TooExpensive -> IconPickerViewState.Type.ICON_PACK_TOO_EXPENSIVE
                }
                state.copy(
                    type = type
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

class IconPickerDialogController :
    MviDialogController<IconPickerViewState, IconPickerDialogController, IconPickerDialogPresenter, IconPickerIntent> {

    private var listener: (Icon?) -> Unit = {}
    private var selectedIcon: AndroidIcon? = null

    private val presenter by required { iconPickerPresenter }

    constructor(listener: (Icon?) -> Unit, selectedIcon: AndroidIcon? = null) : this() {
        this.listener = listener
        this.selectedIcon = selectedIcon
    }

    constructor(args: Bundle? = null) : super(args)

    override fun createPresenter() = presenter

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.icon_picker_title)
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val contentView = inflater.inflate(R.layout.dialog_icon_picker, null)
        contentView.iconGrid.layoutManager = GridLayoutManager(activity!!, 4)
        return contentView
    }

    override fun onCreateDialog(dialogBuilder: AlertDialog.Builder, contentView: View, savedViewState: Bundle?): AlertDialog =
        dialogBuilder
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.no_icon, { _, _ ->
                listener(null)
            })
            .create()

    override fun onAttach(view: View) {
        super.onAttach(view)
        val icon = selectedIcon?.let {
            Icon.valueOf(it.name)
        }
        send(IconPickerIntent.LoadData(icon))
    }

    override fun render(state: IconPickerViewState, view: View) {

        when (state.type) {
            LOADING -> {
                val viewModels = Icon.values().map {
                    IconViewModel(it, it == selectedIcon ?: false, false)
                }
                view.iconGrid.adapter = IconAdapter(viewModels)
            }

            DATA_CHANGED -> {
                changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
                (view.iconGrid.adapter as IconAdapter).updateAll(state.viewModels)
            }

            SHOW_UNLOCK -> {

                val fadeOut = ObjectAnimator.ofFloat(dialog.window.decorView, "alpha", 1f, 0.0f)
                val fadeIn = ObjectAnimator.ofFloat(dialog.window.decorView, "alpha", 0.5f, 1f)
                fadeIn.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        renderBuyIconPack(view)
                    }
                })

                fadeIn.interpolator = AccelerateInterpolator()
                fadeOut.duration = shortAnimTime
                fadeIn.duration = mediumAnimTime

                val set = AnimatorSet()
                set.playSequentially(fadeOut, fadeIn)
                set.start()
            }

            SHOW_ICONS -> {
                TransitionManager.beginDelayedTransition(view.container as ViewGroup)
                view.iconGrid.visibility = View.VISIBLE
                view.unlockContainer.visibility = View.GONE
                changeTitle(R.string.icon_picker_title)
                changeNeutralButtonText(R.string.no_icon)
                setNeutralButtonListener(null)
            }

            ICON_PACK_TOO_EXPENSIVE -> {
                Toast.makeText(view.context, stringRes(R.string.icon_pack_not_enough_coins), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderBuyIconPack(view: View) {
        view.iconGrid.visibility = View.GONE
        view.unlockContainer.visibility = View.VISIBLE
        changeTitle(R.string.unlock_icon_pack_title)
        changeNeutralButtonText(R.string.back)
        setNeutralButtonListener {
            send(IconPickerIntent.ShowIcons)
        }
        view.buyIconPack.setOnClickListener {
            send(IconPickerIntent.BuyIconPack(IconPack.BASIC))
        }
        view.iconPackPrice.text = IconPack.BASIC.price.toString()
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
                iconView.setBackgroundResource(R.drawable.oval_background)
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
            } else {
                view.setOnClickListener {
                    send(IconPickerIntent.UnlockItem)
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