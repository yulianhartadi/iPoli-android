package io.ipoli.android.friends.feed.post

import android.animation.Animator
import android.animation.AnimatorInflater
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.view.*
import android.view.animation.OvershootInterpolator
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.RecyclerViewViewModel
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.friends.feed.*
import io.ipoli.android.friends.feed.data.AndroidReactionType
import io.ipoli.android.friends.feed.data.Post
import io.ipoli.android.friends.feed.post.PostViewState.StateType.DATA_CHANGED
import io.ipoli.android.friends.feed.post.PostViewState.StateType.LOADING
import io.ipoli.android.player.data.AndroidAvatar
import kotlinx.android.synthetic.main.controller_post.view.*
import kotlinx.android.synthetic.main.item_popup_reaction.view.*
import kotlinx.android.synthetic.main.item_post_comment.view.*
import kotlinx.android.synthetic.main.view_default_toolbar.view.*
import kotlinx.android.synthetic.main.view_loader.view.*

class PostViewController(args: Bundle? = null) :
    ReduxViewController<PostAction, PostViewState, PostReducer>(args) {

    override val reducer = PostReducer

    private var postId = ""
    private var canDelete = false

    private val commentWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            view!!.postCommentAdd.isEnabled = !s.isNullOrBlank()
        }

    }

    constructor(postId: String) : this() {
        this.postId = postId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = container.inflate(R.layout.controller_post)
        setToolbar(view.toolbar)

        view.post.layoutManager = LinearLayoutManager(view.context)
        ViewCompat.setNestedScrollingEnabled(view.post, false)
        val reactionPopupHandler = object : ReactionPopupHandler {
            override fun containerCoordinates(): IntArray {
                val parentLoc = IntArray(2)
                view.postContainer.getLocationInWindow(parentLoc)
                return parentLoc
            }

            override fun showReactionPopup(x: Int, y: Float) {
                val reactionPopup = view.reactionPopup
                reactionPopup.x = x.toFloat()
                reactionPopup.y = y

                playAnimation(
                    createPopupAnimator(reactionPopup),
                    onStart = { reactionPopup.visible() },
                    onEnd = {
                        view.reactionPopup.children.forEach { v ->
                            v.reactionAnimation.playAnimation()
                        }
                    })
            }

            override fun hideReactionPopup() {
                view.reactionPopup.children.forEach {
                    it.reactionAnimation.cancelAnimation()
                }
                view.let {
                    playAnimation(
                        createPopupAnimator(
                            it.reactionPopup, true
                        ),
                        onEnd = { it.reactionPopup.gone() }
                    )
                }
            }

            override fun isPopupShown() = view.reactionPopup.alpha > 0

        }
        view.post.adapter = PostAdapter(
            reactionPopupHandler = reactionPopupHandler,
            activity = activity!!
        )

        view.reactionPopup.layoutManager = GridLayoutManager(view.context, 3)
        view.reactionPopup.adapter = ReactionPopupAdapter(reactionPopupHandler)
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
                        dispatch(PostAction.React(postId, it))
                    }
                }
            }
        )

        view.postCommentList.layoutManager = LinearLayoutManager(view.context)
        ViewCompat.setNestedScrollingEnabled(view.postCommentList, false)
        view.postCommentList.adapter = CommentAdapter()

        view.postCommentAdd.isEnabled = false

        view.postCommentAdd.onDebounceClick {
            dispatch(PostAction.SaveComment(postId, view.postCommentText.text.toString()))
            view.postCommentText.setText("")
            ViewUtils.hideKeyboard(view.postCommentText)
        }

        return view
    }

    override fun onAttach(view: View) {
        showBackButton()
        super.onAttach(view)
        toolbarTitle = stringRes(R.string.post)
        view.postCommentText.addTextChangedListener(commentWatcher)
    }

    override fun onDetach(view: View) {
        view.postCommentText.removeTextChangedListener(commentWatcher)
        super.onDetach(view)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            return router.handleBack()
        }
        if (item.itemId == R.id.actionDelete) {
            navigate().toConfirmation(
                stringRes(R.string.dialog_remove_post_title),
                stringRes(R.string.dialog_remove_post_message)
            ) {
                dispatch(PostAction.Remove(postId))
                router.handleBack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.post_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.actionDelete).isVisible = canDelete
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateLoadAction() = PostAction.Load(postId)

    override fun render(state: PostViewState, view: View) {
        when (state.type) {
            LOADING -> {
                view.loader.visible()
                view.postContainerScroll.gone()
                view.addCommentGroup.gone()
            }

            DATA_CHANGED -> {
                view.loader.gone()
                view.postContainerScroll.visible()
                view.addCommentGroup.visible()

                canDelete = state.canDelete
                activity?.invalidateOptionsMenu()
                (view.post.adapter as PostAdapter).updateAll(listOf(state.postViewModel))
                if (state.comments.isEmpty()) {
                    view.emptyComments.visible()
                } else {
                    view.emptyComments.gone()
                }
                (view.postCommentList.adapter as CommentAdapter).updateAll(state.commentViewModels)
            }
        }
    }

    data class CommentViewModel(
        override val id: String,
        val playerAvatar: AndroidAvatar,
        val playerName: String,
        val playerUsername: String,
        val playerLevel: String,
        val postedTime: String,
        val text: String
    ) : RecyclerViewViewModel


    inner class CommentAdapter :
        BaseRecyclerViewAdapter<CommentViewModel>(R.layout.item_post_comment) {

        override fun onBindViewModel(vm: CommentViewModel, view: View, holder: SimpleViewHolder) {

            Glide.with(view.context).load(vm.playerAvatar.image)
                .apply(RequestOptions.circleCropTransform())
                .into(view.commentPlayerAvatar)

            val gradientDrawable = view.commentPlayerAvatar.background as GradientDrawable
            gradientDrawable.mutate()
            gradientDrawable.setColor(view.context.colorRes(vm.playerAvatar.backgroundColor))

            view.commentPlayerName.text = vm.playerName
            view.commentPlayerUsername.text = "@${vm.playerUsername}"
            view.commentPlayerLevel.text = vm.playerLevel
            view.commentPostedTime.text = vm.postedTime

            view.commentText.text = vm.text
        }
    }

    private val PostViewState.commentViewModels
        get() =
            comments.map {
                CommentViewModel(
                    id = it.id,
                    playerAvatar = AndroidAvatar.valueOf(it.playerAvatar.name),
                    playerName = it.playerDisplayName,
                    playerUsername = it.playerUsername,
                    playerLevel = "Level ${it.playerLevel}",
                    text = it.text,
                    postedTime = DateUtils.getRelativeTimeSpanString(
                        it.createdAt.toEpochMilli(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL
                    ).toString()
                )
            }

    private val PostViewState.postViewModel: PostViewModel
        get() = toPostViewModel(
            context = activity!!,
            post = post!!,
            showShortPlayerMessage = false,
            reactListener = { _, _ ->
            },
            reactListListener = {
                navigate().toReactionHistory(it)
            },
            shareListener = { m ->
                navigate().toSharePost(m)
            },
            avatarClickListener = { postPlayerId ->
                if(!post.isFromCurrentPlayer) {
                    navigateFromRoot().toProfile(postPlayerId)
                }
            }
        )

    private fun createPopupAnimator(view: View, reverse: Boolean = false): Animator {
        val anim = if (reverse) R.animator.popout else R.animator.popup
        val animator = AnimatorInflater.loadAnimator(view.context, anim)
        animator.setTarget(view)
        animator.interpolator = OvershootInterpolator()
        animator.duration = shortAnimTime
        return animator
    }
}