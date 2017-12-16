package io.ipoli.android

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.amplitude.api.Amplitude
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.view.playerTheme
import io.ipoli.android.home.HomeViewController
import io.ipoli.android.player.AuthProvider
import io.ipoli.android.player.Player
import io.ipoli.android.player.persistence.model.ProviderType
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 7/6/17.
 */
class MainActivity : AppCompatActivity(), Injects<ControllerModule> {

    lateinit var router: Router

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

        router = Conductor.attachRouter(this, findViewById(R.id.controllerContainer), savedInstanceState)
        inject(iPoliApp.controllerModule(this, router))

        if (playerRepository.find() == null) {
            val player = Player(
                authProvider = AuthProvider(provider = ProviderType.ANONYMOUS.name)
            )
            playerRepository.save(player)
            petStatsChangeScheduler.schedule()
        }

        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(HomeViewController()))
        }
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

    fun animateFadeIn() {
        val root = findViewById<CoordinatorLayout>(R.id.rootCoordinator)
        root.alpha = 0f
        val fadeIn = ObjectAnimator.ofFloat(root, "alpha", 0f, 1f)
        fadeIn.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        fadeIn.start()
    }
}