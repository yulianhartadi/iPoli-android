package io.ipoli.android

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Toast
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.LoadDataAction
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.home.HomeAction
import io.ipoli.android.common.home.HomeViewController
import io.ipoli.android.common.migration.MigrationViewController
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.Dispatcher
import io.ipoli.android.common.redux.SideEffectHandler
import io.ipoli.android.common.view.Debounce
import io.ipoli.android.common.view.playerTheme
import io.ipoli.android.onboarding.OnboardViewController
import io.ipoli.android.pet.PetViewController
import io.ipoli.android.planday.PlanDayViewController
import io.ipoli.android.player.Membership
import io.ipoli.android.player.Player
import io.ipoli.android.player.auth.AuthAction
import io.ipoli.android.player.auth.AuthViewController
import io.ipoli.android.quest.schedule.addquest.AddQuestViewController
import io.ipoli.android.quest.show.QuestViewController
import io.ipoli.android.store.membership.MembershipViewController
import io.ipoli.android.store.powerup.AndroidPowerUp
import io.ipoli.android.store.powerup.buy.BuyPowerUpDialogController
import io.ipoli.android.store.powerup.middleware.ShowBuyPowerUpAction
import io.ipoli.android.tag.show.TagAction
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.threeten.bp.LocalDate
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required
import java.util.*


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 7/6/17.
 */
class MainActivity : AppCompatActivity(), Injects<Module>, SideEffectHandler<AppState> {

    lateinit var router: Router

    private val playerRepository by required { playerRepository }
    private val sharedPreferences by required { sharedPreferences }

    private val stateStore by required { stateStore }

    private val migrationExecutor by required { migrationExecutor }

    val rootRouter get() = router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setTheme(playerTheme)
        setContentView(R.layout.activity_main)
        if (!shouldShowQuickAdd(intent)) {
            val backgroundColor = TypedValue().let {
                theme.resolveAttribute(android.R.attr.colorBackground, it, true)
                it.resourceId
            }
            window.setBackgroundDrawableResource(backgroundColor)
            findViewById<ViewGroup>(R.id.activityContainer)
                .setBackgroundResource(backgroundColor)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent =
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            startActivityForResult(intent, 0)
            Toast.makeText(this, R.string.allow_overlay_request, Toast.LENGTH_LONG).show()
        }

        inject(myPoliApp.module(this))
        incrementAppRun()

        router =
            Conductor.attachRouter(
                this,
                findViewById(R.id.controllerContainer),
                savedInstanceState
            )
        router.setPopsLastView(true)

