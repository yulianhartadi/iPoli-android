package io.ipoli.android.common.view

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import io.ipoli.android.R
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.view.ColorDialogViewState.Type.*
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.Player
import io.ipoli.android.player.usecase.ListenForPlayerChangesUseCase
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.ColorPack
import kotlinx.android.synthetic.main.dialog_color_picker.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Injects
import space.traversal.kapsule.required
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */

sealed class ColorDialogIntent : Intent

data class LoadDataIntent(val selectedColor: Color? = null) : ColorDialogIntent()
data class ChangePlayerIntent(val player: Player) : ColorDialogIntent()

data class ColorDialogViewState(
    val type: Type,
    val petAvatar: PetAvatar? = null,
    val selectedColor: Color? = null,
    val viewModels: List<ColorPickerDialogController.ColorViewModel> = listOf()
) : ViewState {
    enum class Type {
        LOADING,
        DATA_LOADED,
        PLAYER_CHANGED
    }
}


class ColorDialogPresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    coroutineContext: CoroutineContext) :
    BaseMviPresenter<ViewStateRenderer<ColorDialogViewState>, ColorDialogViewState, ColorDialogIntent>(
        ColorDialogViewState(ColorDialogViewState.Type.LOADING),
        coroutineContext
    ) {
    override fun reduceState(intent: ColorDialogIntent, state: ColorDialogViewState) =
        when (intent) {
            is LoadDataIntent -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(ChangePlayerIntent(it))
                    }
                }
                state.copy(
                    selectedColor = intent.selectedColor
                )
            }

            is ChangePlayerIntent -> {
                val player = intent.player
                val type = if (state.petAvatar == null) {
                    DATA_LOADED
                } else {
                    PLAYER_CHANGED
                }
                state.copy(
                    type = type,
                    petAvatar = player.pet.avatar,
                    viewModels = createViewModels(state, player.inventory.colorPacks)
                )
            }
        }

    private fun createViewModels(state: ColorDialogViewState, colorPacks: Set<ColorPack>): List<ColorPickerDialogController.ColorViewModel> {
        val selectedColor = state.selectedColor!!
        return Color.values().map {
            ColorPickerDialogController.ColorViewModel(it, it == selectedColor, !colorPacks.contains(it.pack))
        }
    }
}

class ColorPickerDialogController : MviDialogController<ColorDialogViewState, ColorPickerDialogController, ColorDialogPresenter, ColorDialogIntent>
    , ViewStateRenderer<ColorDialogViewState>, Injects<ControllerModule> {

    interface ColorPickedListener {
        fun onColorPicked(color: AndroidColor)
    }

    private var listener: ColorPickedListener? = null
    private var selectedColor: AndroidColor? = null

    private val presenter by required { colorDialogPresenter }

    constructor(listener: ColorPickedListener, selectedColor: AndroidColor? = null) : this() {
        this.listener = listener
        this.selectedColor = selectedColor
    }

    constructor(args: Bundle? = null) : super(args)

    override fun createPresenter() = presenter

    override fun onCreateDialog(savedViewState: Bundle?): DialogView {

        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_color_picker, null)

        val dialog = AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle("Choose color")
            .setNegativeButton(R.string.cancel, null)
            .create()

        return DialogView(dialog, contentView)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        val color = selectedColor?.let {
            Color.valueOf(it.name)
        }
        send(LoadDataIntent(color))
    }

    override fun render(state: ColorDialogViewState, view: View) {
        when (state.type) {
            LOADING -> {
                val colorGrid = view.colorGrid
                colorGrid.layoutManager = GridLayoutManager(activity!!, 4)
                val colorViewModels = AndroidColor.values().map {
                    ColorViewModel(Color.valueOf(it.name), it == selectedColor ?: false)
                }
                colorGrid.adapter = ColorAdapter(colorViewModels)
            }

            DATA_LOADED -> {
                changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
                (view.colorGrid.adapter as ColorAdapter).updateAll(state.viewModels)
            }

            PLAYER_CHANGED -> {

            }
        }
    }

    data class ColorViewModel(val color: Color, val isSelected: Boolean, val isLocked: Boolean = false)

    inner class ColorAdapter(private var colors: List<ColorViewModel>) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = colors[position]
            val iv = holder.itemView as ImageView

            val androidColor = AndroidColor.valueOf(vm.color.name)

            val drawable = iv.background as GradientDrawable
            drawable.setColor(ContextCompat.getColor(iv.context, androidColor.color500))

            when {
                vm.isSelected -> iv.setImageResource(R.drawable.ic_done_white_24dp)
                vm.isLocked -> iv.setImageResource(R.drawable.ic_lock_white_24dp)
                else -> iv.setImageDrawable(null)
            }
            if (!vm.isLocked) {
                iv.setOnClickListener {
                    listener?.onColorPicked(androidColor)
                    dismissDialog()
                }
            }
        }

        override fun getItemCount() = colors.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_color_picker, parent, false))

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        fun updateAll(viewModels: List<ColorViewModel>) {
            this.colors = viewModels
            notifyDataSetChanged()
        }

    }
}