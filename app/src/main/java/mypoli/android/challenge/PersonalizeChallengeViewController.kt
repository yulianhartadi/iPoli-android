package mypoli.android.challenge

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.controller_personalize_challenge.view.*
import kotlinx.android.synthetic.main.item_challenge_quest.view.*
import mypoli.android.R
import mypoli.android.common.mvi.MviViewController
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class PersonalizeChallengeViewController(args: Bundle? = null) :
    MviViewController<PersonalizeChallengeViewState, PersonalizeChallengeViewController, PersonalizeChallengePresenter, PersonalizeChallengeIntent>(args) {

    private val presenter by required { personalizeChallengePresenter }

    override fun createPresenter() = presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_personalize_challenge, container, false)
        view.challengeQuestList.layoutManager = LinearLayoutManager(container.context, LinearLayoutManager.VERTICAL, false)
        view.challengeQuestList.adapter = ChallengeQuestAdapter(
            listOf(
                ChallengeQuestViewModel("Hello New World", true),
                ChallengeQuestViewModel("Rune like the wind Rune like the wind Rune like the wind Rune like the wind", false)
            )
        )
        return view
    }

    override fun render(state: PersonalizeChallengeViewState, view: View) {

    }

    data class ChallengeQuestViewModel(val name: String, val isSelected: Boolean)

    inner class ChallengeQuestAdapter(private var viewModels: List<ChallengeQuestViewModel> = listOf()) :
        RecyclerView.Adapter<ChallengeQuestAdapter.ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = viewModels[position]
            val itemView = holder.itemView

            itemView.challengeQuestCheckbox.isChecked = vm.isSelected
            itemView.challengeQuestName.text = vm.name
        }

        override fun getItemCount() = viewModels.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_challenge_quest, parent, false))

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        fun updateAll(viewModels: List<ChallengeQuestViewModel>) {
            this.viewModels = viewModels
            notifyDataSetChanged()
        }

    }

}