        launch(CommonPool) {
            val hasPlayer = playerRepository.hasPlayer()
            if (!hasPlayer) {
                withContext(UI) {
                    router.setRoot(RouterTransaction.with(OnboardViewController()))
                }
            } else {
                val pSchemaVersion = playerRepository.findSchemaVersion()!!
                if (migrationExecutor.shouldMigrate(pSchemaVersion)) {
                    withContext(UI) {
                        router.setRoot(RouterTransaction.with(MigrationViewController(pSchemaVersion)))
                    }
                } else {
                    val p = playerRepository.find()!!
                    withContext(UI) {
                        if (p.isLoggedIn() && p.username.isNullOrEmpty()) {
                            router.setRoot(RouterTransaction.with(AuthViewController()))
                        } else {
                            stateStore.dispatch(LoadDataAction.All)
                            startApp(p)
                        }
                    }
                }
            }
        }
    }

    private fun startApp(player: Player) {
        if (intent.action == ACTION_SHOW_TIMER) {
            showTimer(intent)
        } else if (shouldShowQuickAdd(intent)) {
            showQuickAdd()
        } else if (intent.action == ACTION_SHOW_PET) {
            showPet()
        } else if (intent.action == ACTION_PLAN_DAY) {
            router.setRoot(RouterTransaction.with(PlanDayViewController()))
        } else if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(HomeViewController()))
            if (Random().nextInt(10) == 1 && player.membership == Membership.NONE) {
                showPremiumSnackbar()
            }
        }
    }

    private fun shouldShowQuickAdd(startIntent: Intent) =
        startIntent.action == ACTION_SHOW_QUICK_ADD

    private fun incrementAppRun() {
        val run = sharedPreferences.getInt(Constants.KEY_APP_RUN_COUNT, 0)
        sharedPreferences.edit().putInt(Constants.KEY_APP_RUN_COUNT, run + 1).apply()
    }

    override fun onResume() {
        super.onResume()
        stateStore.addSideEffectHandler(this)
    }

    override fun onPause() {
        stateStore.removeSideEffectHandler(this)
        super.onPause()
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
        if (!router.hasRootController()) {
            finish()
        }
    }

    private fun showQuickAdd() {
        router.setRoot(
            RouterTransaction.with(
                AddQuestViewController(
                    closeListener = {
                        finish()
                    },
                    currentDate = LocalDate.now(),
                    isFullscreen = true
                )
            )
        )
    }

    private fun showTimer(intent: Intent) {
        val questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY)
        router.setRoot(
            QuestViewController.routerTransaction(questId)
        )
    }

    private fun showPet() {
        router.pushController(RouterTransaction.with(PetViewController(showBackButton = false)))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        router.onActivityResult(requestCode, resultCode, data)
    }

    fun showBackButton() {
        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)
    }

    fun pushWithRootRouter(transaction: RouterTransaction) {
        router.pushController(transaction)
    }

    private fun showPremiumSnackbar() {
        Snackbar.make(
            findViewById(R.id.activityContainer),
            getString(
                R.string.trial_membership_message,
                Constants.POWER_UPS_TRIAL_PERIOD_DAYS
            ),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.go_premium, Debounce.clickListener { _ ->
            router.pushController(RouterTransaction.with(MembershipViewController()))
        }).show()
    }

    override suspend fun execute(action: Action, state: AppState, dispatcher: Dispatcher) {
        withContext(UI) {
            when (action) {
                is ShowBuyPowerUpAction ->
                    showPowerUpDialog(action)

                is TagAction.TagCountLimitReached ->
                    Toast.makeText(
                        this@MainActivity,
                        R.string.max_tag_count_reached,
                        Toast.LENGTH_LONG
                    ).show()

                is HomeAction.ShowPlayerSetup ->
                    router.setRoot(RouterTransaction.with(AuthViewController()))

                AuthAction.GuestCreated,
                AuthAction.PlayerSetupCompleted -> {
                    showPremiumSnackbar()
                }

                is DataLoadedAction.PlayerChanged -> {
                    val editor = sharedPreferences.edit()
                    val player = action.player
                    editor
                        .putString(Constants.KEY_TIME_FORMAT, player.preferences.timeFormat.name)
                        .putInt(Constants.KEY_SCHEMA_VERSION, player.schemaVersion)
                        .apply()
                }
            }
        }
    }

    private fun showPowerUpDialog(action: ShowBuyPowerUpAction) {
        BuyPowerUpDialogController(action.powerUp, { result ->
            when (result) {
                BuyPowerUpDialogController.Result.TooExpensive ->
                    Toast.makeText(
                        this,
                        R.string.power_up_too_expensive,
                        Toast.LENGTH_SHORT
                    ).show()

                is BuyPowerUpDialogController.Result.Bought ->
                    Toast.makeText(
                        this,
                        getString(
                            R.string.power_up_bought,
                            getString(AndroidPowerUp.valueOf(result.powerUp.name).title)
                        ),
                        Toast.LENGTH_LONG
                    ).show()

                is BuyPowerUpDialogController.Result.UnlockAll ->
                    router.pushController(
                        RouterTransaction.with(
                            MembershipViewController()
                        )
                            .pushChangeHandler(FadeChangeHandler())
                            .popChangeHandler(FadeChangeHandler())
                    )
            }
        }).show(router)
    }

    override fun canHandle(action: Action) =
        action is ShowBuyPowerUpAction
            || action === TagAction.TagCountLimitReached
            || action === HomeAction.ShowPlayerSetup
            || action === AuthAction.PlayerSetupCompleted
            || action === AuthAction.GuestCreated
            || action is DataLoadedAction.PlayerChanged

    companion object {
        const val ACTION_SHOW_TIMER = "io.ipoli.android.intent.action.SHOW_TIMER"
        const val ACTION_SHOW_QUICK_ADD = "io.ipoli.android.intent.action.SHOW_QUICK_ADD"
        const val ACTION_SHOW_PET = "io.ipoli.android.intent.action.SHOW_PET"
        const val ACTION_PLAN_DAY = "io.ipoli.android.intent.action.PLAN_DAY"
    }
}