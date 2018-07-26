package io.ipoli.android.player.profile

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.ipoli.android.R
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.redux.android.BaseViewController
import io.ipoli.android.common.view.colorRes
import io.ipoli.android.common.view.gone
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.RecyclerViewViewModel
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.common.view.stringRes
import io.ipoli.android.common.view.visible
import io.ipoli.android.player.data.AndroidAvatar
import io.ipoli.android.player.profile.ProfileViewState.StateType.*
import kotlinx.android.synthetic.main.controller_profile_friend_list.view.*
import kotlinx.android.synthetic.main.item_friend_list.view.*
import kotlinx.android.synthetic.main.view_empty_list.view.*
import kotlinx.android.synthetic.main.view_loader.view.*
import kotlinx.android.synthetic.main.view_require_login.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.Period

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 7/21/18.
 */
class ProfileFriendListViewController(args: Bundle? = null) :
    BaseViewController<ProfileAction, ProfileViewState>(args) {

    override var stateKey = ""

    constructor(reducerKey: String) : this() {
        this.stateKey = reducerKey
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_profile_friend_list, container, false)
        view.friendList.layoutManager = LinearLayoutManager(view.context)
        view.friendList.adapter = FriendAdapter()

        view.emptyAnimation.setAnimation("empty_friends.json")

        return view
    }

    override fun onCreateLoadAction() =
        ProfileAction.LoadFriends

    override fun colorLayoutBars() {

    }

    override fun render(state: ProfileViewState, view: View) {
        when (state.type) {
            SHOW_REQUIRE_LOGIN -> {
                view.loader.gone()
                view.emptyAnimation.pauseAnimation()
                view.emptyContainer.gone()
                view.friendList.gone()
                view.loginMessageContainer.visible()
                view.loginMessage.setText(R.string.friends_sign_in)
                view.loginButton.onDebounceClick {
                    navigateFromRoot().toAuth(null)
                }
            }

            NO_INTERNET_CONNECTION -> {
                view.loader.gone()
                view.emptyContainer.visible()
                view.emptyAnimation.pauseAnimation()
                view.emptyAnimation.gone()
                view.friendList.gone()
                view.loginMessageContainer.gone()

                view.emptyTitle.text = stringRes(R.string.error_no_internet_title)
                view.emptyText.text = stringRes(R.string.friends_no_internet_text)
            }

            FRIENDS_LIST_CHANGED -> {
                view.loader.gone()
                view.emptyAnimation.pauseAnimation()
                if (state.friends!!.isEmpty()) {
                    view.emptyContainer.visible()
                    view.emptyAnimation.visible()
                    view.emptyAnimation.playAnimation()
                    view.emptyTitle.setText(R.string.empty_friend_list_title)
                    view.emptyText.setText(R.string.empty_friend_list_text)
                    view.friendList.gone()
                } else {
                    view.emptyContainer.gone()
                    view.friendList.visible()
                    (view.friendList.adapter as FriendAdapter).updateAll(state.viewModels)
                }
            }

            else -> {
            }
        }
    }

    data class FriendViewModel(
        override val id: String,
        val avatar: AndroidAvatar,
        val name: String,
        val username: String,
        val joinedTime: String,
        val level: String
    ) : RecyclerViewViewModel

    inner class FriendAdapter :
        BaseRecyclerViewAdapter<FriendViewModel>(R.layout.item_friend_list) {

        override fun onBindViewModel(vm: FriendViewModel, view: View, holder: SimpleViewHolder) {
            Glide.with(view.context).load(vm.avatar.image)
                .apply(RequestOptions.circleCropTransform())
                .into(view.playerAvatar)

            val gradientDrawable = view.playerAvatar.background as GradientDrawable
            gradientDrawable.mutate()
            gradientDrawable.setColor(colorRes(vm.avatar.backgroundColor))

            view.playerName.text = vm.name
            view.playerUsername.text = vm.username
            view.playerLevel.text = vm.level
            view.joinedTime.text = vm.joinedTime

            view.onDebounceClick {
                navigateFromRoot().toProfile(vm.id)
            }
        }
    }

    private val ProfileViewState.viewModels: List<FriendViewModel>
        get() = friends!!.map {
            FriendViewModel(
                id = it.id,
                avatar = AndroidAvatar.valueOf(it.avatar.name),
                name = it.displayName,
                username = "@${it.username}",
                joinedTime = createdAgo(it.createdAt.toEpochMilli()),
                level = "Lvl ${it.level}"
            )
        }

    private fun createdAgo(createdAt: Long): String {
        val p = Period.between(createdAt.startOfDayUTC, LocalDate.now())
        return when {
            p.isZero || p.isNegative -> stringRes(R.string.today).toLowerCase()
            p.years > 0 -> "${p.years} years ago"
            p.months > 0 -> "${p.months} months ago"
            else -> "${p.days} days ago"
        }
    }

}