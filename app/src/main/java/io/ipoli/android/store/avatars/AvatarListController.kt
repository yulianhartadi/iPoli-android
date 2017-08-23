package io.ipoli.android.store.avatars

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.jakewharton.rxbinding2.view.RxView
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.daggerComponent
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.controller_avatar_list.view.*
import kotlinx.android.synthetic.main.item_avatar_store.view.*
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import io.reactivex.Observable
import android.view.animation.AnimationUtils
import android.widget.Toast
import io.ipoli.android.reward.RewardModel
import io.ipoli.android.store.avatars.data.Avatar
import io.reactivex.functions.Consumer
import timber.log.Timber

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/20/17.
 */
class AvatarListController : BaseController<AvatarListController, AvatarListPresenter>() {
    private var restoringState: Boolean = false

    lateinit private var avatarList: RecyclerView

    private var buySubject = PublishSubject.create<AvatarViewModel>()
    private var useSubject = PublishSubject.create<AvatarViewModel>()

    private lateinit var adapter: AvatarListAdapter

    val avatarListComponent: AvatarListComponent by lazy {
        val component = DaggerAvatarListComponent.builder()
            .controllerComponent(daggerComponent)
            .build()
        component.inject(this@AvatarListController)
        component
    }

    override fun createPresenter(): AvatarListPresenter = avatarListComponent.createAvatarListPresenter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_avatar_list, container, false)
        avatarList = view.avatarList
        avatarList.setHasFixedSize(true)
        avatarList.layoutManager = GridLayoutManager(view.context, 2)

        val delegatesManager = AdapterDelegatesManager<List<AvatarViewModel>>()
            .addDelegate(AvatarListController.AvatarAdapterDelegate(LayoutInflater.from(activity), buySubject, useSubject))
        adapter = AvatarListAdapter(delegatesManager)
        avatarList.adapter = adapter

        return view
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        avatarListComponent // will ensure that dagger component will be initialized lazily.
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {
        this.restoringState = restoringViewState
    }

    fun displayAvatarListIntent(): Observable<Boolean> =
        Observable.just(!restoringState).filter { _ -> true }

    fun buyAvatarIntent(): Observable<AvatarViewModel> {
        return buySubject
    }

    fun useAvatarIntent(): Observable<AvatarViewModel> {
        return useSubject
    }

    fun render(state: AvatarListViewState) {
        Log.d("AAA", state.toString())
        when (state) {
            is AvatarListViewState.Loading -> {
                Toast.makeText(activity, "Loading", Toast.LENGTH_SHORT).show()
            }

            is AvatarListViewState.Error -> {
                Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
            }

            is AvatarListViewState.DataLoaded -> {
                adapter.items = state.avatars
                adapter.notifyDataSetChanged()
            }

            is AvatarListViewState.AvatarBought -> {
                val name = activity?.getString(state.avatarViewModel.name)
                Toast.makeText(activity, name + " successfully bought", Toast.LENGTH_SHORT).show();
            }

            is AvatarListViewState.AvatarUsed -> {
                val name = activity?.getString(state.avatarViewModel.name)
                Toast.makeText(activity, name + " successfully used", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class AvatarListAdapter(manager: AdapterDelegatesManager<List<AvatarViewModel>>) : ListDelegationAdapter<List<AvatarViewModel>>(
        manager)

    class AvatarAdapterDelegate(private val inflater: LayoutInflater,
                                private val buySubject: PublishSubject<AvatarViewModel>,
                                private val useSubject: PublishSubject<AvatarViewModel>) : AdapterDelegate<List<AvatarViewModel>>() {

        private var lastAnimatedPosition = -1

        private val colors = intArrayOf(R.color.md_green_300,
            R.color.md_indigo_300,
            R.color.md_blue_300,
            R.color.md_red_300,
            R.color.md_deep_orange_300,
            R.color.md_purple_300,
            R.color.md_orange_300,
            R.color.md_pink_300)

        override fun isForViewType(items: List<AvatarViewModel>, position: Int): Boolean = true

        override fun onBindViewHolder(items: List<AvatarViewModel>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
            val vh = holder as AvatarViewHolder
            val avatar = items[position]
            vh.bindAvatar(avatar, colors.get(position % colors.size))
            playEnterAnimation(holder.itemView, holder.getAdapterPosition());
        }

        override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder =
            AvatarViewHolder(inflater.inflate(R.layout.item_avatar_store, parent, false))

        private fun playEnterAnimation(viewToAnimate: View, position: Int) {
            if (position > lastAnimatedPosition) {
                val anim = AnimationUtils.loadAnimation(viewToAnimate.context, R.anim.fade_in)
                anim.startOffset = (position * 50).toLong()
                viewToAnimate.startAnimation(anim)
                lastAnimatedPosition = position
            }
        }

        inner class AvatarViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            fun bindAvatar(vm: AvatarViewModel, @ColorRes backgroundColor: Int) {
                with(vm) {
                    val context = itemView.context
                    val observable = RxView.clicks(itemView.avatarPrice).map { vm }
                    if (isBought) {
                        itemView.avatarPrice.setText(context.getString(R.string.avatar_store_use_avatar).toUpperCase())
                        itemView.avatarPrice.setIconResource(null as Drawable?)
                        observable.subscribe(useSubject)
                    } else {
                        itemView.avatarPrice.setText(price.toString())
                        itemView.avatarPrice.setIconResource(context.getDrawable(R.drawable.ic_life_coin_white_24dp))
                        observable.subscribe(buySubject)
                    }
                    itemView.avatarName.text = context.getString(name)
                    itemView.avatarPicture.setImageResource(picture)
                    itemView.setBackgroundColor(ContextCompat.getColor(context, backgroundColor))
                }
            }
        }

    }
}