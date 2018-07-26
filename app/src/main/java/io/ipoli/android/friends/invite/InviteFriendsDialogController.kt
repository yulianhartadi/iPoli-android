package io.ipoli.android.friends.invite

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.MessageDialog
import com.facebook.share.widget.ShareDialog
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.common.view.*
import io.ipoli.android.friends.invite.InviteFriendsViewState.InviteProvider
import io.ipoli.android.friends.invite.InviteFriendsViewState.StateType
import kotlinx.android.synthetic.main.dialog_invite_friends.*
import kotlinx.android.synthetic.main.dialog_invite_friends.view.*
import kotlinx.android.synthetic.main.view_loader.view.*
import java.net.URI

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 07/10/2018.
 */

sealed class InviteFriendsAction : Action {

    data class CreateLink(val inviteProvider: InviteFriendsViewState.InviteProvider) :
        InviteFriendsAction() {
        override fun toMap() = mapOf("provider" to inviteProvider.name)
    }

    data class LinkReady(val link: URI) : InviteFriendsAction() {
        override fun toMap() = mapOf("link" to link.toString())
    }

    data class CreateLinkError(val error: ErrorType) : InviteFriendsAction() {
        enum class ErrorType { NO_INTERNET, UNKNOWN }

        override fun toMap() = mapOf("error" to error.name)
    }
}

object InviteFriendsReducer : BaseViewStateReducer<InviteFriendsViewState>() {

    override fun reduce(
        state: AppState,
        subState: InviteFriendsViewState,
        action: Action
    ) =
        when (action) {
            is InviteFriendsAction.CreateLink ->
                subState.copy(
                    type = StateType.CREATING_LINK,
                    inviteProvider = action.inviteProvider
                )

            is InviteFriendsAction.LinkReady ->
                subState.copy(
                    type = StateType.INVITE_LINK_CREATED,
                    link = action.link
                )

            is InviteFriendsAction.CreateLinkError ->
                if (action.error == InviteFriendsAction.CreateLinkError.ErrorType.NO_INTERNET) {
                    subState.copy(
                        type = StateType.NO_INTERNET_ERROR
                    )
                } else {
                    subState.copy(
                        type = StateType.UNKNOWN_ERROR
                    )
                }

            else -> subState
        }

    override fun defaultState() =
        InviteFriendsViewState(
            type = StateType.DATA_LOADED,
            link = null,
            inviteProvider = null
        )

    override val stateKey = key<InviteFriendsViewState>()
}

data class InviteFriendsViewState(
    val type: StateType,
    val link: URI?,
    val inviteProvider: InviteProvider?
) : BaseViewState() {
    enum class StateType { DATA_LOADED, CREATING_LINK, INVITE_LINK_CREATED, UNKNOWN_ERROR, NO_INTERNET_ERROR }
    enum class InviteProvider {
        SMS, EMAIL, WHATSAPP, FACEBOOK, LINK
    }
}

