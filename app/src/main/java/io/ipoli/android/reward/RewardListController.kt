package io.ipoli.android.reward

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.jakewharton.rxbinding2.view.RxView
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.daggerComponent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.controller_rewards.view.*
import kotlinx.android.synthetic.main.item_reward.view.*


class RewardListController : BaseController<RewardListController, RewardListPresenter>() {

    private var restoringState: Boolean = false

    lateinit private var rewardList: RecyclerView

    private lateinit var adapter: RewardsAdapter

    private val useRewardSubject = PublishSubject.create<RewardModel>()
    private val deleteRewardSubject = PublishSubject.create<RewardModel>()

    val rewardListComponent: RewardListComponent by lazy {
        val component = DaggerRewardListComponent
            .builder()
            .controllerComponent(daggerComponent)
            .build()
        component.inject(this@RewardListController)
        component
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        rewardListComponent // will ensure that dagger component will be initialized lazily.
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_rewards, container, false) as ViewGroup
        rewardList = view.rewardList
        rewardList.setHasFixedSize(true)
        rewardList.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)

//        val rewardRepository = RewardRepository()

//        rewardRepository.save(Reward(name = "Hello", description = "It is a great reward!"))

//        rewardList.adapter = RewardListAdapter(rewardRepository.loadRewards(), { reward ->
//
//            val pushHandler = HorizontalChangeHandler()
//            val popHandler = HorizontalChangeHandler()
//            router.pushController(RouterTransaction.with(EditRewardController(rewardId = reward.id))
//                    .pushChangeHandler(pushHandler)
//                    .popChangeHandler(popHandler))
//        })


        val delegatesManager = AdapterDelegatesManager<List<RewardModel>>()
            .addDelegate(RewardAdapterDelegate(LayoutInflater.from(activity), useRewardSubject, deleteRewardSubject, {
                val pushHandler = HorizontalChangeHandler()
                val popHandler = HorizontalChangeHandler()
                router.pushController(RouterTransaction.with(EditRewardController(rewardId = it.id))
                    .pushChangeHandler(pushHandler)
                    .popChangeHandler(popHandler))
            }))

        adapter = RewardsAdapter(delegatesManager)

        rewardList.adapter = adapter

        return view;
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {
        this.restoringState = restoringViewState
    }

    override fun createPresenter(): RewardListPresenter {
        return rewardListComponent.createRewardListPresenter()
    }

    fun loadRewardsIntent(): Observable<Boolean> {
        return Observable.just(!restoringState).filter { _ -> true }.doOnComplete { Log.d("Chingy", "thingy") }
    }

    fun useRewardIntent(): Observable<RewardModel> {
        return useRewardSubject
    }

    fun deleteRewardIntent(): Observable<RewardModel> {
        return deleteRewardSubject;
    }

    fun render(state: RewardViewState) {
        when (state) {
            is RewardsInitialLoadingState -> {
                Toast.makeText(activity, "Loading", Toast.LENGTH_LONG).show()
            }
            is RewardsLoadedState -> {
                adapter.items = state.rewards
                adapter.notifyDataSetChanged()
//                rewardList.adapter = RewardListAdapter(state.rewards!!, { reward ->
//
//                    val pushHandler = HorizontalChangeHandler()
//                    val popHandler = HorizontalChangeHandler()
//                    router.pushController(RouterTransaction.with(EditRewardController(rewardId = reward.id))
//                            .pushChangeHandler(pushHandler)
//                            .popChangeHandler(popHandler))
//                })
            }
        }
    }

    class RewardsAdapter(manager: AdapterDelegatesManager<List<RewardModel>>) : ListDelegationAdapter<List<RewardModel>>(
        manager) {

//        init {
//            setHasStableIds(true)
//        }

//        override fun getItemId(position: Int): Long = items[position].id

    }

    class RewardAdapterDelegate(private val inflater: LayoutInflater,
                                private val clickSubject: PublishSubject<RewardModel>,
                                private val deleteSubject: PublishSubject<RewardModel>,
                                private val clickListener: (RewardModel) -> Unit) : AdapterDelegate<List<RewardModel>>() {

        override fun onBindViewHolder(items: List<RewardModel>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
            val vh = holder as RewardViewHolder
            val reward = items[position]
            vh.bindReward(reward)
        }

        override fun isForViewType(items: List<RewardModel>, position: Int): Boolean = true

        override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder =
            RewardViewHolder(inflater.inflate(R.layout.item_reward, parent, false))


        inner class RewardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            fun bindReward(reward: RewardModel) {
                with(reward) {
                    RxView.clicks(itemView.buyReward).takeUntil(RxView.detaches(itemView)).map { reward }.subscribe(clickSubject)
                    RxView.clicks(itemView.delete).takeUntil(RxView.detaches(itemView)).map { reward }.subscribe(deleteSubject)
                    itemView.setOnClickListener { clickListener(reward) }
                    itemView.name.setText(name)
                    itemView.description.setText(description)
                }
            }
        }

    }

}