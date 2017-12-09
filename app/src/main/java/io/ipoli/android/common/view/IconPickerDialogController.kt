package io.ipoli.android.common.view

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

class IconPickerDialogController : MviDialogController<PetDialogViewState, IconPickerDialogController, PetDialogPresenter, LoadPetIntent>
    , ViewStateRenderer<PetDialogViewState>, Injects<ControllerModule> {

    private var listener: (AndroidIcon?) -> Unit = {}
    private var selectedIcon: AndroidIcon? = null

    private val presenter by required { petDialogPresenter }

    constructor(listener: (AndroidIcon?) -> Unit, selectedIcon: AndroidIcon? = null) : this() {
        this.listener = listener
        this.selectedIcon = selectedIcon
    }

    constructor(args: Bundle? = null) : super(args)

    override fun createPresenter() = presenter

    override fun onCreateDialog(savedViewState: Bundle?): DialogView {

        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_icon_picker, null)
        val colorGrid = contentView.colorGrid
        colorGrid.layoutManager = GridLayoutManager(activity!!, 4)

        val iconViewModels = AndroidIcon.values().map {
            IconViewModel(it, it == selectedIcon ?: false)
        }


        colorGrid.adapter = IconAdapter(iconViewModels)

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
        send(LoadPetIntent)
    }

    override fun render(state: PetDialogViewState, view: View) {
        if (state.type == PetDialogViewState.Type.PET_LOADED) {
            changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
        }
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
                    .paddingDp(4)
                    .sizeDp(40)
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