package io.ipoli.android.reward.list

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import io.ipoli.android.R
import io.ipoli.android.challenge.list.ui.AutoUpdatableAdapter
import io.ipoli.android.common.BaseController
import io.ipoli.android.common.daggerComponent
import io.ipoli.android.common.di.AppModule
import io.ipoli.android.common.ui.visible
import io.ipoli.android.reward.list.usecase.RemoveRewardFromListUseCase
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.controller_reward_list.view.*
import kotlinx.android.synthetic.main.item_reward.view.*
import kotlinx.android.synthetic.main.view_empty.view.*
import kotlinx.android.synthetic.main.view_error.view.*
import kotlinx.android.synthetic.main.view_loading.view.*
import kotlin.properties.Delegates


class RewardListController : BaseController<RewardListController, RewardListPresenter, RewardListComponent>() {

    lateinit private var rewardList: RecyclerView

    private lateinit var adapter: RewardListAdapter

    private val useRewardSubject = PublishSubject.create<RewardViewModel>()
    private val removeRewardSubject = PublishSubject.create<RemoveRewardFromListUseCase.RemoveParameters>()
    private val undoRemoveRewardSubject = PublishSubject.create<RemoveRewardFromListUseCase.UndoParameters>()

    override fun buildComponent(): RewardListComponent =
        DaggerRewardListComponent.builder()
            .controllerComponent(daggerComponent)
            .rewardListModule(RewardListModule(applicationContext!!))
            .build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_reward_list, container, false) as ViewGroup
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


//        val delegatesManager = AdapterDelegatesManager<List<RewardViewModel>>()
//            .addDelegate(RewardAdapterDelegate(LayoutInflater.from(activity), removeRewardSubject, useRewardSubject, {
//                val pushHandler = HorizontalChangeHandler()
//                val popHandler = HorizontalChangeHandler()
//                router.pushController(RouterTransaction.with(EditRewardController(rewardId = it.id))
//                    .pushChangeHandler(pushHandler)
//                    .popChangeHandler(popHandler))
//            }))

//        val delegatesManager = AdapterDelegatesManager<List<RewardViewModel>>()
//            .addDelegate(RewardAdapterDelegate(LayoutInflater.from(activity), removeRewardSubject))
//
//        adapter = RewardListAdapter(delegatesManager)

        adapter = RewardListAdapter(removeRewardSubject)

        rewardList.adapter = adapter

        return view;
    }


    fun loadRewardsIntent(): Observable<Boolean> =
        Observable.just(!restoringState).filter { _ -> true }.doOnComplete { Log.d("Chingy", "thingy") }

    fun useRewardIntent(): Observable<RewardViewModel> = useRewardSubject

    fun removeRewardIntent(): Observable<RemoveRewardFromListUseCase.RemoveParameters> =
        removeRewardSubject

    fun undoRemoveRewardIntent(): Observable<RemoveRewardFromListUseCase.UndoParameters> =
        undoRemoveRewardSubject

    fun render(state: RewardListViewState) {
        val contentView = view!!
        with(state) {
            contentView.emptyView.visible = state.isEmpty
            contentView.errorView.visible = state.hasError
            contentView.loadingView.visible = state.isLoading
            contentView.rewardList.visible = state.shouldShowData

            if (shouldShowData) {
                adapter.rewardList = state.rewards.toMutableList()
            }

            if (isRewardRemoved) {
                val snackbar = Snackbar.make(parentController?.view!!, "Reward removed", 2000)
                snackbar.setAction("UNDO", {
                    undoRemoveRewardSubject.onNext(RemoveRewardFromListUseCase.UndoParameters(
                        state.rewards,
                        state.removedReward!!,
                        state.removedRewardIndex!!
                    ))
                })
                snackbar.show()
//                Toast.makeText(applicationContext, "Reward Removed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class RewardListAdapter(val deleteSubject: PublishSubject<RemoveRewardFromListUseCase.RemoveParameters>) : RecyclerView.Adapter<RewardListAdapter.RewardViewHolder>(), AutoUpdatableAdapter {

        var rewardList: MutableList<RewardViewModel> by Delegates.observable(mutableListOf()) { _, old, new ->
            autoNotify(old, new) { o, n -> o.id == n.id }
        }

        override fun getItemCount(): Int = rewardList.size

        override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
            val post = rewardList[position]
            holder.bind(post)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder =
            RewardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_reward, parent, false))

        inner class RewardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            fun bind(rewardView: RewardViewModel) {
                with(rewardView) {
                    RxView.clicks(itemView.delete)
                        .map { RemoveRewardFromListUseCase.RemoveParameters(rewardList, rewardView) }
                        .subscribe(deleteSubject)
                    itemView.name.text = name
                    itemView.description.text = description
                }
            }
        }
    }
}