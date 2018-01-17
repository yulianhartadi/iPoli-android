package mypoli.android.common.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_color_picker.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.R
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.common.view.ColorPickerViewState.Type.*
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.pet.PetAvatar
import mypoli.android.player.Player
import mypoli.android.player.usecase.BuyColorPackUseCase
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import mypoli.android.quest.Color
import mypoli.android.quest.ColorPack
import space.traversal.kapsule.required
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/2/17.
 */

sealed class ColorPickerIntent : Intent {
    data class LoadData(val selectedColor: Color? = null) : ColorPickerIntent()
    data class ChangePlayer(val player: Player) : ColorPickerIntent()
    object UnlockColorPack : ColorPickerIntent()
    object ShowColors : ColorPickerIntent()
    data class BuyColorPack(val colorPack: ColorPack) : ColorPickerIntent()
}


data class ColorPickerViewState(
    val type: Type,
    val petAvatar: PetAvatar? = null,
    val selectedColor: Color? = null,
    val viewModels: List<ColorPickerDialogController.ColorViewModel> = listOf()
) : ViewState {
    enum class Type {
        LOADING,
        DATA_CHANGED,
        SHOW_UNLOCK,
        SHOW_COLORS,
        COLOR_PACK_TOO_EXPENSIVE
    }
}

class ColorPickerPresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    private val buyColorPackUseCase: BuyColorPackUseCase,
    coroutineContext: CoroutineContext
) :
    BaseMviPresenter<ViewStateRenderer<ColorPickerViewState>, ColorPickerViewState, ColorPickerIntent>(
        ColorPickerViewState(ColorPickerViewState.Type.LOADING),
        coroutineContext
    ) {
    override fun reduceState(intent: ColorPickerIntent, state: ColorPickerViewState) =
        when (intent) {
            is ColorPickerIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(ColorPickerIntent.ChangePlayer(it))
                    }
                }
                state.copy(
                    selectedColor = intent.selectedColor
                )
            }

            is ColorPickerIntent.ChangePlayer -> {
                val player = intent.player
                state.copy(
                    type = DATA_CHANGED,
                    petAvatar = player.pet.avatar,
                    viewModels = createViewModels(state, player.inventory.colorPacks)
                )
            }

            is ColorPickerIntent.UnlockColorPack -> {
                state.copy(
                    type = SHOW_UNLOCK
                )
            }

            is ColorPickerIntent.ShowColors -> {
                state.copy(
                    type = SHOW_COLORS
                )
            }

            is ColorPickerIntent.BuyColorPack -> {
                val result =
                    buyColorPackUseCase.execute(BuyColorPackUseCase.Params(intent.colorPack))
                val type = when (result) {
                    is BuyColorPackUseCase.Result.ColorPackBought -> ColorPickerViewState.Type.SHOW_COLORS
                    is BuyColorPackUseCase.Result.TooExpensive -> ColorPickerViewState.Type.COLOR_PACK_TOO_EXPENSIVE
                }
                state.copy(
                    type = type
                )
            }
        }

    private fun createViewModels(
        state: ColorPickerViewState,
        colorPacks: Set<ColorPack>
    ): List<ColorPickerDialogController.ColorViewModel> {
        return Color.values().map {
            val isSelected = if (state.selectedColor == null) false else state.selectedColor == it
            ColorPickerDialogController.ColorViewModel(
                it,
                isSelected,
                !colorPacks.contains(it.pack)
            )
        }
    }
}

class ColorPickerDialogController :
    MviDialogController<ColorPickerViewState, ColorPickerDialogController, ColorPickerPresenter, ColorPickerIntent> {

    interface ColorPickedListener {
        fun onColorPicked(color: AndroidColor)
    }

    private var listener: ColorPickedListener? = null
    private var selectedColor: AndroidColor? = null

    private val presenter by required { colorPickerPresenter }

    constructor(listener: ColorPickedListener, selectedColor: AndroidColor? = null) : this() {
        this.listener = listener
        this.selectedColor = selectedColor
    }

    constructor(args: Bundle? = null) : super(args)

    override fun createPresenter() = presenter

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.color_picker_title)
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val contentView = inflater.inflate(R.layout.dialog_color_picker, null)
        contentView.colorGrid.layoutManager = GridLayoutManager(activity!!, 4)
        return contentView
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.back, null)
            .create()

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).visibility = View.GONE
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        val color = selectedColor?.let {
            Color.valueOf(it.name)
        }
        send(ColorPickerIntent.LoadData(color))
    }

    override fun render(state: ColorPickerViewState, view: View) {
        when (state.type) {
            LOADING -> {
                val colorGrid = view.colorGrid
                colorGrid.layoutManager = GridLayoutManager(activity!!, 4)
                val colorViewModels = Color.values().map {
                    ColorViewModel(it, it == selectedColor ?: false, false)
                }
                colorGrid.adapter = ColorAdapter(colorViewModels)
            }

            DATA_CHANGED -> {
                changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
                (view.colorGrid.adapter as ColorAdapter).updateAll(state.viewModels)
            }

            SHOW_UNLOCK -> {
                TransitionManager.beginDelayedTransition(dialog.window.decorView as ViewGroup)
                view.colorGrid.visibility = View.GONE
                view.unlockContainer.visibility = View.VISIBLE
                changeTitle(R.string.unlock_color_pack_title)
                changeNeutralButtonText(R.string.back)

                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).visibility = View.VISIBLE

                setNeutralButtonListener {
                    send(ColorPickerIntent.ShowColors)
                }
                view.buyColorPack.setOnClickListener {
                    send(ColorPickerIntent.BuyColorPack(ColorPack.BASIC))
                }
                view.colorPackPrice.text = ColorPack.BASIC.gemPrice.toString()
            }

            SHOW_COLORS -> {
                val fadeOut = ObjectAnimator.ofFloat(dialog.window.decorView, "alpha", 1f, 0.0f)
                val fadeIn = ObjectAnimator.ofFloat(dialog.window.decorView, "alpha", 0.5f, 1f)
                fadeIn.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        renderColorGrid(view)
                    }
                })

                fadeIn.interpolator = AccelerateInterpolator()
                fadeOut.duration = shortAnimTime
                fadeIn.duration = mediumAnimTime

                val set = AnimatorSet()
                set.playSequentially(fadeOut, fadeIn)
                set.start()
            }

            COLOR_PACK_TOO_EXPENSIVE -> {
                CurrencyConverterDialogController().showDialog(router, "currency-converter")
                Toast.makeText(
                    view.context,
                    stringRes(R.string.color_pack_not_enough_coins),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun renderColorGrid(view: View) {
        view.unlockContainer.visibility = View.GONE
        view.colorGrid.visibility = View.VISIBLE
        changeTitle(R.string.color_picker_title)
        setNeutralButtonListener(null)
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).visibility = View.GONE
    }

    data class ColorViewModel(val color: Color, val isSelected: Boolean, val isLocked: Boolean)

    inner class ColorAdapter(private var colors: List<ColorViewModel>) :
        RecyclerView.Adapter<ColorAdapter.ViewHolder>() {
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
            } else {
                iv.sendOnClick(ColorPickerIntent.UnlockColorPack)
            }
        }

        override fun getItemCount() = colors.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_color_picker,
                    parent,
                    false
                )
            )

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        fun updateAll(viewModels: List<ColorViewModel>) {
            this.colors = viewModels
            notifyDataSetChanged()
        }

    }
}