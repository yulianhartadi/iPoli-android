package io.ipoli.android.common.view

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.R
import kotlinx.android.synthetic.main.dialog_color_picker.view.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */

class IconPickerDialogController : BaseDialogController {

    private var listener: (AndroidIcon?) -> Unit = {}
    private var selectedIcon: AndroidIcon? = null

    constructor(listener: (AndroidIcon?) -> Unit, selectedIcon: AndroidIcon? = null) : this() {
        this.listener = listener
        this.selectedIcon = selectedIcon
    }

    constructor(args: Bundle? = null) : super(args)

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_icon_picker, null)
        val colorGrid = contentView.colorGrid
        colorGrid.layoutManager = GridLayoutManager(activity!!, 4)

        val iconViewModels = AndroidIcon.values().map {
            IconViewModel(it, it == selectedIcon ?: false)
        }


        colorGrid.adapter = IconAdapter(iconViewModels)

        return AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle("Choose icon")
            .setIcon(R.drawable.pet_5_head)
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton("No icon", { _, _ ->
                listener(null)
            })
            .create()
    }

    data class IconViewModel(val icon: AndroidIcon, val isSelected: Boolean)

    inner class IconAdapter(private val icons: List<IconViewModel>) : RecyclerView.Adapter<IconAdapter.ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = icons[holder.adapterPosition]
            val iv = holder.itemView as ImageView

            iv.setImageDrawable(
                IconicsDrawable(iv.context)
                    .icon(vm.icon.icon)
                    .colorRes(vm.icon.color)
                    .sizeDp(32)
            )

            if (vm.isSelected) {
                iv.setBackgroundResource(R.drawable.selected_icon_background)
            } else {
                iv.background = null
            }

            iv.setOnClickListener {
                listener(vm.icon)
                dismissDialog()
            }
        }

        override fun getItemCount() = icons.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_icon_picker, parent, false))

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    }
}