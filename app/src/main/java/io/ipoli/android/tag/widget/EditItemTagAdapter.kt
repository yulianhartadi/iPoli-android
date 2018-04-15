package io.ipoli.android.tag.widget

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import io.ipoli.android.R
import io.ipoli.android.common.view.normalIcon
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.tag.Tag
import kotlinx.android.synthetic.main.item_edit_quest_tag.view.*

class EditItemAutocompleteTagAdapter(tagNames: List<String>, context: Context) :
    ArrayAdapter<String>(
        context,
        android.R.layout.simple_dropdown_item_1line,
        tagNames
    )

class EditItemTagAdapter(private val removeTagCallback: (Tag) -> Unit = {}) :
    BaseRecyclerViewAdapter<EditItemTagAdapter.TagViewModel>(R.layout.item_edit_quest_tag) {

    data class TagViewModel(
        val name: String,
        val icon: IIcon,
        val tag: Tag
    )

    override fun onBindViewModel(vm: TagViewModel, view: View, holder: SimpleViewHolder) {
        view.tagName.text = vm.name
        view.tagName.setCompoundDrawablesWithIntrinsicBounds(
            IconicsDrawable(view.context)
                .normalIcon(
                    vm.icon,
                    R.color.md_white
                ).respectFontBounds(true), null, null, null
        )
        view.tagRemove.setOnClickListener {
            removeTagCallback(vm.tag)
        }
    }
}