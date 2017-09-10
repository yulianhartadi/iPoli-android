package io.ipoli.android.quest.calendar.ui.dayview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import kotlinx.android.synthetic.main.unscheduled_quest_item.view.*

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/17.
 */
class UnscheduledQuestsAdapter(private val context: Context, private val items: List<UnscheduledQuestViewModel>) : RecyclerView.Adapter<UnscheduledQuestsAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.unscheduled_quest_item, parent, false))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(viewModel: UnscheduledQuestViewModel) {
            itemView.name.text = viewModel.name
        }
    }
}