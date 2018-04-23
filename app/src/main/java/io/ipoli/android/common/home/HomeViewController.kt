package io.ipoli.android.common.home

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import io.ipoli.android.Constants
import io.ipoli.android.MainActivity
import io.ipoli.android.R
import io.ipoli.android.challenge.list.ChallengeListViewController
import io.ipoli.android.common.InviteFriendsDialogController
import io.ipoli.android.common.home.HomeViewState.StateType.*
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.AndroidPetMood
import io.ipoli.android.pet.PetViewController
import io.ipoli.android.player.auth.AuthViewController
import io.ipoli.android.player.data.AndroidAvatar
import io.ipoli.android.quest.bucketlist.BucketListViewController
import io.ipoli.android.quest.schedule.ScheduleViewController
import io.ipoli.android.repeatingquest.list.RepeatingQuestListViewController
import io.ipoli.android.settings.SettingsViewController
import io.ipoli.android.store.StoreViewController
import io.ipoli.android.store.avatar.AvatarStoreViewController
import io.ipoli.android.tag.Tag
import io.ipoli.android.tag.list.TagListViewController
import io.ipoli.android.tag.show.TagViewController
import kotlinx.android.synthetic.main.controller_home.view.*
import kotlinx.android.synthetic.main.drawer_header_home.view.*
import kotlinx.android.synthetic.main.menu_item_tag_view.view.*
import space.traversal.kapsule.required


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/19/17.
 */
