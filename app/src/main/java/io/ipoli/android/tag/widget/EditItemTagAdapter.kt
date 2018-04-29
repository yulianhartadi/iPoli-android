package io.ipoli.android.tag.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import io.ipoli.android.R
import io.ipoli.android.common.view.AndroidIcon
import io.ipoli.android.common.view.normalIcon
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.tag.Tag
import kotlinx.android.synthetic.main.item_edit_quest_tag.view.*


class EditItemAutocompleteTagAdapter(tags: List<Tag>, context: Context) :
    ArrayAdapter<Tag>(
        context,
        R.layout.item_tag_autocomplete,
        tags
    ) {
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return bindView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return bindView(position, convertView, parent)
    }

    private fun bindView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            inflater.inflate(R.layout.item_tag_autocomplete, parent, false) as TextView
        } else {
            convertView as TextView
        }

        val item = getItem(position)
        view.text = item.name

        val icon = item.icon?.let { AndroidIcon.valueOf(it.name).icon } ?: MaterialDesignIconic.Icon.gmi_label
        val color = item.icon?.let { AndroidIcon.valueOf(it.name).color } ?: R.color.md_green_500

        view.setCompoundDrawablesWithIntrinsicBounds(
            IconicsDrawable(view.context)
                .normalIcon(
                    icon,
                    color
                ).respectFontBounds(true), null, null, null
        )

        return view
    }
}

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