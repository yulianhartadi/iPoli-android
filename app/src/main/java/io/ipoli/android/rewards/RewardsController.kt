package io.ipoli.android.rewards

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RouterTransaction

import com.hannesdorfmann.mosby3.RestoreViewOnCreateMviController
import io.ipoli.android.R
import kotlinx.android.synthetic.main.item_reward.view.*

class RewardsController : RestoreViewOnCreateMviController<RewardsController, RewardsPresenter>() {

    private var restoringState: Boolean = false

    lateinit private var rewardList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_rewards, container, false) as ViewGroup
        rewardList = view.findViewById<RecyclerView>(R.id.reward_list)
        rewardList.setHasFixedSize(true)
        rewardList.layoutManager = LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false)

        val rewardRepository = RewardRepository()

//        rewardRepository.save(Reward(name = "Hello", description = "It is a great reward!"))

        rewardList.adapter = RewardListAdapter(rewardRepository.loadRewards(), { reward ->
            router.pushController(RouterTransaction.with(EditRewardController(rewardId = reward.id)))
        })

        return view;
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {
        this.restoringState = restoringViewState
    }

    override fun createPresenter(): RewardsPresenter {
        return RewardsPresenter()
    }

    class RewardListAdapter(val rewards: List<Reward>, val itemClick: (Reward) -> Unit) :
            RecyclerView.Adapter<RewardListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reward, parent, false)
            return ViewHolder(view, itemClick)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindReward(rewards[position])
        }

        override fun getItemCount() = rewards.size

        class ViewHolder(view: View, val itemClick: (Reward) -> Unit) : RecyclerView.ViewHolder(view) {

            fun bindReward(reward: Reward) {
                with(reward) {
                    itemView.setOnClickListener { itemClick.invoke(reward) }
                    itemView.name.setText(reward.name)
                    itemView.description.setText(reward.description)
                }
            }
        }
    }

}