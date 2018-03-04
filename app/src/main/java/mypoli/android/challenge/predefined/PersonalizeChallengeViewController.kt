package mypoli.android.challenge.predefined

import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import kotlinx.android.synthetic.main.controller_personalize_challenge.view.*
import kotlinx.android.synthetic.main.item_challenge_quest.view.*
import mypoli.android.R
import mypoli.android.challenge.predefined.PersonalizeChallengeViewState.StateType.*
import mypoli.android.challenge.predefined.entity.AndroidPredefinedChallenge
import mypoli.android.challenge.predefined.entity.PredefinedChallenge
import mypoli.android.challenge.predefined.entity.PredefinedChallengeData
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.view.*
import space.traversal.kapsule.required


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class PersonalizeChallengeViewController :
    MviViewController<PersonalizeChallengeViewState, PersonalizeChallengeViewController, PersonalizeChallengePresenter, PersonalizeChallengeIntent> {

    private lateinit var challenge: PredefinedChallenge

    constructor(args: Bundle? = null) : super(args)

    constructor(challenge: PredefinedChallenge) : super() {
        this.challenge = challenge
    }

    private val presenter by required { personalizeChallengePresenter }

    override fun createPresenter() = presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_personalize_challenge, container, false)
        view.challengeQuestList.layoutManager =
            LinearLayoutManager(container.context, LinearLayoutManager.VERTICAL, false)
        val androidChallenge = AndroidPredefinedChallenge.valueOf(challenge.name)
        view.challengeBackgroundImage.setBackgroundResource(androidChallenge.backgroundImage)
        view.challengeImage.setBackgroundResource(androidChallenge.smallImage)
        view.collapsingToolbarContainer.title = stringRes(androidChallenge.title)

        view.challengeQuestList.adapter = ChallengeQuestAdapter()
        setToolbar(view.toolbar)
        view.acceptChallenge.sendOnClick(
            PersonalizeChallengeIntent.AcceptChallenge(
                challenge
            )
        )
        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            router.popCurrentController()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAttach(view: View) {
        showBackButton()
        colorStatusBar(android.R.color.transparent)
        super.onAttach(view)
        send(PersonalizeChallengeIntent.LoadData(challenge))
    }

    private fun colorStatusBar(@ColorRes color: Int) {
        val window = activity!!.window

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = colorRes(color)
    }

    override fun onDetach(view: View) {
        val window = activity!!.window
        window.statusBarColor = attrData(R.attr.colorPrimaryDark)
        super.onDetach(view)
    }

    override fun render(state: PersonalizeChallengeViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                (view.challengeQuestList.adapter as ChallengeQuestAdapter).updateAll(state.viewModels)
            }

            CHALLENGE_ACCEPTED -> {
                showShortToast(R.string.challenge_accepted)
                router.popCurrentController()
            }

            NO_QUESTS_SELECTED -> {
                showShortToast(R.string.no_quest_selected)
            }

            else -> {
            }
        }
    }

    data class ChallengeQuestViewModel(
        val name: String,
        val isSelected: Boolean,
        val quest: PredefinedChallengeData.Quest
    )

    inner class ChallengeQuestAdapter(private var viewModels: List<ChallengeQuestViewModel> = listOf()) :
        RecyclerView.Adapter<ChallengeQuestAdapter.ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = viewModels[position]
            val itemView = holder.itemView

            itemView.setOnClickListener {
                send(
                    PersonalizeChallengeIntent.ToggleSelected(
                        vm
                    )
                )
                itemView.challengeQuestCheckbox.isChecked =
                    !itemView.challengeQuestCheckbox.isChecked
            }

            itemView.challengeQuestCheckbox.isChecked = vm.isSelected
            itemView.challengeQuestCheckbox.setOnCheckedChangeListener { _, _ ->
                send(
                    PersonalizeChallengeIntent.ToggleSelected(
                        vm
                    )
                )
            }
            itemView.challengeQuestName.text = vm.name
        }

        override fun getItemCount() = viewModels.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_challenge_quest,
                    parent,
                    false
                )
            )

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        fun updateAll(viewModels: List<ChallengeQuestViewModel>) {
            this.viewModels = viewModels
            notifyDataSetChanged()
        }

    }

}