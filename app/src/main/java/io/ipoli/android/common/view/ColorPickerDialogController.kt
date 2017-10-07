package io.ipoli.android.common.view

import android.app.Dialog
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
import kotlinx.android.synthetic.main.dialog_color_picker.view.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */

class ColorPickerDialogController : BaseDialogController {
    interface ColorPickedListener {
        fun onColorPicked(color: Color)
    }

    private var listener: ColorPickedListener? = null
    private var selectedColor: Color? = null

    constructor(listener: ColorPickedListener, selectedColor: Color? = null) : super() {
        this.listener = listener
        this.selectedColor = selectedColor
    }

    protected constructor() : super()

    protected constructor(args: Bundle?) : super(args)

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_color_picker, null)
        val colorGrid = contentView.colorGrid
        colorGrid.layoutManager = GridLayoutManager(activity!!, 4)

        val colorViewModels = Color.values().map {
            ColorViewModel(it, it == selectedColor ?: false)
        }

        colorGrid.adapter = ColorAdapter(colorViewModels)

        return AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle("Pick color")
            .setIcon(R.drawable.pet_5_head)
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    data class ColorViewModel(val color: Color, val isSelected: Boolean)

    inner class ColorAdapter(private val colors: List<ColorViewModel>) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = colors[position]
            val iv = holder.itemView as ImageView
            val drawable = iv.background as GradientDrawable
            drawable.setColor(ContextCompat.getColor(iv.context, vm.color.color200))

            if (vm.isSelected) {
                iv.setImageResource(R.drawable.ic_done_white_24dp)
            }

            iv.setOnClickListener {
                listener?.onColorPicked(vm.color)
                dismissDialog()
            }
        }

        override fun getItemCount() = colors.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_color_picker, parent, false))
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    }
}