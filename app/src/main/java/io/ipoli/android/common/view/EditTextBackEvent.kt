package io.ipoli.android.common.view

import android.content.Context
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.KeyEvent

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/8/17.
 */

class EditTextBackEvent : AppCompatEditText {

    private var onImeBackListener: EditTextImeBackListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            onImeBackListener?.onImeBack(this, this.text.toString())
        }
        return super.dispatchKeyEvent(event)
    }

    fun setOnEditTextImeBackListener(listener: EditTextImeBackListener?) {
        onImeBackListener = listener
    }

}

interface EditTextImeBackListener {
    fun onImeBack(ctrl: EditTextBackEvent, text: String)
}