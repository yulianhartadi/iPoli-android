package io.ipoli.android

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import io.ipoli.android.common.di.Module
import io.ipoli.android.home.HomeController
import io.ipoli.android.player.persistence.ProviderType
import io.ipoli.android.quest.AuthProvider
import io.ipoli.android.quest.Player
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 7/6/17.
 */
class MainActivity : AppCompatActivity(), Injects<Module> {

    lateinit var router: Router

    private val playerRepository by required { playerRepository }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        ActivityCompat.requestPermissions(this,
//            arrayOf(Manifest.permission.READ_PHONE_STATE),
//            1231)

        router = Conductor.attachRouter(this, findViewById(R.id.controllerContainer), savedInstanceState)
        inject(iPoliApp.module(this, router))
        val hasNoRootController = !router.hasRootController()

        if (playerRepository.find() == null) {
            val player = Player(authProvider = AuthProvider(provider = ProviderType.ANONYMOUS.name))
            playerRepository.save(player)
        }

        if (hasNoRootController) {
            router.setRoot(RouterTransaction.with(HomeController()))
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