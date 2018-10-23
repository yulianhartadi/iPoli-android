package io.ipoli.android.common

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore

fun Activity.showImagePicker(requestCode: Int) {
    val i = Intent(Intent.ACTION_GET_CONTENT)
    i.type = "image/*"
    startActivityForResult(i, requestCode)
}

fun Activity.takeImage(requestCode: Int) {
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
        takePictureIntent.resolveActivity(packageManager)?.also {
            startActivityForResult(takePictureIntent, requestCode)
        }
    }
}