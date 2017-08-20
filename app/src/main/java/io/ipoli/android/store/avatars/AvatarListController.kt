package io.ipoli.android.store.avatars

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.jakewharton.rxbinding2.view.RxView
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.daggerComponent
import io.ipoli.android.reward.RewardModel
import io.ipoli.android.store.DaggerStoreComponent
import io.ipoli.android.store.StoreComponent
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_avatar_store.view.*
import kotlinx.android.synthetic.main.item_reward.view.*
import android.graphics.drawable.Drawable
import android.support.v7.widget.LinearLayoutManager
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import io.ipoli.android.R.id.price
import io.ipoli.android.reward.EditRewardController
import io.ipoli.android.reward.RewardListController
import kotlinx.android.synthetic.main.controller_avatar_list.view.*
import kotlinx.android.synthetic.main.controller_rewards.view.*


/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/20/17.
 */
class AvatarListController : BaseController<AvatarListController, AvatarListPresenter>() {

    lateinit private var avatarList: RecyclerView

    private val buySubject = PublishSubject.create<AvatarModel>()
    private val useSubject = PublishSubject.create<AvatarModel>()

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
        avatarList.layoutManager = LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false)

        val delegatesManager = AdapterDelegatesManager<List<AvatarModel>>()
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
    }

    class AvatarListAdapter(manager: AdapterDelegatesManager<List<AvatarModel>>) : ListDelegationAdapter<List<AvatarModel>>(
        manager) {

//        init {
//            setHasStableIds(true)
//        }

//        override fun getItemId(position: Int): Long = items[position].id

    }

    class AvatarAdapterDelegate(private val inflater: LayoutInflater,
                                private val buySubject: PublishSubject<AvatarModel>,
                                private val useSubject: PublishSubject<AvatarModel>) : AdapterDelegate<List<AvatarModel>>() {
        override fun isForViewType(items: List<AvatarModel>, position: Int): Boolean = true

        override fun onBindViewHolder(items: List<AvatarModel>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
            val vh = holder as AvatarViewHolder
            val avatar = items[position]
            vh.bindAvatar(avatar)
        }

        override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder =
            AvatarViewHolder(inflater.inflate(R.layout.item_avatar_store, parent, false))


        inner class AvatarViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            fun bindAvatar(avatar: AvatarModel) {
                with(avatar) {
                    val observable = RxView.clicks(itemView.avatarPrice).takeUntil(RxView.detaches(itemView)).map { avatar }
                    val resources = itemView.resources
                    if (isBought) {
                        itemView.avatarPrice.setText(resources.getString(R.string.avatar_store_use_avatar).toUpperCase())
                        itemView.avatarPrice.setIconResource(null as Drawable?)
                        observable.subscribe(useSubject)
                    } else {
                        itemView.avatarPrice.setText(price.toString())
                        itemView.avatarPrice.setIconResource(resources.getDrawable(R.drawable.ic_life_coin_white_24dp))
                        observable.subscribe(buySubject)
                    }
                    itemView.name.text = name
                    itemView.avatarPicture.setImageResource(picture)
                }
            }
        }

    }

}

