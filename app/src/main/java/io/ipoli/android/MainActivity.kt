package io.ipoli.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import io.ipoli.android.rewards.RewardsController

/**
 * Created by vini on 7/6/17.
 */
class MainActivity : AppCompatActivity() {

    lateinit public var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        router = Conductor.attachRouter(this, findViewById<ViewGroup>(R.id.controller_container), savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(RewardsController()))
        }
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }
}