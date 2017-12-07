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
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.LoadPetIntent
import io.ipoli.android.pet.PetDialogPresenter
import io.ipoli.android.pet.PetDialogViewState
import kotlinx.android.synthetic.main.dialog_color_picker.view.*
import space.traversal.kapsule.Injects
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */

class ColorPickerDialogController : MviDialogController<PetDialogViewState, ColorPickerDialogController, PetDialogPresenter, LoadPetIntent>
    , ViewStateRenderer<PetDialogViewState>, Injects<ControllerModule> {

    interface ColorPickedListener {
        fun onColorPicked(color: AndroidColor)
    }

    private var listener: ColorPickedListener? = null
    private var selectedColor: AndroidColor? = null

    private val presenter by required { petDialogPresenter }

    constructor(listener: ColorPickedListener, selectedColor: AndroidColor? = null) : this() {
        this.listener = listener
        this.selectedColor = selectedColor
    }

    constructor(args: Bundle? = null) : super(args)

    override fun createPresenter() = presenter

    override fun onCreateDialog(savedViewState: Bundle?): DialogView {

        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_color_picker, null)
        val colorGrid = contentView.colorGrid
        colorGrid.layoutManager = GridLayoutManager(activity!!, 4)

        val colorViewModels = AndroidColor.values().map {
            ColorViewModel(it, it == selectedColor ?: false)
        }

        colorGrid.adapter = ColorAdapter(colorViewModels)

        val dialog = AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle("Choose color")
            .setNegativeButton(R.string.cancel, null)
            .create()

        return DialogView(dialog, contentView)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(LoadPetIntent)
    }

    override fun render(state: PetDialogViewState, view: View) {
        if (state.type == PetDialogViewState.Type.PET_LOADED) {
            changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
        }
    }

    data class ColorViewModel(val color: AndroidColor, val isSelected: Boolean)

    inner class ColorAdapter(private val colors: List<ColorViewModel>) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = colors[position]
            val iv = holder.itemView as ImageView
            val drawable = iv.background as GradientDrawable
            drawable.setColor(ContextCompat.getColor(iv.context, vm.color.color500))

            if (vm.isSelected) {
                iv.setImageResource(R.drawable.ic_done_white_24dp)
            }

            iv.setOnClickListener {
                listener?.onColorPicked(vm.color)
                dismissDialog()
            }
        }

        override fun getItemCount() = colors.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_color_picker, parent, false))

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    }
}