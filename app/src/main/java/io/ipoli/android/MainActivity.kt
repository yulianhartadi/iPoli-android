package io.ipoli.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import com.bluelinelabs.conductor.Router
import io.ipoli.android.common.di.Module
import io.ipoli.android.reminder.view.ReminderNotificationOverlay
import space.traversal.kapsule.Injects
import space.traversal.kapsule.required
import timber.log.Timber

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

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))
            startActivityForResult(intent, 0)
        }

        DemoSyncJob.scheduleJob()

//        ReminderNotificationOverlay(object : ReminderNotificationOverlay.OnClickListener {
//            override fun onDismiss() {
//            }
//
//            override fun onSnooze() {
//            }
//
//            override fun onDone() {
//                Timber.d("DonnnyyyYYY")
//            }
//        }).show(this)
        finish()

//        router = Conductor.attachRouter(this, findViewById(R.id.controllerContainer), savedInstanceState)
//        inject(iPoliApp.module(this, router))
//        val hasNoRootController = !router.hasRootController()
//
//        PetMessage(object : PetMessage.UndoClickedListener{
//            override fun onClick() {
//            }
//
//        }).show(router)
//        if (playerRepository.find() == null) {
//            val player = Player(authProvider = AuthProvider(provider = ProviderType.ANONYMOUS.name))
//            playerRepository.save(player)
//        }
//
//        if (hasNoRootController) {
//            router.setRoot(RouterTransaction.with(HomeController()))
//        }
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