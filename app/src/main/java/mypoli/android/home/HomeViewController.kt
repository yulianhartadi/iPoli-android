package mypoli.android.home

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.amplitude.api.Amplitude
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.controller_home.view.*
import kotlinx.android.synthetic.main.drawer_header_home.view.*
import mypoli.android.Constants
import mypoli.android.MainActivity
import mypoli.android.R
import mypoli.android.auth.AuthViewController
import mypoli.android.challenge.category.ChallengeCategoryListViewController
import mypoli.android.common.EmailUtils
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.view.*
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.pet.AndroidPetMood
import mypoli.android.pet.PetViewController
import mypoli.android.quest.schedule.ScheduleViewController
import mypoli.android.repeatingquest.list.RepeatingQuestListViewController
import mypoli.android.store.GemStoreViewController
import mypoli.android.store.theme.ThemeStoreViewController
import org.json.JSONObject
import space.traversal.kapsule.required
import timber.log.Timber


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

    private var showSignIn = true

    private val fadeChangeHandler = FadeChangeHandler()

    private val sharedPreferences by required { sharedPreferences }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        val contentView = inflater.inflate(R.layout.controller_home, container, false)


        contentView.navigationView.setNavigationItemSelectedListener(this)
        contentView.navigationView.menu.findItem(R.id.signIn).isVisible = showSignIn

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
            R.id.agenda ->
                changeChildController(ScheduleViewController())

            R.id.repeatingQuests ->
                changeChildController(RepeatingQuestListViewController())

            R.id.challenges ->
                changeChildController(ChallengeCategoryListViewController())

            R.id.store -> {
                router.pushController(
                    RouterTransaction.with(GemStoreViewController())
                        .pushChangeHandler(fadeChangeHandler)
                        .popChangeHandler(fadeChangeHandler)
                )
            }

            R.id.themes -> {
                showThemes()
            }

            R.id.feedback -> {
                FeedbackDialogController(object : FeedbackDialogController.FeedbackListener {
                    override fun onSendFeedback(feedback: String) {
                        if (feedback.isNotEmpty()) {
                            Amplitude.getInstance().logEvent(
                                "feedback",
                                JSONObject().put("feedback", feedback)
                            )
                            showShortToast(R.string.feedback_response)
                        }
                    }

                    override fun onChatWithUs() {
                        Amplitude.getInstance().logEvent("feedback_chat")
                        val myIntent = Intent(ACTION_VIEW, Uri.parse(Constants.DISCORD_CHAT_LINK))
                        startActivity(myIntent)
                    }
                }).showDialog(router, "feedback-dialog")
            }

            R.id.contactUs -> {
                EmailUtils.send(
                    activity!!,
                    "Hi",
                    sharedPreferences.getString(Constants.KEY_PLAYER_ID, null),
                    stringRes(R.string.contact_us_email_chooser_title)
                )
            }

            R.id.signIn -> {
                showAuth()
            }
        }

        view!!.navigationView.setCheckedItem(item.itemId)
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

        Timber.d("AAA HVC before")
        super.onAttach(view)
        Timber.d("AAA HVC after")
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
        val handler = FadeChangeHandler()
        router.pushController(
            RouterTransaction.with(AuthViewController())
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )
    }


    private fun showPet() {
        val handler = FadeChangeHandler()
        router.pushController(
            RouterTransaction.with(PetViewController())
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )
    }


    private fun showThemes() {
        val handler = FadeChangeHandler()
        router.pushController(
            RouterTransaction.with(ThemeStoreViewController())
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )
    }

    override fun render(state: HomeViewState, view: View) {
        when (state) {
            is HomeViewState.Initial -> {
                showSignIn = state.showSignIn
            }

            is HomeViewState.PlayerChanged -> {
                view.drawerPlayerGems.text = state.gemsText
                view.drawerPlayerCoins.text = state.lifeCoinsText
                view.drawerCurrentExperience.text = state.experienceText

                view.drawerPlayerTitle.text = state.title(resources!!)

                Glide.with(view.context).load(state.petHeadImage)
                    .apply(RequestOptions.circleCropTransform())
                    .into(view.petHeadImage)

                val drawable = view.petMood.background as GradientDrawable
                drawable.setColor(colorRes(state.petMoodColor))

                view.levelProgress.max = state.maxProgress
                view.levelProgress.progress = state.progress

                view.petContainer.setOnClickListener {
                    showPet()
                }
            }
        }
    }

    private val HomeViewState.PlayerChanged.petHeadImage
        get() = AndroidPetAvatar.valueOf(petAvatar.name).headImage

    private val HomeViewState.PlayerChanged.petMoodColor
        get() = AndroidPetMood.valueOf(petMood.name).color

    private fun HomeViewState.PlayerChanged.title(resources: Resources): String {
        val titles = resources.getStringArray(R.array.player_titles)
        val titleText = titles[Math.min(titleIndex, titles.size - 1)]
        return stringRes(R.string.player_level, level, titleText)
    }

    private val HomeViewState.PlayerChanged.gemsText
        get() = formatValue(gems.toLong())

    private val HomeViewState.PlayerChanged.lifeCoinsText
        get() = formatValue(lifeCoins.toLong())

    private val HomeViewState.PlayerChanged.experienceText
        get() = formatValue(experience)

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
}