package io.ipoli.android.friends.feed

import android.animation.Animator
import android.animation.AnimatorInflater
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.view.animation.OvershootInterpolator
import com.google.firebase.auth.FirebaseAuth
import io.ipoli.android.R
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.friends.feed.FeedViewState.StateType.*
import io.ipoli.android.friends.feed.data.AndroidReactionType
import io.ipoli.android.friends.feed.data.Post
import kotlinx.android.synthetic.main.controller_feed.view.*
import kotlinx.android.synthetic.main.item_popup_reaction.view.*
import kotlinx.android.synthetic.main.view_empty_list.view.*
import kotlinx.android.synthetic.main.view_loader.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 7/10/18.
 */
class FeedViewController(args: Bundle? = null) :
    ReduxViewController<FeedAction, FeedViewState, FeedReducer>(args), ReactionPopupHandler {

    override val reducer = FeedReducer

    private var isGuest = true

    init {
        helpConfig = HelpConfig(
            R.string.help_dialog_heroes_feed_title,
            R.string.help_dialog_heroes_feed_message
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_feed, container, false)

        view.shareItem.onDebounceClick {
            navigateFromRoot().toPostItemPicker()
        }

        view.reactionPopup.layoutManager = GridLayoutManager(view.context, 3)
        view.reactionPopup.adapter = ReactionPopupAdapter(this)
        (view.reactionPopup.adapter as ReactionPopupAdapter).updateAll(
            AndroidReactionType.values().map { it ->
                val title = stringRes(it.title)
                ReactionPopupViewModel(
                    title,
                    it.animation,
                    title,
                    Post.ReactionType.valueOf(it.name)
                ) {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        showShortToast(R.string.error_need_registration)
                    } else {
                        dispatch(FeedAction.React(it))
                    }
                }
            }
        )

        view.feedList.setOnTouchListener { _, _ ->
            if (isPopupShown()) {
                hideReactionPopup()
                true
            } else {
                false
            }
        }

        view.feedList.layoutManager = LinearLayoutManager(view.context)
        view.feedList.adapter = PostAdapter(this, activity!!)

        view.emptyAnimation.setAnimation("empty_posts.json")

        return view
    }

    override fun isPopupShown() = view?.let { it.reactionPopup.alpha > 0 } ?: false

    override fun onCreateLoadAction() = FeedAction.Load

    override fun onAttach(view: View) {
        super.onAttach(view)
        toolbarTitle = stringRes(R.string.drawer_feed)
    }

    override fun onDetach(view: View) {
        dispatch(FeedAction.Unload)
        super.onDetach(view)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.feed_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.actionAddFriend).isVisible = !isGuest
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                return router.handleBack()

            R.id.actionAddFriend -> {
                dispatch(FeedAction.InviteFriend)
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }


    override fun handleBack() =
        if (view != null && view!!.reactionPopup.visible) {
            hideReactionPopup()
            true
        } else false

    override fun render(state: FeedViewState, view: View) {

        when (state.type) {

            LOADING -> {
                view.loader.visible()
                view.emptyAnimation.pauseAnimation()
                view.emptyContainer.gone()
                view.feedList.gone()
                view.reactionPopup.gone()
            }

            POSTS_CHANGED -> {

                state.isPlayerSignedIn?.let {
                    isGuest = !it
                    activity?.invalidateOptionsMenu()
                }

                val posts = state.posts!!

                if (posts.isNotEmpty()) {
                    renderPosts(view, state)
                } else {
                    renderEmptyPosts(view)
                }

                if (isGuest) {
                    view.shareItem.gone()
                } else {
                    view.shareItem.visible()
                }
            }

            NO_INTERNET_CONNECTION -> {
                view.loader.gone()
                view.emptyContainer.visible()
                view.feedList.gone()
                view.shareItem.gone()
                view.reactionPopup.gone()

                view.emptyAnimation.pauseAnimation()
                view.emptyAnimation.gone()
                view.emptyTitle.text = stringRes(R.string.error_no_internet_title)
                view.emptyText.text = stringRes(R.string.feed_no_internet_text)
            }

            NO_INVITES_LEFT ->
                showLongToast(R.string.no_invites_left)

            SHOW_INVITE_FRIEND ->
                navigate().toInviteFriends()

            else -> {

            }
        }
    }

    private fun renderEmptyPosts(view: View) {
        view.loader.gone()
        view.emptyContainer.visible()
        view.feedList.gone()
        view.reactionPopup.gone()
        view.shareItem.visible()

        view.emptyAnimation.playAnimation()
        view.emptyAnimation.visible()
        view.emptyTitle.text = stringRes(R.string.feed_empty_title)
        view.emptyText.text = stringRes(R.string.feed_empty_text)
    }

    private fun renderPosts(
        view: View,
        state: FeedViewState
    ) {
        view.loader.gone()
        view.emptyContainer.gone()
        view.emptyAnimation.pauseAnimation()
        view.feedList.visible()
        view.shareItem.visible()

        val postVMs = state.posts!!.map { p ->
            toPostViewModel(context = activity!!, post = p,
                reactListener = { postId, _ ->
                    dispatch(FeedAction.ShowReactionPopupForPost(postId))
                },
                reactListListener = {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        showShortToast(R.string.error_need_registration)
                    } else {
                        navigate().toReactionHistory(it)
                    }
                },
                commentsListener = {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        showShortToast(R.string.error_need_registration)
                    } else {
                        navigateFromRoot().toPost(it)
                    }
                },
                shareListener = { m ->
                    navigate().toSharePost(m)
                },
                postClickListener = { postId ->
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        showShortToast(R.string.error_need_registration)
                    } else {
                        navigateFromRoot().toPost(postId)
                    }
                },
                avatarClickListener = { postPlayerId ->
                    navigateFromRoot().toProfile(postPlayerId)
                }
            )
        }

        (view.feedList.adapter as PostAdapter).updateAll(postVMs)
    }

    override fun containerCoordinates() =
        view?.let {
            val parentLoc = IntArray(2)
            view!!.container.getLocationInWindow(parentLoc)
            parentLoc
        } ?: intArrayOf(0, 0)

    override fun hideReactionPopup() {
        view?.reactionPopup?.children?.forEach {
            it.reactionAnimation.cancelAnimation()
        }
        view?.let {
            playAnimation(
                createPopupAnimator(
                    it.reactionPopup, true
                ),
                onEnd = { it.reactionPopup.gone() }
            )
        }
    }

    override fun showReactionPopup(x: Int, y: Float) {
        view?.let {
            val reactionPopup = it.reactionPopup
            reactionPopup.x = x.toFloat()
            reactionPopup.y = y

            playAnimation(
                createPopupAnimator(reactionPopup),
                onStart = { reactionPopup.visible() },
                onEnd = {
                    it.reactionPopup.children.forEach { v ->
                        v.reactionAnimation.playAnimation()
                    }
                })
        }
    }

    private fun createPopupAnimator(view: View, reverse: Boolean = false): Animator {
        val anim = if (reverse) R.animator.popout else R.animator.popup
        val animator = AnimatorInflater.loadAnimator(view.context, anim)
        animator.setTarget(view)
        animator.interpolator = OvershootInterpolator()
        animator.duration = shortAnimTime
        return animator
    }
}