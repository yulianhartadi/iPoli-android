package mypoli.android.challenge

import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import kotlinx.android.synthetic.main.controller_challenge_list_for_category.view.*
import kotlinx.android.synthetic.main.item_buy_challenge.view.*
import kotlinx.android.synthetic.main.view_default_toolbar.view.*
import mypoli.android.MainActivity
import mypoli.android.R
import mypoli.android.challenge.data.Challenge
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.view.colorRes
import mypoli.android.common.view.setToolbar
import mypoli.android.common.view.showBackButton
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/30/17.
 */
class ChallengeListForCategoryViewController :
    MviViewController<ChallengeListForCategoryViewState, ChallengeListForCategoryViewController, ChallengeListForCategoryPresenter, ChallengeListForCategoryIntent> {

    private val presenter by required { challengeListForCategoryPresenter }

    private lateinit var challengeCategory: Challenge.Category

    constructor(challengeCategory: Challenge.Category) : this() {
        this.challengeCategory = challengeCategory
    }

    constructor(args: Bundle? = null) : super(args)

    override fun createPresenter() = presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_challenge_list_for_category, container, false)
        setToolbar(view.toolbar)
//        setToolbar(view.toolbar)
//        toolbarTitle = "Challenges"
        view.challengeList.layoutManager = LinearLayoutManager(container.context, LinearLayoutManager.VERTICAL, false)
        view.challengeList.adapter = ChallengeAdapter(
            listOf(
                ChallengeViewModel(
                    R.string.challenge_coding_ninja_name,
                    R.string.challenge_coding_ninja_description,
                    R.color.md_blue_600,
                    R.drawable.challenge_category_build_skill_image,
                    5,
                    false,
                    challenge = MainActivity.allChallenges(resources!!)[0]
                )
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
        super.onAttach(view)
        send(ChallengeListForCategoryIntent.LoadData(challengeCategory))
    }

    override fun render(state: ChallengeListForCategoryViewState, view: View) {

    }

    private fun showChallenge(challenge: Challenge) {
        val handler = FadeChangeHandler()
        router.pushController(
            RouterTransaction.with(PersonalizeChallengeViewController(challenge))
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )
    }

    data class ChallengeViewModel(
        @StringRes val name: Int,
        @StringRes val description: Int,
        @ColorRes val backgroundColor: Int,
        @DrawableRes val image: Int,
        val gemPrice: Int,
        val isBought: Boolean,
        val challenge: Challenge
    )

    inner class ChallengeAdapter(private var viewModels: List<ChallengeViewModel> = listOf()) :
        RecyclerView.Adapter<ChallengeAdapter.ViewHolder>() {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = viewModels[position]
            val itemView = holder.itemView
            itemView.setOnClickListener {
                showChallenge(vm.challenge)
            }
            itemView.challengeContainer.setCardBackgroundColor(colorRes(vm.backgroundColor))
            itemView.challengeName.setText(vm.name)
            itemView.challengeDescription.setText(vm.description)
            itemView.challengeImage.setImageResource(vm.image)
            itemView.challengePrice.text = "${vm.gemPrice}"
        }

        override fun getItemCount() = viewModels.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_buy_challenge, parent, false))

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        fun updateAll(viewModels: List<ChallengeViewModel>) {
            this.viewModels = viewModels
            notifyDataSetChanged()
        }

    }

}