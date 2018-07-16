package io.ipoli.android

import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import com.amplitude.api.Amplitude
import com.crashlytics.android.Crashlytics
import com.evernote.android.job.JobManager
import com.jakewharton.threetenabp.AndroidThreeTen
import io.fabric.sdk.android.Fabric
import io.ipoli.android.common.di.*
import io.ipoli.android.common.job.myPoliJobCreator
import space.traversal.kapsule.transitive
import timber.log.Timber


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 7/7/17.
 */

class myPoliApp : Application() {

    @Volatile
    private var uiModule: UIModule? = null

    @Volatile
    private var backgroundModule: BackgroundModule? = null

    companion object {

        lateinit var instance: myPoliApp

        fun uiModule(context: Context): UIModule {
            val appInstance = context.applicationContext as myPoliApp
            return appInstance.uiModule
                ?: synchronized(context.applicationContext) {
                    appInstance.uiModule = UIModule(
                        androidModule = MainAndroidModule(context),
                        useCaseModule = MainUseCaseModule(context),
                        stateStoreModule = AndroidStateStoreModule()
                    ).transitive()
                    appInstance.uiModule!!
                }
        }

        fun backgroundModule(context: Context): BackgroundModule {
            val appInstance = context.applicationContext as myPoliApp
            return appInstance.backgroundModule
                ?: synchronized(context.applicationContext) {
                    appInstance.backgroundModule = BackgroundModule(
                        androidModule = MainAndroidModule(context),
                        useCaseModule = MainUseCaseModule(context)
                    ).transitive()
                    appInstance.backgroundModule!!
                }
        }
    }

    @SuppressLint("NewApi")
    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        Amplitude.getInstance().initialize(applicationContext, AnalyticsConstants.AMPLITUDE_KEY)

        if (!BuildConfig.DEBUG) {

            Fabric.with(
                Fabric.Builder(this)
                    .kits(Crashlytics())
                    .debuggable(BuildConfig.DEBUG)
                    .build()
            )

        } else {
            Amplitude.getInstance().setOptOut(true)
            Timber.plant(Timber.DebugTree())
        }

        JobManager.create(this).addJobCreator(myPoliJobCreator())

        instance = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channels = notificationManager.notificationChannels
            if (channels.firstOrNull { it.id == Constants.REMINDERS_NOTIFICATION_CHANNEL_ID } == null) {
                notificationManager.createNotificationChannel(createReminderChannel())
            }
            if (channels.firstOrNull { it.id == Constants.PLAN_DAY_NOTIFICATION_CHANNEL_ID } == null) {
                notificationManager.createNotificationChannel(createPlanDayChannel())
            }
        }
    }

    @SuppressLint("NewApi")
    private fun createPlanDayChannel(): NotificationChannel {
        val channel = NotificationChannel(
            Constants.PLAN_DAY_NOTIFICATION_CHANNEL_ID,
            Constants.PLAN_DAY_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Notifications to plan your day"
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.setSound(
            Uri.parse("android.resource://" + packageName + "/" + R.raw.notification),
            Notification.AUDIO_ATTRIBUTES_DEFAULT
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        return channel
    }

    @SuppressLint("NewApi")
    private fun createReminderChannel(): NotificationChannel {
        val channel = NotificationChannel(
            Constants.REMINDERS_NOTIFICATION_CHANNEL_ID,
            Constants.REMINDERS_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Reminder notifications"
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.setSound(
            Uri.parse("android.resource://" + packageName + "/" + R.raw.notification),
            Notification.AUDIO_ATTRIBUTES_DEFAULT
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        return channel
    }
}