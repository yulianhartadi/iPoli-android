package io.ipoli.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.amplitude.api.Amplitude
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.home.HomeViewController
import io.ipoli.android.player.persistence.ProviderType
import io.ipoli.android.player.AuthProvider
import io.ipoli.android.player.Player
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        val hasNoRootController = !router.hasRootController()

        if (playerRepository.find() == null) {
            val player = Player(
                level = 1,
                authProvider = AuthProvider(provider = ProviderType.ANONYMOUS.name)
            )
            playerRepository.save(player)
        }

        if (hasNoRootController) {
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
}