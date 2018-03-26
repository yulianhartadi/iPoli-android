package mypoli.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import mypoli.android.common.AppState
import mypoli.android.common.LoadDataAction
import mypoli.android.common.di.Module
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.Dispatcher
import mypoli.android.common.redux.SideEffectHandler
import mypoli.android.common.view.playerTheme
import mypoli.android.home.HomeViewController
import mypoli.android.player.auth.AuthViewController
import mypoli.android.quest.timer.TimerViewController
import mypoli.android.store.membership.MembershipViewController
import mypoli.android.store.powerup.AndroidPowerUp
import mypoli.android.store.powerup.buy.BuyPowerUpDialogController
import mypoli.android.store.powerup.middleware.ShowBuyPowerUpAction
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 7/6/17.
 */
class MainActivity : AppCompatActivity(), Injects<Module>, SideEffectHandler<AppState> {

    lateinit var router: Router

    private val playerRepository by required { playerRepository }

    private val stateStore by required { stateStore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(playerTheme)
        setContentView(R.layout.activity_main)

        if (BuildConfig.DEBUG) {

            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, 0)
            }
        }

        incrementAppRun()

        router =
            Conductor.attachRouter(this, findViewById(R.id.controllerContainer), savedInstanceState)
        router.setPopsLastView(true)
        inject(myPoliApp.module(this))

        if (!playerRepository.hasPlayer()) {
            router.setRoot(RouterTransaction.with(AuthViewController()))
        } else {
            startApp()
        }
    }

    private fun startApp() {

        val startIntent = intent
        if (startIntent != null && startIntent.action == ACTION_SHOW_TIMER) {
            val questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY)
            router.setRoot(
                RouterTransaction
                    .with(TimerViewController(questId))
                    .tag(TimerViewController.TAG)
            )
        } else if (!router.hasRootController()) {
//                        router.setRoot(RouterTransaction.with(RepeatingQuestViewController("")))
            router.setRoot(RouterTransaction.with(HomeViewController()))
//                        router.setRoot(RouterTransaction.with(PowerUpStoreViewController()))
        }

        stateStore.dispatch(LoadDataAction.All)

    }

    private fun incrementAppRun() {
        val pm = PreferenceManager.getDefaultSharedPreferences(this)
        val run = pm.getInt(Constants.KEY_APP_RUN_COUNT, 0)
        pm.edit().putInt(Constants.KEY_APP_RUN_COUNT, run + 1).apply()
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

    override fun onNewIntent(intent: Intent) {
        if (ACTION_SHOW_TIMER == intent.action) {

            val questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY)
            router.setRoot(
                RouterTransaction
                    .with(TimerViewController(questId))
                    .tag(TimerViewController.TAG)
            )
        }
        super.onNewIntent(intent)
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

    val rootRouter get() = router

    fun enterFullScreen() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
    }

    fun exitFullScreen() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    override suspend fun execute(action: Action, state: AppState, dispatcher: Dispatcher) {
        launch(UI) {
            when (action) {
                is ShowBuyPowerUpAction -> {
                    showPowerUpDialog(action)
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

    override fun canHandle(action: Action) = action is ShowBuyPowerUpAction

    companion object {
        const val ACTION_SHOW_TIMER = "mypoli.android.intent.action.SHOW_TIMER"
    }
}