class InviteFriendsDialogController(args: Bundle? = null) :
    ReduxDialogController<InviteFriendsAction, InviteFriendsViewState, InviteFriendsReducer>(args) {

    override val reducer = InviteFriendsReducer

    @SuppressLint("InflateParams")
    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_invite_friends, null)

        view.inviteLogo.background.setColorFilter(
            colorRes(R.color.md_blue_500),
            PorterDuff.Mode.SRC_ATOP
        )

        view.inviteProviderFacebook.background.setColorFilter(
            colorRes(R.color.com_facebook_button_background_color),
            PorterDuff.Mode.SRC_ATOP
        )

        view.inviteProviderSms.background.setColorFilter(
            colorRes(R.color.invite_provider_background_sms),
            PorterDuff.Mode.SRC_ATOP
        )

        view.inviteProviderEmail.background.setColorFilter(
            colorRes(R.color.invite_provider_background_email),
            PorterDuff.Mode.SRC_ATOP
        )

        view.inviteProviderLink.background.setColorFilter(
            colorRes(R.color.invite_provider_background_link),
            PorterDuff.Mode.SRC_ATOP
        )

        setInviteProviderClickListener(view, view.inviteProviderSms, InviteProvider.SMS)
        setInviteProviderClickListener(view, view.inviteProviderEmail, InviteProvider.EMAIL)
        setInviteProviderClickListener(view, view.inviteProviderWhatsapp, InviteProvider.WHATSAPP)
        setInviteProviderClickListener(view, view.inviteProviderFacebook, InviteProvider.FACEBOOK)
        setInviteProviderClickListener(view, view.inviteProviderLink, InviteProvider.LINK)

        if (!view.context.isAppInstalled(Constants.WHATSAPP_PACKAGE)) {
            view.inviteProviderWhatsapp.gone()
        } else {
            view.inviteProviderWhatsapp.visible()
        }

        return view
    }

    private fun setInviteProviderClickListener(
        view: View,
        providerView: View,
        inviteProvider: InviteProvider
    ) {
        providerView.dispatchOnClick {
            view.loader.visible()
            view.inviteProviderWhatsapp.gone()
            view.inviteProviderGroup.gone()
            InviteFriendsAction.CreateLink(inviteProvider)
        }
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setCustomTitle(null)
            .create()

    override fun onAttach(view: View) {
        super.onAttach(view)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.inviteClose.setOnClickListener {
            dismiss()
        }
    }

    override fun render(state: InviteFriendsViewState, view: View) {
        when (state.type) {

            StateType.INVITE_LINK_CREATED -> {
                showProviders(view)
                sendLinkToProvider(state.link!!, state.inviteProvider!!)
            }

            StateType.NO_INTERNET_ERROR -> {
                showProviders(view)
                showShortToast(R.string.invite_friends_error_no_internet)
            }

            StateType.UNKNOWN_ERROR -> {
                showProviders(view)
                showShortToast(R.string.invite_friends_error_unknown)
            }

            else -> {
            }
        }
    }

    private fun sendLinkToProvider(
        link: URI,
        inviteProvider: InviteProvider
    ) {
        activity?.let {
            when (inviteProvider) {
                InviteProvider.SMS -> {
                    inviteWithSms(link)
                }

                InviteProvider.EMAIL -> {
                    inviteWithEmail(link)
                }

                InviteProvider.WHATSAPP -> {
                    inviteWithWhatsapp(link)
                }

                InviteProvider.FACEBOOK -> {
                    inviteWithFacebook(link, it)
                }

                InviteProvider.LINK -> {
                    inviteWithLink(link, it)
                }
            }
        }
    }

    private fun inviteWithLink(link: URI, activity: Activity) {
        val clipboard =
            activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newRawUri("mypoli_invite", Uri.parse(link.toString()))
        clipboard.primaryClip = clip
        showShortToast(R.string.invite_link_copied)

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, "$link")
        shareIntent.type = "text/plain"
        startActivity(
            Intent.createChooser(
                shareIntent,
                stringRes(R.string.invite_via)
            )
        )
    }

    private fun inviteWithWhatsapp(link: URI) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(
            Intent.EXTRA_TEXT,
            stringRes(R.string.invite_friends_message, link)
        )
        intent.type = "text/plain"
        intent.setPackage(Constants.WHATSAPP_PACKAGE)
        startActivity(intent)
    }

    private fun inviteWithSms(link: URI) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", "", null))
        intent.putExtra(
            "sms_body",
            stringRes(R.string.invite_friends_message, link)
        )
        startActivity(intent)
    }

    private fun inviteWithEmail(link: URI) {
        val intent = Intent(
            Intent.ACTION_SEND, Uri.fromParts(
                "mailto", "", null
            )
        )
        intent.type = "text/html"
        intent.putExtra(
            Intent.EXTRA_SUBJECT,
            "Invitation to increase your productivity"
        )

        @Suppress("DEPRECATION")
        val linkHtml = Html.fromHtml(
            stringRes(R.string.invite_friends_message, "  ") +
                "\n\n<a href=\"$link\">$link</a>"
        )
        intent.putExtra(
            Intent.EXTRA_TEXT, linkHtml
        )
        startActivity(Intent.createChooser(intent, "Invite friends with "))
    }

    private fun inviteWithFacebook(link: URI, activity: Activity) {
        val linkContent = ShareLinkContent.Builder()
            .setContentUrl(Uri.parse(link.toString()))
            .build()
        when {
            MessageDialog.canShow(ShareLinkContent::class.java) ->
                MessageDialog.show(
                    activity,
                    linkContent
                )
            ShareDialog.canShow(ShareLinkContent::class.java) ->
                ShareDialog.show(
                    activity,
                    linkContent
                )
            else -> showLongToast(R.string.invite_request_update_facebook)
        }
    }

    private fun showProviders(view: View) {
        view.loader.gone()
        view.inviteProviderGroup.visible()
        if (!view.context.isAppInstalled(Constants.WHATSAPP_PACKAGE)) {
            view.inviteProviderWhatsapp.gone()
        } else {
            view.inviteProviderWhatsapp.visible()
        }
    }

}