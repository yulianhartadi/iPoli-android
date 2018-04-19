package io.ipoli.android.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.BuildConfig
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.EmailUtils
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.stringRes
import io.ipoli.android.common.view.toolbarTitle
import io.ipoli.android.event.calendar.picker.CalendarPickerDialogController
import kotlinx.android.synthetic.main.controller_settings.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 4/2/18.
 */
class SettingsViewController(args: Bundle? = null) :
    ReduxViewController<SettingsAction, SettingsViewState, SettingsReducer>(args) {
    override val reducer = SettingsReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        toolbarTitle = stringRes(R.string.settings)
        return inflater.inflate(R.layout.controller_settings, null)
    }

    override fun onCreateLoadAction() = SettingsAction.Load

    override fun render(state: SettingsViewState, view: View) {
        when (state) {
            is SettingsViewState.Changed -> {
                renderAboutSection(state, view)
                renderSyncCalendarsSection(state, view)
            }
        }
    }

    private fun renderSyncCalendarsSection(
        state: SettingsViewState.Changed,
        view: View
    ) {
        view.enableSyncCalendars.setOnCheckedChangeListener(null)
        view.enableSyncCalendars.isChecked = state.isCalendarSyncEnabled
        view.enableSyncCalendars.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestPermissions(
                    mapOf(Manifest.permission.READ_CALENDAR to stringRes(R.string.allow_read_calendars_perm_reason)),
                    Constants.RC_CALENDAR_PERM
                )
            } else {
                dispatch(SettingsAction.DisableCalendarsSync)
            }
        }

        view.selectedSyncCalendars.text = state.selectedCalendarsText
        view.selectSyncCalendarsContainer.setOnClickListener {
            requestPermissions(
                mapOf(Manifest.permission.READ_CALENDAR to stringRes(R.string.allow_read_calendars_perm_reason)),
                Constants.RC_CALENDAR_PERM
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, permissions: List<String>) {
        showCalendarPicker()
    }

    private fun showCalendarPicker() {
        CalendarPickerDialogController({ calendars ->
            dispatch(SettingsAction.SyncCalendarsSelected(calendars))
        }).show(router)
    }

    private fun renderAboutSection(state: SettingsViewState.Changed, view: View) {

        view.contactContainer.setOnClickListener {
            EmailUtils.send(
                activity!!,
                "Hi",
                state.playerId,
                stringRes(R.string.contact_us_email_chooser_title)
            )
        }

        view.rateContainer.setOnClickListener {
            val uri = Uri.parse("market://details?id=" + activity!!.packageName)
            val linkToMarket = Intent(Intent.ACTION_VIEW, uri)
            startActivity(linkToMarket)
        }

        view.appVersion.text = BuildConfig.VERSION_NAME
    }

    private val SettingsViewState.Changed.selectedCalendarsText: String
        get() = if (selectedCalendars == 0) {
            stringRes(R.string.no_calendars_selected_to_sync)
        } else {
            stringRes(R.string.sync_calendars_count, selectedCalendars)
        }
}