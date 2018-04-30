package io.ipoli.android.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler
import io.ipoli.android.BuildConfig
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.EmailUtils
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.event.calendar.picker.CalendarPickerDialogController
import io.ipoli.android.settings.SettingsViewState.StateType.DATA_CHANGED
import io.ipoli.android.settings.SettingsViewState.StateType.ENABLE_SYNC_CALENDARS
import kotlinx.android.synthetic.main.controller_settings.view.*
import kotlinx.android.synthetic.main.view_default_toolbar.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 4/2/18.
 */
class SettingsViewController(args: Bundle? = null) :
    ReduxViewController<SettingsAction, SettingsViewState, SettingsReducer>(
        args,
        renderDuplicateStates = false
    ) {

    override val reducer = SettingsReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = container.inflate(R.layout.controller_settings)
        setToolbar(view.toolbar)
        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        toolbarTitle = stringRes(R.string.settings)
        showBackButton()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            return router.handleBack()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateLoadAction() = SettingsAction.Load

    override fun render(state: SettingsViewState, view: View) {
        when (state.type) {
            DATA_CHANGED -> {
                renderAboutSection(state, view)
                renderSyncCalendarsSection(state, view)
            }

            ENABLE_SYNC_CALENDARS -> {
                renderSyncCalendarsSection(state, view)
                showSyncCalendars()
            }
        }
    }

    private fun renderSyncCalendarsSection(
        state: SettingsViewState,
        view: View
    ) {
        view.enableSyncCalendars.setOnCheckedChangeListener(null)
        view.enableSyncCalendars.isChecked = state.isCalendarSyncEnabled
        view.syncCalendarsContainer.setOnClickListener {
            dispatch(SettingsAction.ToggleSyncCalendar(!state.isCalendarSyncEnabled))
        }
        view.enableSyncCalendars.setOnCheckedChangeListener { _, isChecked ->
            dispatch(SettingsAction.ToggleSyncCalendar(isChecked))
        }

        view.selectedSyncCalendars.text = state.selectedCalendarsText
        if (state.isCalendarSyncEnabled) {
            view.calendarsToSyncTitle.setTextColor(colorRes(R.color.md_dark_text_87))
            view.selectedSyncCalendars.setTextColor(colorRes(R.color.md_dark_text_54))
            view.selectSyncCalendarsContainer.isEnabled = true
            view.selectSyncCalendarsContainer.setOnClickListener {
                showSyncCalendars()
            }
        } else {
            view.selectSyncCalendarsContainer.isEnabled = false
            view.calendarsToSyncTitle.setTextColor(colorRes(R.color.md_dark_text_38))
            view.selectedSyncCalendars.setTextColor(colorRes(R.color.md_dark_text_38))
            view.selectSyncCalendarsContainer.setOnClickListener(null)
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

    private fun showSyncCalendars() {
        requestPermissions(
            mapOf(Manifest.permission.READ_CALENDAR to stringRes(R.string.allow_read_calendars_perm_reason)),
            Constants.RC_CALENDAR_PERM
        )
    }

    private fun renderAboutSection(state: SettingsViewState, view: View) {

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

    private val SettingsViewState.selectedCalendarsText: String
        get() = if (selectedCalendars == 0) {
            stringRes(R.string.no_calendars_selected_to_sync)
        } else {
            stringRes(R.string.sync_calendars_count, selectedCalendars)
        }

    companion object {
        fun routerTransaction() =
            RouterTransaction.with(SettingsViewController())
                .pushChangeHandler(VerticalChangeHandler())
                .popChangeHandler(VerticalChangeHandler())
    }
}