package io.ipoli.android.common.view

import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView

fun ImageView.loadFromFile(filePath: Uri) {
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, filePath)
    setImageBitmap(bitmap)
}