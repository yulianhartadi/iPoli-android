package io.ipoli.android

import android.content.Intent

/**
 * Created by vini on 8/14/17.
 */
interface ActivityStarter {
    fun startActivityForResult(intent: Intent, requestCode: Int): Unit
}