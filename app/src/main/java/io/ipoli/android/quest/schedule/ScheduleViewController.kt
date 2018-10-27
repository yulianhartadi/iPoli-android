package io.ipoli.android.quest.schedule

import android.os.Bundle
import android.view.*
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.navigation.Navigator
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.quest.schedule.ScheduleViewState.StateType.*
import io.ipoli.android.quest.schedule.addquest.AddQuestAnimationHelper
import io.ipoli.android.quest.schedule.agenda.view.AgendaViewController
import kotlinx.android.synthetic.main.controller_schedule.view.*
import kotlinx.android.synthetic.main.view_calendar_toolbar.view.*
import org.threeten.bp.LocalDate

class ScheduleViewController(args: Bundle? = null) :
    ReduxViewController<ScheduleAction, ScheduleViewState, ScheduleReducer>(args) {

    override val reducer = ScheduleReducer

    private var calendarToolbar: ViewGroup? = null

    private lateinit var addQuestAnimationHelper: AddQuestAnimationHelper

    private var viewModeIcon: IIcon = GoogleMaterial.Icon.gmd_event

    private var viewModeTitle = "Calendar"

    private var showDailyChallenge = false

    private var currentDate: LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {

        setHasOptionsMenu(true)
        val view = container.inflate(R.layout.controller_schedule)

        addQuestAnimationHelper = AddQuestAnimationHelper(
            controller = this,
            addContainer = view.addContainer,
            fab = view.addQuest,
            background = view.addContainerBackground
        )
        initAddQuest(view)

        parentController!!.view!!.post {
            addToolbarView(R.layout.view_calendar_toolbar)?.let {
                val ct = it as ViewGroup
                calendarToolbar = ct
                ct.onDebounceClick { _ ->
                    closeAddIfShown {
                        navigateFromRoot().toScheduleSummary(currentDate)
                    }
                }
            }
        }

        setChildController(
            view.contentContainer,
            AgendaViewController(currentDate)
        )
        return view
    }

    override fun onCreateLoadAction() = ScheduleAction.Load(currentDate)

    override fun onDestroyView(view: View) {
        calendarToolbar?.let { removeToolbarView(it) }
        super.onDestroyView(view)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.schedule_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.actionViewMode).setIcon(
            IconicsDrawable(view!!.context)
                .icon(viewModeIcon)
                .colorRes(R.color.md_white)
                .sizeDp(24)
        ).title = viewModeTitle
        menu.findItem(R.id.actionDailyChallenge).isVisible = showDailyChallenge
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {

            R.id.actionAgendaMode -> {
                dispatch(ScheduleAction.ToggleAgendaPreviewMode)
                true
            }

            R.id.actionViewMode -> {
                closeAddIfShown {
                    dispatch(ScheduleAction.ToggleViewMode)
                }

                true
            }

            R.id.actionDailyChallenge -> {
                closeAddIfShown {
                    navigateFromRoot().toDailyChallenge()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }


    private fun initAddQuest(view: View) {
        view.addContainerBackground.setOnClickListener {
            closeAddIfShown()
        }
    }

    private fun closeAddIfShown(endListener: (() -> Unit)? = null) {
        if (view == null) return
        val containerRouter = addContainerRouter(view!!)
        if (containerRouter.hasRootController()) {
            containerRouter.popCurrentController()
            ViewUtils.hideKeyboard(view!!)
            addQuestAnimationHelper.closeAddContainer(endListener)
        } else {
            endListener?.invoke()
        }
    }

    private fun addContainerRouter(view: View) =
        getChildRouter(view.addContainer, "add-quest")

    override fun render(state: ScheduleViewState, view: View) {
        view.addQuest.setOnClickListener {
            addQuestAnimationHelper.openAddContainer(state.currentDate)
        }

        when (state.type) {

            INITIAL -> {
                renderNewDate(state)
                showDailyChallenge = state.showDailyChallenge
                activity?.invalidateOptionsMenu()
            }

            SHOW_DAILY_CHALLENGE_CHANGED -> {
                showDailyChallenge = state.showDailyChallenge
                activity?.invalidateOptionsMenu()
            }

            DATE_AUTO_CHANGED -> {
                renderNewDate(state)
            }

            CALENDAR_DATE_CHANGED -> {
                renderNewDate(state)
            }

            SWIPE_DATE_CHANGED -> {
                renderNewDate(state)
            }

            VIEW_MODE_CHANGED -> {

                val childRouter = getChildRouter(view.contentContainer, null)
                val n = Navigator(childRouter)
                if (state.viewMode == ScheduleViewState.ViewMode.CALENDAR) {
                    n.replaceWithCalendar(state.currentDate)
                } else {
                    n.replaceWithAgenda(state.currentDate)
                }

                viewModeIcon = state.viewModeIcon
                viewModeTitle = state.viewModeTitle
                activity?.invalidateOptionsMenu()
            }

            else -> {
            }
        }
    }

    private fun renderNewDate(state: ScheduleViewState) {
        currentDate = state.currentDate
        calendarToolbar?.day?.text = state.dayText(activity!!)
        calendarToolbar?.date?.text = state.dateText(activity!!)
    }

    fun onStartEdit() {
        view!!.addQuest.visible = false
    }

    fun onStopEdit() {
        view!!.addQuest.visible = true
    }

    private val ScheduleViewState.viewModeIcon: IIcon
        get() = if (viewMode == ScheduleViewState.ViewMode.CALENDAR)
            GoogleMaterial.Icon.gmd_format_list_bulleted
        else
            GoogleMaterial.Icon.gmd_event
}