package mypoli.android.challenge

import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import kotlinx.android.synthetic.main.controller_personalize_challenge.view.*
import kotlinx.android.synthetic.main.item_challenge_quest.view.*
import mypoli.android.R
import mypoli.android.challenge.data.Challenge
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.view.attr
import mypoli.android.common.view.colorRes
import mypoli.android.common.view.setToolbar
import space.traversal.kapsule.required


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class PersonalizeChallengeViewController :
    MviViewController<PersonalizeChallengeViewState, PersonalizeChallengeViewController, PersonalizeChallengePresenter, PersonalizeChallengeIntent> {

    private lateinit var challenge: Challenge

    constructor(args: Bundle? = null) : super(args)

    constructor(challenge: Challenge) : super() {
        this.challenge = challenge
    }

    private val presenter by required { personalizeChallengePresenter }

    override fun createPresenter() = presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_personalize_challenge, container, false)
        view.challengeQuestList.layoutManager = LinearLayoutManager(container.context, LinearLayoutManager.VERTICAL, false)

        val vms = challenge.quests.map {
            when (it) {

                is Challenge.Quest.OneTime -> {
                    ChallengeQuestViewModel(it.text, it.selected)
                }

                is Challenge.Quest.Repeating -> {
                    ChallengeQuestViewModel(it.text, it.selected)
                }
            }
        }

        view.challengeQuestList.adapter = ChallengeQuestAdapter(vms)
        setToolbar(view.toolbar)
        return view
    }

    override fun onAttach(view: View) {
        colorStatusBar(android.R.color.transparent)
        super.onAttach(view)
    }

    private fun colorStatusBar(@ColorRes color: Int) {
        val window = activity!!.window

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = colorRes(color)
    }

    override fun onDetach(view: View) {
        val window = activity!!.window

// clear FLAG_TRANSLUCENT_STATUS flag:
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//
//// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

// finally change the color
        window.statusBarColor = attr(R.attr.colorPrimaryDark)
        super.onDetach(view)
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