package io.ipoli.android.common.feedback

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.view.inflate

class FeedbackViewController(args: Bundle? = null) :
    RestoreViewOnCreateController(
        args
    ) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = container.inflate(R.layout.controller_feedback) as WebView
        view.webChromeClient = WebChromeClient()
        @SuppressLint("SetJavaScriptEnabled")
        view.settings.javaScriptEnabled = true
        view.settings.domStorageEnabled = true
        view.loadUrl(Constants.FEEDBACK_LINK)
        return view
    }

}