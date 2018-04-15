package io.ipoli.android.quest.subquest.view

import android.content.res.ColorStateList
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.MotionEvent
import android.view.View
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.view.children
import io.ipoli.android.common.view.gone
import io.ipoli.android.common.view.normalIcon
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.ReorderItemHelper
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.common.view.visible
import kotlinx.android.synthetic.main.item_edit_repeating_quest_sub_quest.view.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/11/2018.
 */
class ReadOnlySubQuestAdapter(
        private val recyclerView: RecyclerView,
        useLightTheme: Boolean = false
) :
        BaseRecyclerViewAdapter<ReadOnlySubQuestAdapter.ReadOnlySubQuestViewModel>(
                R.layout.item_edit_repeating_quest_sub_quest
        ) {

    data class ReadOnlySubQuestViewModel(val id: String, val name: String)

    data class ItemColorConfig(
            @ColorRes val indicatorColor: Int,
            @ColorRes val nameColor: Int,
            @ColorRes val nameHintColor: Int,
            @ColorRes val buttonColor: Int
    )

    private val touchHelper: ItemTouchHelper
    private val itemColorConfig: ItemColorConfig

    init {
        val dragHelper =
                ReorderItemHelper(
                        onItemMoved = { oldPosition, newPosition ->
                            move(oldPosition, newPosition)
                        }
                )

        touchHelper = ItemTouchHelper(dragHelper)
        touchHelper.attachToRecyclerView(recyclerView)


        itemColorConfig =
                if (useLightTheme)
                    ItemColorConfig(
                            indicatorColor = R.color.md_light_text_50,
                            nameColor = R.color.md_light_text_100,
                            nameHintColor = R.color.md_light_text_70,
                            buttonColor = R.color.md_light_text_70
                    )
                else
                    ItemColorConfig(
                            indicatorColor = R.color.md_dark_text_54,
                            nameColor = R.color.md_dark_text_87,
                            nameHintColor = R.color.md_dark_text_54,
                            buttonColor = R.color.md_dark_text_54
                    )
    }

    override fun onBindViewModel(
            vm: ReadOnlySubQuestViewModel,
            view: View,
            holder: SimpleViewHolder
    ) {

        view.subQuestIndicator.backgroundTintList =
                ColorStateList.valueOf(
                        ContextCompat.getColor(
                                view.context,
                                itemColorConfig.indicatorColor
                        )
                )
        view.editSubQuestName.setText(vm.name)
        view.editSubQuestName.tag = vm.id

        view.reorderButton.setImageDrawable(
                IconicsDrawable(view.context).normalIcon(
                        GoogleMaterial.Icon.gmd_reorder,
                        itemColorConfig.buttonColor
                )
                        .respectFontBounds(true)
        )

        view.reorderButton.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                touchHelper.startDrag(holder)
            }
            false
        }

        view.removeButton.setImageDrawable(
                IconicsDrawable(view.context).normalIcon(
                        GoogleMaterial.Icon.gmd_close,
                        itemColorConfig.buttonColor
                )
                        .respectFontBounds(true)
        )

        view.removeButton.setOnClickListener {
            removeAt(holder.adapterPosition)
        }

        view.editSubQuestName.setTextColor(
                ContextCompat.getColor(
                        view.context,
                        itemColorConfig.nameColor
                )
        )
        view.editSubQuestName.setHintTextColor(
                ContextCompat.getColor(
                        view.context,
                        itemColorConfig.nameHintColor
                )
        )

        view.editSubQuestName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                startEdit(view)
            }
        }
    }

    private fun startEdit(view: View) {
        exitEditMode()
        view.reorderButton.gone()
        view.removeButton.visible()
        view.editSubQuestName.requestFocus()
        ViewUtils.showKeyboard(view.context, view.editSubQuestName)
        view.editSubQuestName.setSelection(view.editSubQuestName.length())
    }

    private fun exitEditMode() {
        recyclerView.children.forEach {
            it.removeButton.gone()
            it.reorderButton.visible()
        }
    }
}