class HomeViewController(args: Bundle? = null) :
    ReduxViewController<HomeAction, HomeViewState, HomeReducer>(args),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private var navigationItemSelected: MenuItem? = null

    override val reducer = HomeReducer

    private val fadeChangeHandler = FadeChangeHandler()

    private val eventLogger by required { eventLogger }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        val contentView = inflater.inflate(R.layout.controller_home, container, false)

        contentView.navigationView.setNavigationItemSelectedListener(this)

        actionBarDrawerToggle = object :
            ActionBarDrawerToggle(
                activity,
                contentView.drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
            ) {

            override fun onDrawerOpened(drawerView: View) {
                navigationItemSelected = null
            }

            override fun onDrawerClosed(drawerView: View) {
                navigationItemSelected?.let {
                    onItemSelectedFromDrawer(it)
                }

            }
        }

        contentView.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        return contentView
    }

    override fun onCreateLoadAction() = HomeAction.Load

    private fun onItemSelectedFromDrawer(item: MenuItem) {

        when (item.itemId) {
            R.id.calendar ->
                changeChildController(ScheduleViewController())

            R.id.bucketList ->
                changeChildController(BucketListViewController())

            R.id.repeatingQuests ->
                changeChildController(RepeatingQuestListViewController())

            R.id.challenges ->
                changeChildController(ChallengeListViewController())

            R.id.tags -> {
                changeChildController(TagListViewController())
            }

            R.id.store ->
                pushWithRootRouter(
                    RouterTransaction.with(StoreViewController())
                        .pushChangeHandler(fadeChangeHandler)
                        .popChangeHandler(fadeChangeHandler)
                )

            R.id.community ->
                openCommunity()

            R.id.inviteFriends ->
                showInviteFriends()

            R.id.settings ->
                pushWithRootRouter(
                    RouterTransaction.with(SettingsViewController())
                        .pushChangeHandler(fadeChangeHandler)
                        .popChangeHandler(fadeChangeHandler)
                )

            R.id.feedback
            ->
                showFeedback()

            else -> {
                if (TAG_IDS.contains(item.itemId)) {
                    dispatch(HomeAction.SelectTag(TAG_IDS.indexOf(item.itemId)))
                }
            }
        }

        view!!.navigationView.setCheckedItem(item.itemId)
    }

    private fun showInviteFriends() {
        InviteFriendsDialogController().show(router)
    }

    private fun showFeedback() {
        FeedbackDialogController(object : FeedbackDialogController.FeedbackListener {
            override fun onSendFeedback(feedback: String) {
                if (feedback.isNotEmpty()) {
                    eventLogger.logEvent(
                        "feedback",
                        Bundle().apply { putString("feedback", feedback) }
                    )
                    showShortToast(R.string.feedback_response)
                }
            }

            override fun onChatWithUs() {
                eventLogger.logEvent("feedback_chat")
                openCommunity()
            }
        }).show(router, "feedback-dialog")
    }

    private fun openCommunity() {
        val myIntent = Intent(ACTION_VIEW, Uri.parse(Constants.DISCORD_CHAT_LINK))
        startActivity(myIntent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        navigationItemSelected = when {
        // do not allow re-selecting the same item (creates same Controller again)
            view!!.navigationView.menu.findItem(item.itemId).isChecked -> null
            else -> item
        }

        view!!.drawerLayout.closeDrawer(GravityCompat.START)
        return false
    }

    override fun onAttach(view: View) {
        setToolbar(view.toolbar)
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBarDrawerToggle.syncState()

        super.onAttach(view)
        view.navigationView.bringToFront()

        val childRouter = getChildRouter(view.childControllerContainer, null)
        if (!childRouter.hasRootController()) {
            childRouter.setRoot(
                RouterTransaction.with(ScheduleViewController())
                    .pushChangeHandler(fadeChangeHandler)
                    .popChangeHandler(fadeChangeHandler)
            )
        }
    }

    override fun onDetach(view: View) {
        view.rootCoordinator.bringToFront()
        super.onDetach(view)
    }

    private fun changeChildController(controller: Controller) {
        val childRouter = getChildRouter(view!!.childControllerContainer, null)
        childRouter.setRoot(
            RouterTransaction.with(controller)
                .pushChangeHandler(fadeChangeHandler)
                .popChangeHandler(fadeChangeHandler)
        )
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {

            android.R.id.home -> {
                view!!.drawerLayout.openDrawer(GravityCompat.START)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    private fun showAuth() {
        router.pushController(
            RouterTransaction.with(AuthViewController())
                .pushChangeHandler(fadeChangeHandler)
                .popChangeHandler(fadeChangeHandler)
        )
    }


    private fun showPet() {
        router.pushController(
            RouterTransaction.with(PetViewController())
                .pushChangeHandler(fadeChangeHandler)
                .popChangeHandler(fadeChangeHandler)
        )
    }

    override fun render(state: HomeViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                renderSignIn(view, state.showSignIn)
                renderBucketList(state.bucketListQuestCount, view)
                renderPlayer(view, state)
                renderTags(view, state)
            }

            PLAYER_CHANGED ->
                renderPlayer(view, state)

            TAGS_CHANGED ->
                renderTags(view, state)

            TAG_SELECTED -> {
                val fadeChangeHandler = FadeChangeHandler()
                pushWithRootRouter(
                    RouterTransaction.with(
                        TagViewController(state.tags[state.selectedTagIndex!!].id)
                    )
                        .pushChangeHandler(fadeChangeHandler)
                        .popChangeHandler(fadeChangeHandler)
                )
            }

            UNSCHEDULED_QUESTS_CHANGED ->
                renderBucketList(state.bucketListQuestCount, view)
        }
    }

    private fun renderBucketList(bucketListQuestCount: Int, view: View) {
        val item = view.navigationView.menu.findItem(R.id.bucketList)

        item.actionView =
            LayoutInflater.from(view.context).inflate(R.layout.menu_item_tag_view, null)
        item.actionView.questCount.text = bucketListQuestCount.toString()
    }

    private fun renderTags(view: View, state: HomeViewState) {
        TAG_IDS.forEach { view.navigationView.menu.removeItem(it) }

        val dropDown = view.navigationView.findViewById<ImageView>(R.id.dropDown)
        dropDown.visible()

        if (state.tags.isEmpty()) {
            dropDown.invisible()
            return
        }

        val tagItems = mutableListOf<MenuItem>()
        state.tags.forEachIndexed { index, tag ->
            tagItems.add(
                createTagForNavigationDrawer(
                    view = view,
                    tagId = TAG_IDS[index],
                    name = tag.name,
                    icon = state.tagIcon(tag),
                    iconColor = tag.color.androidColor.color500,
                    questCount = tag.questCount,
                    isVisible = state.showTags
                )
            )
        }

        dropDown.rotation = if (!state.showTags) 0f else 180f
        dropDown.setOnClickListener {
            val shouldClose = tagItems.isEmpty() || tagItems.first().isVisible
            val rotationDegree = if (shouldClose) 0f else 180f
            it.rotation = rotationDegree
            tagItems.forEach {
                it.isVisible = !it.isVisible
            }
            if (shouldClose) {
                dispatch(HomeAction.HideTags)
            } else {
                dispatch(HomeAction.ShowTags)
            }
        }


    }

    private fun renderPlayer(view: View, state: HomeViewState) {
        Glide.with(view.context).load(state.avatarImage)
            .apply(RequestOptions.circleCropTransform())
            .into(view.playerAvatar)

        view.playerAvatar.setOnClickListener {
            router.pushController(
                RouterTransaction.with(AvatarStoreViewController())
                    .pushChangeHandler(fadeChangeHandler)
                    .popChangeHandler(fadeChangeHandler)
            )
        }

        Glide.with(view.context).load(state.petHeadImage)
            .apply(RequestOptions.circleCropTransform())
            .into(view.petHeadImage)


        view.petContainer.setOnClickListener {
            showPet()
        }

        view.drawerPlayerGems.text = state.gemsText
        view.drawerPlayerCoins.text = state.lifeCoinsText
        view.drawerCurrentExperience.text = state.experienceText

        view.drawerPlayerTitle.text = state.title(resources!!)

        val drawable = view.petMood.background as GradientDrawable
        drawable.setColor(colorRes(state.petMoodColor))

        view.levelProgress.max = state.maxProgress
        view.levelProgress.progress = state.progress
    }

    private fun createTagForNavigationDrawer(
        view: View,
        @IdRes tagId: Int,
        name: String,
        icon: IIcon,
        questCount: Int,
        @ColorRes iconColor: Int,
        isVisible: Boolean
    ): MenuItem {
        val item = view.navigationView.menu.add(
            R.id.drawerMainGroup,
            tagId,
            0,
            name
        )
        item.actionView =
            LayoutInflater.from(view.context).inflate(R.layout.menu_item_tag_view, null)
        item.actionView.questCount.text = questCount.toString()

        val iconDrawable =
            IconicsDrawable(activity!!)
                .icon(icon)
                .paddingDp(3)
                .sizeDp(24)

        item.icon = iconDrawable
        val ic = item.icon
        ic.mutate()
        ic.setColorFilter(colorRes(iconColor), PorterDuff.Mode.SRC_ATOP)
        item.isVisible = isVisible
        item.isCheckable = true
        return item
    }

    private fun renderSignIn(
        view: View,
        showSignIn: Boolean
    ) {
        if (showSignIn) {
            view.navigationView.signIn.visible()
            view.navigationView.signIn.setOnClickListener {
                showAuth()
            }
        } else {
            view.navigationView.signIn.gone()
        }
    }

    private val HomeViewState.avatarImage
        get() = AndroidAvatar.valueOf(avatar.name).image

    private val HomeViewState.petHeadImage
        get() = AndroidPetAvatar.valueOf(petAvatar.name).headImage

    private val HomeViewState.petMoodColor
        get() = AndroidPetMood.valueOf(petMood.name).color

    private fun HomeViewState.title(resources: Resources): String {
        val titles = resources.getStringArray(R.array.player_titles)
        val titleText = titles[Math.min(titleIndex, titles.size - 1)]
        return stringRes(R.string.player_level, level, titleText)
    }

    private val HomeViewState.gemsText
        get() = formatValue(gems.toLong())

    private val HomeViewState.lifeCoinsText
        get() = formatValue(lifeCoins.toLong())

    private val HomeViewState.experienceText
        get() = formatValue(experience)

    private fun HomeViewState.tagIcon(tag: Tag): IIcon =
        tag.icon?.androidIcon?.icon ?: MaterialDesignIconic.Icon.gmi_label


    private fun formatValue(value: Long): String {
        val valString = value.toString()
        if (value < 1000) {
            return valString
        }
        val main = valString.substring(0, valString.length - 3)
        var result = main
        val tail = valString[valString.length - 3]
        if (tail != '0') {
            result += "." + tail
        }
        return stringRes(R.string.big_value_format, result)
    }

    companion object {
        val TAG_IDS = listOf(
            R.id.tag1,
            R.id.tag2,
            R.id.tag3,
            R.id.tag4,
            R.id.tag5,
            R.id.tag6,
            R.id.tag7,
            R.id.tag8,
            R.id.tag9,
            R.id.tag10
        )
    }
}