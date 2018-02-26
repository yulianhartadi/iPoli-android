package mypoli.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.amplitude.api.Amplitude
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import mypoli.android.auth.AuthViewController
import mypoli.android.common.LoadDataAction
import mypoli.android.common.LoaderDialogController
import mypoli.android.common.di.Module
import mypoli.android.common.view.playerTheme
import mypoli.android.home.HomeViewController
import mypoli.android.repeatingquest.edit.EditRepeatingQuestViewController
import mypoli.android.repeatingquest.list.RepeatingQuestListViewController
import mypoli.android.timer.TimerViewController
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 7/6/17.
 */
class MainActivity : AppCompatActivity(), Injects<Module> {

    lateinit var router: Router

    private val database by required { database }

    private val playerRepository by required { playerRepository }
    private val petStatsChangeScheduler by required { lowerPetStatsScheduler }
    private val stateStore by required { stateStore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(playerTheme)
        setContentView(R.layout.activity_main)

        val amplitudeClient =
            Amplitude.getInstance().initialize(this, AnalyticsConstants.AMPLITUDE_KEY)
        amplitudeClient.enableForegroundTracking(application)
        if (BuildConfig.DEBUG) {
            Amplitude.getInstance().setLogLevel(Log.VERBOSE)
            amplitudeClient.setOptOut(true)

            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + packageName)
                )
                startActivityForResult(intent, 0)
            }
        }

        incrementAppRun()

        router =
            Conductor.attachRouter(this, findViewById(R.id.controllerContainer), savedInstanceState)
        router.setPopsLastView(true)
        inject(myPoliApp.module(this))

        if (database.count > 0) {
            startApp()
        } else if (!playerRepository.hasPlayer()) {
            router.setRoot(RouterTransaction.with(AuthViewController()))
            return
        } else {
            startApp()
        }


    }

    private fun startApp() {

        try {
            var loader: LoaderDialogController? = null
            launch(CommonPool) {
                Migration().run({
                    launch(UI) {
                        loader = LoaderDialogController()
                        loader!!.showDialog(router, "loader")
                    }
                })
                launch(UI) {
                    loader?.dismissDialog()
                    stateStore.dispatch(LoadDataAction.All)
                    petStatsChangeScheduler.schedule()
                    val startIntent = intent
                    if (startIntent != null && startIntent.action == ACTION_SHOW_TIMER) {
                        val questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY)
                        router.setRoot(RouterTransaction.with(TimerViewController(questId)))
                    } else if (!router.hasRootController()) {
//                        router.setRoot(RouterTransaction.with(HomeViewController()))
                        router.setRoot(RouterTransaction.with(EditRepeatingQuestViewController("")))
                    }
                }

            }
        } catch (e: Exception) {
            Toast.makeText(
                this@MainActivity,
                R.string.sign_in_no_connection,
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun incrementAppRun() {
        val pm = PreferenceManager.getDefaultSharedPreferences(this)
        val run = pm.getInt(Constants.KEY_APP_RUN_COUNT, 0)
        pm.edit().putInt(Constants.KEY_APP_RUN_COUNT, run + 1).apply()
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
        if (!router.hasRootController()) {
            finish()
        }
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

    companion object {
        const val ACTION_SHOW_TIMER = "mypoli.android.intent.action.SHOW_TIMER"
    }
}