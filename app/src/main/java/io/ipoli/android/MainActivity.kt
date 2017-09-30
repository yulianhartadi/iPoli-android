package io.ipoli.android

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import io.ipoli.android.home.HomeController
import io.ipoli.android.player.persistence.RealmPlayerRepository
import io.ipoli.android.player.ui.SignInController

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 7/6/17.
 */
class MainActivity : AppCompatActivity() {

    lateinit var router: Router
//
//    @Inject
//    lateinit var playerRepository: PlayerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val daggerComponent = iPoliApp.getComponent(applicationContext)
//        daggerComponent.inject(this)

        router = Conductor.attachRouter(this, findViewById(R.id.controllerContainer), savedInstanceState)
        val hasNoRootController = !router.hasRootController()
        if (hasNoRootController && RealmPlayerRepository().get() == null) {
            router.setRoot(RouterTransaction.with(SignInController()))
        } else if (hasNoRootController) {
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