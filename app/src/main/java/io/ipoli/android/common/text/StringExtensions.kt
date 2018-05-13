package io.ipoli.android.common.text

fun String.toSnakeCase(): String {
    var text = ""
    var isFirst = true
    forEach {
        if (it.isUpperCase()) {
            if (isFirst)
                isFirst = false
            else
                text += "_"
            text += it.toLowerCase()
        } else {
            text += it
        }
    }
    return text
}