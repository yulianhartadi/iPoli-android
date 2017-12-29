package mypoli.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.amplitude.api.Amplitude
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import mypoli.android.challenge.data.Challenge
import mypoli.android.challenge.data.Challenge.Category.*
import mypoli.android.common.datetime.Time
import mypoli.android.common.di.ControllerModule
import mypoli.android.common.view.playerTheme
import mypoli.android.home.HomeViewController
import mypoli.android.player.AuthProvider
import mypoli.android.player.Player
import mypoli.android.player.persistence.model.ProviderType
import net.fortuna.ical4j.model.WeekDay
import org.threeten.bp.DayOfWeek
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 7/6/17.
 */
class MainActivity : AppCompatActivity(), Injects<ControllerModule> {

    lateinit var router: Router

    private val database by required { database }

    private val playerRepository by required { playerRepository }
    private val petStatsChangeScheduler by required { lowerPetStatsScheduler }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(playerTheme)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<AppBarLayout>(R.id.appbar).outlineProvider = null

        val amplitudeClient = Amplitude.getInstance().initialize(this, AnalyticsConstants.AMPLITUDE_KEY)
        amplitudeClient.enableForegroundTracking(application)
        if (BuildConfig.DEBUG) {
            Amplitude.getInstance().setLogLevel(Log.VERBOSE)
            amplitudeClient.setOptOut(true)

            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))
                startActivityForResult(intent, 0)
            }
        }

        incrementAppRun()

        router = Conductor.attachRouter(this, findViewById(R.id.controllerContainer), savedInstanceState)
        inject(myPoliApp.controllerModule(this, router))

        if (!playerRepository.hasPlayer()) {
            val player = Player(
                authProvider = AuthProvider(provider = ProviderType.ANONYMOUS.name),
                schemaVersion = Constants.SCHEMA_VERSION
            )
            playerRepository.save(player)
            petStatsChangeScheduler.schedule()
        } else {
            migrateIfNeeded()
        }

        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(HomeViewController()))
        }
    }

    private fun migrateIfNeeded() {
        val playerSchema = playerRepository.findSchemaVersion()
        if (playerSchema == null || playerSchema != Constants.SCHEMA_VERSION) {
            Migration(database).run()
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        router.onActivityResult(requestCode, resultCode, data)
    }

    fun showBackButton() {
        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)
    }

    fun hideBackButton() {
        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(false)
        actionBar.setDisplayShowHomeEnabled(false)
    }

    fun allChallenges() =
        listOf(
            Challenge(
                getString(R.string.challenge_stress_free_mind),
                HEALTH_AND_FITNESS,
                listOf(
                    Challenge.Quest.Repeating(
                        "Meditate every day for 10 min",
                        "Meditate",
                        duration = 10,
                        weekDays = DayOfWeek.values().toList(),
                        startTime = Time.at(19, 0)
                    ),
                    Challenge.Quest.Repeating(
                        "Read a book for 30 min 3 times a week",
                        "Read a book",
                        duration = 30,
                        weekDays = listOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY)
                    ),
                    Challenge.Quest.OneTime(
                        "Share your troubles with a friend",
                        "Share your troubles with a friend",
                        preferredDayOfWeek = DayOfWeek.SATURDAY,
                        duration = 60
                    ),
                    Challenge.Quest.Repeating(
                        "Take a walk for 30 min 5 times a week",
                        "Take a walk",
                        duration = 30,
                        weekDays = listOf(
                            DayOfWeek.MONDAY,
                            DayOfWeek.TUESDAY,
                            DayOfWeek.THURSDAY,
                            DayOfWeek.FRIDAY,
                            DayOfWeek.SUNDAY
                        )
                    ),
                    Challenge.Quest.Repeating(
                        "Say 3 things that I am grateful for every morning",
                        "Say 3 things that I am grateful for",
                        duration = 10,
                        weekDays = DayOfWeek.values().toList(),
                        startTime = Time.at(10, 0)
                    )
                )
            )
        )
}