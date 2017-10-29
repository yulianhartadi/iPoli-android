package io.ipoli.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.view.PetMessage
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
class MainActivity : AppCompatActivity(), Injects<ControllerModule> {

    lateinit var router: Router

    private val playerRepository by required { playerRepository }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))
            startActivityForResult(intent, 0)
        }

//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val id = "iPoli"
//        val channelName = "iPoli"
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val importance = NotificationManager.IMPORTANCE_MIN
//            val channel = NotificationChannel(id, channelName, importance)
//            channel.description = "Reminder notification"
//            channel.enableLights(true)
//            channel.enableVibration(true)
//            notificationManager.createNotificationChannel(channel)
//        }
//
//
//        val notification = NotificationCompat.Builder(this, "iPoli")
//            .setSmallIcon(R.drawable.ic_notification_small)
//            .setContentTitle("Reminder")
//            .setContentText("Reminder")
//            .setDefaults(NotificationCompat.DEFAULT_ALL)
////            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
////            .setPriority(NotificationCompat.PRIORITY_MIN)
////            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            .build()
//
//        notificationManager.notify(1, notification)

//        ReminderNotificationJob.scheduleJob()
//
//        finish()

        router = Conductor.attachRouter(this, findViewById(R.id.controllerContainer), savedInstanceState)
        inject(iPoliApp.controllerModule(this, router))
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