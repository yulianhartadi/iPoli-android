package io.ipoli.android.common

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.MessageDialog
import com.facebook.share.widget.ShareDialog
import com.google.android.gms.appinvite.AppInviteInvitation
import io.ipoli.android.Constants
import io.ipoli.android.Constants.Companion.FACEBOOK_PACKAGE
import io.ipoli.android.Constants.Companion.TWITTER_PACKAGE
import io.ipoli.android.MyPoliApp
import io.ipoli.android.R
import io.ipoli.android.common.di.UIModule
import io.ipoli.android.common.view.BaseDialogController
import io.ipoli.android.common.view.showLongToast
import io.ipoli.android.common.view.stringRes
import kotlinx.android.synthetic.main.item_share.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required


/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/26/18.
 */
class ShareAppDialogController(args: Bundle? = null) : BaseDialogController(args),
    Injects<UIModule> {

    private val eventLogger by required { eventLogger }

    companion object {
        const val SHARE_APP_REQUEST_CODE = 876
    }

    private var dialogTitle: String = ""
    private var message: String = ""

    constructor(dialogTitle: String, message: String) : this() {
        this.dialogTitle = dialogTitle
        this.message = message
    }

    @SuppressLint("InflateParams")
    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        inject(MyPoliApp.uiModule(MyPoliApp.instance))
        registerForActivityResult(SHARE_APP_REQUEST_CODE)
        activity?.let {
            eventLogger.logCurrentScreen(it, "ShareApp")
        }
        return inflater.inflate(R.layout.dialog_share_app, null)
    }

    override fun onHeaderViewCreated(headerView: View?) {
        headerView!!.dialogHeaderTitle.text = dialogTitle
        val v = ViewUtils.dpToPx(8f, headerView.context).toInt()
        headerView.dialogHeaderIcon.setPadding(v, v, v, v)
        headerView.dialogHeaderIcon.setImageResource(R.drawable.ic_person_add_white_24dp)
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog {

        val inviteIntent = Intent(Intent.ACTION_SEND)
        inviteIntent.type = "text/plain"
        inviteIntent.putExtra(Intent.EXTRA_TEXT, "")

        val adapter =
            ShareDialogAdapter(
                contentView.context,
                filterInviteProviders(contentView.context, inviteIntent)
            )
        dialogBuilder.setAdapter(adapter) { _, item ->
            val sa = adapter.getItem(item)
            val packageName = sa.packageName

            when {
                packageName == null -> {
                    eventLogger.logEvent("share_app", mapOf("provider" to "Firebase"))
                    onInviteWithFirebase(message)
                }
                isFacebook(packageName) -> {
                    eventLogger.logEvent("share_app", mapOf("provider" to "Facebook"))
                    onInviteWithFacebook()
                }
                else -> {
                    var text = message
                    if (isTwitter(packageName)) {
                        text += " via " + Constants.TWITTER_USERNAME
                    }
                    eventLogger.logEvent("share_app", mapOf("provider" to sa.name))

                    inviteIntent.putExtra(Intent.EXTRA_TEXT, text)
                    inviteIntent.`package` = packageName
                    activity!!.startActivity(inviteIntent)
                }
            }
        }
        return dialogBuilder.create()
    }

    private fun onInviteWithFirebase(message: String) {
        val intent = AppInviteInvitation.IntentBuilder(stringRes(R.string.invite_title))
            .setMessage(message)
            .setCustomImage(Uri.parse(Constants.INVITE_IMAGE_URL))
            .setCallToActionText(stringRes(R.string.invite_call_to_action))
            .build()
        activity!!.startActivityForResult(intent, SHARE_APP_REQUEST_CODE)
    }

    private fun onInviteWithFacebook() {
        val linkContent = ShareLinkContent.Builder()
            .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=io.ipoli.android"))
            .build()
        when {
            MessageDialog.canShow(ShareLinkContent::class.java) ->
                MessageDialog.show(
                    activity!!,
                    linkContent
                )
            ShareDialog.canShow(ShareLinkContent::class.java) ->
                ShareDialog.show(
                    activity!!,
                    linkContent
                )
            else -> showLongToast(R.string.invite_request_update_facebook)
        }
    }

    private fun filterInviteProviders(context: Context, inviteIntent: Intent): List<ShareApp> {
        val shareApps = mutableListOf<ShareApp>()
        val apps = context.packageManager.queryIntentActivities(inviteIntent, 0)
        var twitter: ResolveInfo? = null
        for (info in apps) {
            val packageName = info.activityInfo.packageName
            val name = info.loadLabel(context.packageManager).toString()
            if (isTwitter(packageName)) {
                if (name == "Tweet") {
                    twitter = info
                }
                continue
            }
            if (isFacebook(packageName)) {
                continue
            }

            shareApps.add(ShareApp(packageName, name, info.loadIcon(context.packageManager)))
        }

        if (twitter != null) {
            shareApps.add(
                0,
                ShareApp(
                    twitter.activityInfo.packageName,
                    "Twitter",
                    twitter.loadIcon(context.packageManager)
                )
            )
        }

        shareApps.add(
            0,
            ShareApp(
                Constants.FACEBOOK_PACKAGE,
                "Facebook",
                ContextCompat.getDrawable(context, R.drawable.ic_facebook_blue_40dp)!!
            )
        )

        shareApps.add(
            0,
            ShareApp(
                null,
                "Email or SMS",
                ContextCompat.getDrawable(context, R.drawable.ic_email_red_40dp)!!
            )
        )
        return shareApps
    }

    private fun isFacebook(packageName: String) = packageName.startsWith(FACEBOOK_PACKAGE)

    private fun isTwitter(packageName: String) = packageName.startsWith(TWITTER_PACKAGE)

    data class ShareApp(val packageName: String?, val name: String, val icon: Drawable)

    inner class ShareDialogAdapter(context: Context, apps: List<ShareApp>) :
        ArrayAdapter<ShareApp>(context, R.layout.item_share, apps) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            val app = getItem(position)
            if (view == null) {
                view =
                    LayoutInflater.from(context).inflate(R.layout.item_share, parent, false)
            }
            view!!.appName.text = app.name
            view.appIcon.setImageDrawable(app.icon)

            return view
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SHARE_APP_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val inviteIds = AppInviteInvitation.getInvitationIds(resultCode, data!!)

                eventLogger.logEvent(
                    "firebase_invite_sent",
                    mapOf("count" to inviteIds.size)
                )
            } else {
                eventLogger.logEvent("firebase_invite_canceled")
            }
        }
    }
}