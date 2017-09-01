package io.ipoli.android.challenge.list.ui

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import io.ipoli.android.R
import io.ipoli.android.challenge.list.ChallengeListPresenter
import io.ipoli.android.challenge.list.di.ChallengeListComponent
import io.ipoli.android.challenge.list.di.DaggerChallengeListComponent
import io.ipoli.android.challenge.list.usecase.ChallengeListViewState
import io.ipoli.android.common.BaseController
import io.ipoli.android.common.text.DateFormatter
import io.ipoli.android.common.daggerComponent
import io.reactivex.Observable
import kotlinx.android.synthetic.main.controller_challenge_list.view.*
import kotlinx.android.synthetic.main.item_challenge.view.*
import kotlinx.android.synthetic.main.view_empty.view.*
import kotlinx.android.synthetic.main.view_error.view.*
import kotlinx.android.synthetic.main.view_loading.view.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/23/17.
 */
class ChallengeListController : BaseController<ChallengeListController, ChallengeListPresenter, ChallengeListComponent>() {

    lateinit private var challengeList: RecyclerView

    private lateinit var adapter: ChallengeAdapter

    override fun buildComponent(): ChallengeListComponent =
        DaggerChallengeListComponent.builder()
            .controllerComponent(daggerComponent)
            .build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_challenge_list, container, false)
        challengeList = view.challengeList
        challengeList.setHasFixedSize(true)
        challengeList.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)

        val delegatesManager = AdapterDelegatesManager<List<ChallengeViewModel>>()
            .addDelegate(ChallengeViewModelAdapterDelegate(inflater))

        adapter = ChallengeAdapter(delegatesManager)

        challengeList.adapter = adapter
        return view
    }

    fun loadChallengesIntent(): Observable<Boolean> =
        Observable.just(creatingState).filter { _ -> true }

    fun render(state: ChallengeListViewState) {
        val contentView = view!!
        when (state) {
            is ChallengeListViewState.DataLoaded -> {
                contentView.challengeList.visibility = View.VISIBLE
                contentView.loadingView.visibility = View.GONE
                contentView.emptyView.visibility = View.GONE
                contentView.errorView.visibility = View.GONE
                adapter.items = state.data
                adapter.notifyDataSetChanged()
            }
        }
    }
}

class ChallengeAdapter(manager: AdapterDelegatesManager<List<ChallengeViewModel>>) :
    ListDelegationAdapter<List<ChallengeViewModel>>(manager)

class ChallengeViewModelAdapterDelegate(private val inflater: LayoutInflater) : AdapterDelegate<List<ChallengeViewModel>>() {
    override fun isForViewType(items: List<ChallengeViewModel>, position: Int): Boolean = true

    override fun onBindViewHolder(items: List<ChallengeViewModel>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as ViewHolder
        val viewModel = items[position]
        vh.bind(viewModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder =
        ViewHolder(inflater.inflate(R.layout.item_challenge, parent, false))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(viewModel: ChallengeViewModel) {
            itemView.name.text = viewModel.name
            itemView.categoryImage.setImageResource(viewModel.categoryImage)
            itemView.dueDate.text = DateFormatter.format(itemView.context, viewModel.dueDate)

            itemView.moreMenu.setOnClickListener { v ->
                val popupMenu = PopupMenu(itemView.context, v)
                popupMenu.inflate(R.menu.challenge_actions_menu)
                popupMenu.show()
            }
        }
    }
}