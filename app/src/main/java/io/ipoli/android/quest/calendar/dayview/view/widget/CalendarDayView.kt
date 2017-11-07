package io.ipoli.android.quest.calendar.dayview.view.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.database.DataSetObserver
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.quest.calendar.dayview.view.widget.util.PositionToTimeMapper
import kotlinx.android.synthetic.main.view_calendar_day.view.*
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import java.lang.Math.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */

class CalendarDayView : FrameLayout, StateChangeListener {

    class FSM(initialState: State, private val listener: StateChangeListener) {

        interface Action<in E : Event> {
            fun execute(state: State, event: E): State
        }

        class ActionNotFound(actionKey: Pair<*, *>) :
            RuntimeException("Trying to execute undefined action for (${actionKey.first}, ${actionKey.second})")

        private var currentState: State = initialState
        private val actions = mutableMapOf<Pair<State.Type, KClass<*>>, Action<*>>()

        init {
            listener.onStateChanged(currentState)
        }

        val state: State
            get() = currentState

        fun <E : Event> transition(given: State.Type, on: KClass<E>, execute: (state: State, event: E) -> State) {
            val a = object : Action<E> {
                override fun execute(state: State, event: E): State {
                    val newState = execute(state, event)
                    Timber.d("Transition given ${state.type} when $event to ${newState.type}")
                    return newState
                }
            }
            actions[Pair(given, on)] = a
        }

        fun <E : Event> fire(event: E) {
            val actionKey = Pair(currentState.type, event::class)
            if (actionKey !in actions) {
                throw ActionNotFound(actionKey)
            }
            @Suppress("UNCHECKED_CAST")
            val a = actions[actionKey] as Action<E>
            currentState = a.execute(currentState, event)
            listener.onStateChanged(currentState)
        }
    }

    sealed class Event {
        object CompleteEdit : Event()
        object CompleteEditRequest : Event()
        object CancelEdit : Event()
        object Up : Event()
        data class StartCalendarEventEdit(val view: View, val position: Int) : Event()
        data class StartUnscheduledEventEdit(val view: View, val position: Int) : Event()
        data class StartCalendarEventAdd(val startTime: Time, val duration: Int, val name: String, val backgroundColor: AndroidColor) : Event()
        data class Drag(val y: Float) : Event()
        data class DragTopIndicator(val y: Float) : Event()
        data class DragBottomIndicator(val y: Float) : Event()
        object StartEditName : Event()
        data class UpdateName(val name: String) : Event()
        data class ZoomStart(val zoomDistance: Float) : Event()
        data class Zoom(val zoomDistance: Float, val focusY: Float, val scaleFactor: Float) : Event()
        object ZoomEnd : Event()
        data class ChangeBackgroundColor(val color: AndroidColor) : Event()
        object RemoveEvent : Event()
    }

    companion object {
        val DEFAULT_VISIBLE_HOURS = 9
        val MIN_VISIBLE_HOURS = 6
        val MAX_VISIBLE_HOURS = 16
        val MIN_EVENT_DURATION = 10
        val MAX_EVENT_DURATION = Time.h2Min(4)
        val HOURS_IN_A_DAY = 24
    }

    data class State(
        val type: Type,
        val topDragViewPosition: Float? = null,
        val topDragIndicatorPosition: Float? = null,
        val bottomDragIndicatorPosition: Float? = null,
        val eventAdapterPosition: Int? = null,
        val unscheduledEventAdapterPosition: Int? = null,
        val eventId: String = "",
        val height: Int? = null,
        val name: String? = null,
        val color: AndroidColor? = null,
        val isNewEvent: Boolean = false,
        val visibleHours: Int = DEFAULT_VISIBLE_HOURS,
        val hourHeight: Float = 0f,
        val zoomDistance: Float? = null,
        val isScrollLocked: Boolean = false) {
        enum class Type {
            VIEW, EDIT, DRAG, ZOOM
        }

        val minuteHeight = hourHeight / 60f
    }

    private val inflater
        get() = LayoutInflater.from(context)

    private var dragView: View? = null
    private var lastY: Float? = null

    private lateinit var fsm: FSM

    private lateinit var dragImage: Drawable
    private var dragImageSize: Int = toPx(16)

    private lateinit var editModeBackground: View
    private lateinit var topDragView: View
    private lateinit var bottomDragView: View
    private lateinit var scaleDetector: ScaleGestureDetector

    private var scheduledEventsAdapter: ScheduledEventsAdapter<*>? = null
    private var unscheduledEventsAdapter: UnscheduledEventsAdapter<*>? = null

    private var listener: CalendarChangeListener? = null

    private val eventViews = mutableListOf<View>()
    private val hourCellViews = mutableListOf<View>()
    private lateinit var timeLineView: View

    private val minuteChangeHandler = Handler(Looper.getMainLooper())

    private val dataSetObserver = object : DataSetObserver() {
        override fun onChanged() {
            clearAndAddEventsFromAdapter()
        }

        override fun onInvalidated() {
            clearCalendarEvents()
        }
    }

    private fun clearCalendarEvents() {
        for (v in eventViews) {
            eventContainer.removeView(v)
        }
        eventViews.clear()
    }

    constructor(context: Context) : super(context) {
        initUi(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initUi(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initUi(attrs, defStyleAttr)
    }

    private fun initUi(attrs: AttributeSet?, defStyleAttr: Int) {
        setMainLayout()
        fetchStyleAttributes(attrs, defStyleAttr)

        setupFSM()

        setupScroll()
        setupHourCells()
        setupEditBackgroundView()
        setupUnscheduledEvents()
        setupTimeLine()

        topDragView = addDragView()
        bottomDragView = addDragView()

        scaleDetector = createScaleDetector()

        val delaySec = Duration.ofMinutes(1).seconds - LocalDateTime.now().second
        minuteChangeHandler.postDelayed(
            moveTimeLineToNow(),
            TimeUnit.SECONDS.toMillis(delaySec))

        eventContainer.setOnLongClickListener {

            val yPosition = lastY!! - topLocationOnScreen + scrollView.scrollY
            val minuteHeight = fsm.state.minuteHeight
            val timeMapper = PositionToTimeMapper(minuteHeight)
            val eventStartTime = timeMapper
                .timeAt(yPosition, 15)
            val dragView = addAndPositionDragView(eventStartTime.toPosition(minuteHeight) - scrollView.scrollY, fsm.state.hourHeight.toInt())
            dragView.post {
                this.dragView = dragView
                fsm.fire(Event.StartCalendarEventAdd(eventStartTime, 60, "", AndroidColor.GREEN))
            }
            false
        }
    }

    private fun moveTimeLineToNow(): Runnable =
        Runnable {
            timeLineView.addToTopPosition(fsm.state.minuteHeight)
            minuteChangeHandler.postDelayed(moveTimeLineToNow(), TimeUnit.MINUTES.toMillis(1))
        }

    private fun createScaleDetector(): ScaleGestureDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                fsm.fire(Event.ZoomStart(detector.currentSpan))
                return super.onScaleBegin(detector)
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                fsm.fire(Event.Zoom(detector.currentSpan, detector.focusY, detector.scaleFactor))
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector?) {
                fsm.fire(Event.ZoomEnd)
                super.onScaleEnd(detector)
            }
        })

    private fun setupTimeLine() {
        timeLineView = inflater.inflate(R.layout.calendar_time_line, eventContainer, false)
        timeLineView.setTopPosition(Time.now().toPosition(fsm.state.minuteHeight))
        eventContainer.addView(timeLineView)
    }

    private fun hasZoomedEnough(previousDistance: Float, currentDistance: Float): Boolean {
        val distanceDelta = abs(previousDistance - currentDistance)
        return distanceDelta > toPx(24)
    }

    private fun setupFSM() {
        val hourHeight = getScreenHeight() / DEFAULT_VISIBLE_HOURS.toFloat()
        fsm = FSM(State(State.Type.VIEW, hourHeight = hourHeight), this)

        listenForZoom()

        fsm.transition(State.Type.VIEW, Event.StartCalendarEventEdit::class, { s, e ->
            val topPosition = startDrag(e.view)

            val timeMapper = PositionToTimeMapper(s.minuteHeight)
            val topRelativePos = topPosition - unscheduledEvents.height + scrollView.scrollY
            val event = scheduledEventsAdapter!!.events[e.position]
            listener?.onStartEditScheduledEvent(dragView!!,
                timeMapper.timeAt(topRelativePos),
                timeMapper.timeAt(topRelativePos + dragView!!.height),
                event.name, event.backgroundColor, e.position)

            s.copy(
                type = State.Type.DRAG,
                eventId = event.id,
                name = event.name,
                topDragViewPosition = topPosition,
                topDragIndicatorPosition = topPosition - dragImageSize / 2,
                bottomDragIndicatorPosition = topPosition + dragView!!.height - dragImageSize / 2,
                eventAdapterPosition = e.position,
                height = dragView!!.height,
                color = event.backgroundColor,
                isScrollLocked = true)
        })

        fsm.transition(State.Type.VIEW, Event.StartUnscheduledEventEdit::class, { s, e ->
            val topPosition = startDrag(e.view)

            val event = unscheduledEventsAdapter!!.events[e.position]
            listener?.onStartEditUnscheduledEvent(dragView!!, event.name,
                event.backgroundColor, e.position)
            s.copy(
                type = State.Type.DRAG,
                eventId = event.id,
                name = event.name,
                topDragViewPosition = topPosition,
                topDragIndicatorPosition = topPosition - dragImageSize / 2,
                bottomDragIndicatorPosition = topPosition + dragView!!.height - dragImageSize / 2,
                unscheduledEventAdapterPosition = e.position,
                height = dragView!!.height,
                color = event.backgroundColor,
                isScrollLocked = true)
        })

        fsm.transition(State.Type.VIEW, Event.StartCalendarEventAdd::class, { s, e ->
            scrollView.setOnTouchListener(null)
            setupDragViews(dragView!!)
            editModeBackground.bringToFront()
            setBackgroundTouchListener()
            setDragViewTouchListener()
            setTopDragViewListener()
            setBottomDragViewListener()

            val absPos = dragView!!.topLocationOnScreen.toFloat() - topLocationOnScreen
            val topPosition = roundPositionToMinutes(absPos)

            listener?.onStartEditNewScheduledEvent(dragView!!,
                e.startTime,
                Time.plusMinutes(e.startTime, e.duration),
                e.name, e.backgroundColor)

            s.copy(
                type = State.Type.EDIT,
                eventId = "",
                name = e.name,
                isNewEvent = true,
                topDragViewPosition = topPosition,
                topDragIndicatorPosition = topPosition - dragImageSize / 2,
                bottomDragIndicatorPosition = topPosition + dragView!!.height - dragImageSize / 2,
                height = dragView!!.height,
                color = e.backgroundColor,
                isScrollLocked = true)
        })

        fsm.transition(State.Type.DRAG, Event.Drag::class, { s, e ->
            val topPosition = calculateTopPosition(e.y)

            val (startTime: Time?, endTime: Time?) = calculateStartAndEndTime(topPosition, s)
            listener?.onMoveEvent(dragView!!,
                startTime,
                endTime)
            s.copy(
                topDragViewPosition = topPosition,
                topDragIndicatorPosition = topPosition - dragImageSize / 2,
                bottomDragIndicatorPosition = topPosition + dragView!!.height - dragImageSize / 2)
        })

        fsm.transition(State.Type.DRAG, Event.Up::class, { s, _ ->
            s.copy(type = State.Type.EDIT)
        })

        fsm.transition(State.Type.DRAG, Event.DragTopIndicator::class, { s, e ->
            val topPosition = (ceil(calculateTopPosition(e.y).toDouble())).toFloat()
            val height = (dragView!!.bottom - topPosition).toInt()
            if (!isValidHeightForEvent(height)) {
                return@transition s
            }
            listener?.onMoveEvent(dragView!!, calculateStartTime(topPosition, s), null)

            s.copy(
                topDragViewPosition = topPosition,
                topDragIndicatorPosition = topPosition - dragImageSize / 2,
                bottomDragIndicatorPosition = topPosition + height - dragImageSize / 2,
                height = height)
        })

        fsm.transition(State.Type.DRAG, Event.DragBottomIndicator::class, { s, e ->
            val bottomPosition = calculateBottomPosition(e.y)
            val height = (bottomPosition - dragView!!.top).toInt()
            if (!isValidHeightForEvent(height)) {
                return@transition s
            }

            listener?.onMoveEvent(dragView!!, null,
                calculateEndTime(bottomPosition - height, s))

            s.copy(
                bottomDragIndicatorPosition = bottomPosition - dragImageSize / 2,
                height = height)
        })

        fsm.transition(State.Type.EDIT, Event.Drag::class, { s, _ ->
            s.copy(type = State.Type.DRAG)
        })

        fsm.transition(State.Type.EDIT, Event.DragTopIndicator::class, { s, _ ->
            s.copy(type = State.Type.DRAG)
        })

        fsm.transition(State.Type.EDIT, Event.DragBottomIndicator::class, { s, _ ->
            s.copy(type = State.Type.DRAG)
        })

        fsm.transition(State.Type.EDIT, Event.StartEditName::class, { s, _ ->
            s
        })

        fsm.transition(State.Type.EDIT, Event.UpdateName::class, { s, e ->
            s.copy(name = e.name)
        })

        fsm.transition(State.Type.EDIT, Event.ChangeBackgroundColor::class, { s, e ->
            listener?.onDragViewColorChange(dragView!!, e.color)
            s.copy(color = e.color)
        })


        fsm.transition(State.Type.EDIT, Event.Up::class, { s, _ ->
            listener?.onDragViewClick(dragView!!)
            s
        })

        fsm.transition(State.Type.EDIT, Event.CompleteEditRequest::class, { s, _ ->
            if (s.isNewEvent) {
                listener?.onAddEvent(createCalendarEvent(s, true))
            } else if (s.eventAdapterPosition != null) {
                if (shouldUnscheduleScheduledEvent(s)) {
                    listener?.onEditUnscheduledEvent(createUnscheduledEvent(s))
                } else {
                    listener?.onEditCalendarEvent(createCalendarEvent(s), s.eventAdapterPosition)
                }
            } else if (s.unscheduledEventAdapterPosition != null) {
                if (shouldScheduleUnscheduledEvent(s)) {
                    listener?.onEditUnscheduledCalendarEvent(createCalendarEvent(s), s.unscheduledEventAdapterPosition)
                } else {
                    listener?.onEditUnscheduledEvent(createUnscheduledEvent(s))
                }
            }
            s
        })

        fsm.transition(State.Type.EDIT, Event.CompleteEdit::class, { s, _ ->
            when {
                shouldUnscheduleScheduledEvent(s) ->
                    listener?.onUnscheduleScheduledEvent(s.eventAdapterPosition!!)

                shouldRescheduleScheduledEvent(s) ->
                    listener?.onRescheduleScheduledEvent(
                        s.eventAdapterPosition!!,
                        startTimeForEvent(s),
                        durationForEvent(s)
                    )

                shouldScheduleUnscheduledEvent(s) ->
                    listener?.onScheduleUnscheduledEvent(
                        s.unscheduledEventAdapterPosition!!,
                        startTimeForEvent(s)
                    )

                else -> listener?.onCancelRescheduleUnscheduledEvent()
            }

            prepareForViewState()
            s.copy(
                type = State.Type.VIEW,
                isScrollLocked = false,
                isNewEvent = false,
                eventAdapterPosition = null,
                unscheduledEventAdapterPosition = null)
        })


        fsm.transition(State.Type.EDIT, Event.CancelEdit::class, { s, _ ->
            prepareForViewState()
            s.copy(
                type = State.Type.VIEW,
                isScrollLocked = false,
                isNewEvent = false,
                eventAdapterPosition = null,
                unscheduledEventAdapterPosition = null)
        })

        fsm.transition(State.Type.VIEW, Event.CancelEdit::class, { s, _ ->
            s
        })

        fsm.transition(State.Type.EDIT, Event.RemoveEvent::class, { s, _ ->
            prepareForViewState()
            listener?.onRemoveEvent(s.eventId)
            s.copy(
                type = State.Type.VIEW,
                isScrollLocked = false,
                isNewEvent = false,
                eventAdapterPosition = null,
                unscheduledEventAdapterPosition = null)
        })

        fsm.transition(State.Type.DRAG, Event.StartEditName::class, { s, _ ->
            s.copy(type = State.Type.EDIT)
        })

        fsm.transition(State.Type.DRAG, Event.UpdateName::class, { s, e ->
            s.copy(type = State.Type.EDIT, name = e.name)
        })

        fsm.transition(State.Type.VIEW, Event.ZoomStart::class, { s, e ->
            s.copy(type = State.Type.ZOOM, zoomDistance = e.zoomDistance)
        })

        fsm.transition(State.Type.ZOOM, Event.Zoom::class, { s, e ->
            if (!hasZoomedEnough(s.zoomDistance!!, e.zoomDistance)) {
                return@transition s
            }
            val newState = createNewZoomState(s, e)
            resizeCalendar(newState.hourHeight, newState.minuteHeight)
            scrollToFocusPosition(s.minuteHeight, newState.minuteHeight, e.focusY)

            newState
        })

        fsm.transition(State.Type.ZOOM, Event.ZoomEnd::class, { s, _ ->
            s.copy(type = State.Type.VIEW)
        })

    }

    private fun createCalendarEvent(s: State, isNew: Boolean = false): CalendarEvent {
        return object : CalendarEvent {
            override val id = if (isNew) "" else s.eventId
            override val duration = durationForEvent(s)
            override val startMinute = startTimeForEvent(s).toMinuteOfDay()
            override val name = s.name!!
            override val backgroundColor = s.color!!
        }
    }

    private fun createUnscheduledEvent(s: State): UnscheduledEvent {
        return object : UnscheduledEvent {
            override val id = s.eventId
            override val duration = durationForEvent(s)
            override val name = s.name!!
            override val backgroundColor = s.color!!
        }
    }

    private fun prepareForViewState() {
        removeView(dragView)
        dragView = null

        listenForZoom()
        hideViews(editModeBackground, topDragView, bottomDragView)

        editModeBackground.setOnTouchListener(null)
    }

    private fun shouldScheduleUnscheduledEvent(s: State) =
        !isInUnscheduledEventsArea(dragView!!.topLocationOnScreen.toFloat()) && s.unscheduledEventAdapterPosition != null

    private fun shouldRescheduleScheduledEvent(s: State) =
        !isInUnscheduledEventsArea(dragView!!.topLocationOnScreen.toFloat()) && s.eventAdapterPosition != null

    private fun shouldUnscheduleScheduledEvent(s: State) =
        isInUnscheduledEventsArea(dragView!!.topLocationOnScreen.toFloat()) && s.eventAdapterPosition != null

    private fun calculateStartTime(topPosition: Float, s: State) =
        calculateStartAndEndTime(topPosition, s).first

    private fun calculateEndTime(topPosition: Float, s: State) =
        calculateStartAndEndTime(topPosition, s).second

    private fun calculateStartAndEndTime(topPosition: Float, s: State): Pair<Time?, Time?> {
        if (isInUnscheduledEventsArea(dragView!!.topLocationOnScreen.toFloat())) {
            return Pair(null, null)
        }
        val timeMapper = PositionToTimeMapper(s.minuteHeight)
        val topRelativePos = topPosition - unscheduledEvents.height + scrollView.scrollY
        return Pair(
            timeMapper.timeAt(topRelativePos),
            timeMapper.timeAt(topRelativePos + s.height!!)
        )
    }

    private fun durationForEvent(s: State): Int {
        val timeMapper = PositionToTimeMapper(s.minuteHeight)
        val topRelativePos = dragTopRelativePosition(s)
        val startTime = timeMapper.timeAt(topRelativePos)
        val endTime = timeMapper.timeAt(topRelativePos + s.height!!)
        return endTime.toMinuteOfDay() - startTime.toMinuteOfDay()
    }

    private fun dragTopRelativePosition(s: State) =
        s.topDragViewPosition!! - unscheduledEvents.height + scrollView.scrollY

    private fun startTimeForEvent(s: State): Time {
        val timeMapper = PositionToTimeMapper(s.minuteHeight)
        return timeMapper.timeAt(dragTopRelativePosition(s))
    }

    private fun isInUnscheduledEventsArea(topPosition: Float) =
        topPosition < scrollView.topLocationOnScreen

    private fun startDrag(adapterView: View): Float {
        scrollView.setOnTouchListener(null)
        setupDragViews(dragView!!)
        editModeBackground.bringToFront()
        setAdapterViewTouchListener(adapterView)

        val absPos = dragView!!.topLocationOnScreen.toFloat() - topLocationOnScreen
        return roundPositionToMinutes(absPos)
    }

    private fun calculateBottomPosition(y: Float): Float {
        val absPos = y - topLocationOnScreen
        return roundPositionToMinutes(min(absPos, bottom.toFloat()))
    }

    private fun calculateTopPosition(y: Float): Float {
        val absPos = y - topLocationOnScreen
        val maxTopPosition = (bottom - dragView!!.height).toFloat()
        return roundPositionToMinutes(min(max(0f, absPos), maxTopPosition))
    }

    private fun listenForZoom() {
        scrollView.setOnTouchListener { _, e ->
            scaleDetector.onTouchEvent(e)
            false
        }
    }

    private fun createNewZoomState(state: State, event: Event.Zoom): State {
        val visibleHours = calculateNewVisibleHours(state.visibleHours, event.scaleFactor)
        return state.copy(
            visibleHours = visibleHours,
            zoomDistance = event.zoomDistance,
            hourHeight = getScreenHeight() / visibleHours.toFloat()
        )
    }

    private fun scrollToFocusPosition(prevMinuteHeight: Float, minuteHeight: Float, focusY: Float) {
        val relativeFocusY = focusY + scrollView.scrollY
        val focusTime = PositionToTimeMapper(prevMinuteHeight).timeAt(relativeFocusY, 1)
        val scrollDelta = focusTime.toPosition(minuteHeight) - relativeFocusY
        scrollView.scrollBy(0, scrollDelta.toInt())
    }

    private fun resizeCalendar(hourHeight: Float, minuteHeight: Float) {
        startResizeAnimation()
        resizeHourCells(hourHeight)
        resizeCalendarEvents(minuteHeight)
        resizeTimeLine(minuteHeight)
    }

    private fun resizeTimeLine(minuteHeight: Float) {
        timeLineView.setTopPosition(Time.now().toPosition(minuteHeight))
    }

    private fun resizeCalendarEvents(minuteHeight: Float) {
        val a = scheduledEventsAdapter!!
        eventViews.forEachIndexed { i, adapterView ->
            val event = a.getItem(i)
            adapterView.setPositionAndHeight(
                event.startMinute * minuteHeight,
                (event.duration * minuteHeight).toInt())
            adapterView.post {
                listener?.onZoomEvent(adapterView)
            }
        }
    }

    private fun resizeHourCells(hourHeight: Float) {
        hourCellViews.forEachIndexed { i, hc ->
            val topPosition = (i * hourHeight).toInt()
            hc.setPositionAndHeight(topPosition.toFloat(), hourHeight.toInt())
        }
    }

    private fun startResizeAnimation() {
        val transition = AutoTransition()
        transition.duration = 0
        TransitionManager.beginDelayedTransition(this, transition)
    }

    private fun calculateNewVisibleHours(visibleHours: Int, scaleFactor: Float): Int {
        val vh = if (scaleFactor > 1) visibleHours - 1 else visibleHours + 1
        return min(max(vh, MIN_VISIBLE_HOURS), MAX_VISIBLE_HOURS)
    }

    private fun setAdapterViewTouchListener(adapterView: View) {
        val initialOffset = lastY!! - dragView!!.topLocationOnScreen

        setBackgroundTouchListener()

        adapterView.setOnTouchListener { _, e ->

            val action = e.actionMasked

            if (action == MotionEvent.ACTION_MOVE) {
                fsm.fire(Event.Drag(e.rawY - initialOffset))
            }

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                fsm.fire(Event.Up)
                setDragViewTouchListener()
                setTopDragViewListener()
                setBottomDragViewListener()
                adapterView.setOnTouchListener(null)
            }
            true
        }
    }

    private fun setupScroll() {
        scrollView.isVerticalScrollBarEnabled = false
    }

    private fun setMainLayout() {
        inflater.inflate(R.layout.view_calendar_day, this, true)
    }

    private fun fetchStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CalendarDayView, defStyleAttr, 0)
            dragImage = a.getDrawable(R.styleable.CalendarDayView_dragImage)
            dragImageSize = a.getDimensionPixelSize(R.styleable.CalendarDayView_dragImageSize, dragImageSize)
            a.recycle()
        }
    }

    private fun setupEditBackgroundView() {
        editModeBackground = View(context)
        editModeBackground.layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        editModeBackground.setBackgroundResource(R.color.md_dark_text_12)
        editModeBackground.visibility = View.GONE
        addView(editModeBackground)
    }

    private fun setupHourCells() {
        val hourHeight = fsm.state.hourHeight
        for (hour in 0 until HOURS_IN_A_DAY) {
            val hourView = inflater.inflate(R.layout.calendar_hour_cell, this, false)
            val layoutParams = hourView.layoutParams as MarginLayoutParams
            layoutParams.height = hourHeight.toInt()
            layoutParams.topMargin = (hour * hourHeight).toInt()
            hourCellViews.add(hourView)
            eventContainer.addView(hourView)
        }
    }

    private fun addDragView(): View {
        val view = ImageView(context)
        view.layoutParams = LayoutParams(dragImageSize, dragImageSize)
        view.setImageDrawable(dragImage)
        view.visibility = View.GONE
        addView(view)
        return view
    }

    private fun setupUnscheduledEvents() {
        unscheduledEvents.layoutManager = object : LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
            override fun canScrollVertically() = !fsm.state.isScrollLocked
        }
    }

    fun setCalendarChangeListener(listener: CalendarChangeListener) {
        this.listener = listener
    }

    fun setUnscheduledEventsAdapter(adapter: UnscheduledEventsAdapter<*>) {
        unscheduledEventsAdapter = adapter
        unscheduledEvents.adapter = adapter
    }

    fun setScheduledEventsAdapter(adapter: ScheduledEventsAdapter<*>) {
        this.scheduledEventsAdapter?.unregisterDataSetObserver(dataSetObserver)
        this.scheduledEventsAdapter = adapter
        this.scheduledEventsAdapter?.registerDataSetObserver(dataSetObserver)
        clearAndAddEventsFromAdapter()
    }

    private fun clearAndAddEventsFromAdapter() {
        clearCalendarEvents()
        addEventsFromAdapter()
    }

    fun startEditDragEventName() =
        fsm.fire(Event.StartEditName)

    fun updateDragEventName(name: String) =
        fsm.fire(Event.UpdateName(name))

    fun updateDragBackgroundColor(color: AndroidColor) =
        fsm.fire(Event.ChangeBackgroundColor(color))

    fun onEventUpdated() =
        fsm.fire(Event.CompleteEdit)

    fun onEventValidationError() =
        listener?.onEventValidationError(dragView!!)

    fun getDragViewBackgroundColor() =
        fsm.state.color

    fun isEditedEventNew(): Boolean =
        fsm.state.isNewEvent

    fun getEditedEvent(): EditedEvent {
        return if (fsm.state.eventAdapterPosition != null) {
            EditedEvent(true, fsm.state.eventAdapterPosition!!)
        } else {
            EditedEvent(false, fsm.state.unscheduledEventAdapterPosition!!)
        }
    }

    fun cancelEdit() {
        fsm.fire(Event.CancelEdit)
    }

    fun onRemoveEvent() {
        fsm.fire(Event.RemoveEvent)
    }

    private fun addEventsFromAdapter() {
        val a = scheduledEventsAdapter!!
        val minuteHeight = fsm.state.minuteHeight
        for (i in 0 until a.count) {
            val adapterView = a.getView(i, null, eventContainer)
            val event = a.getItem(i)
            adapterView.setPositionAndHeight(
                event.startMinute * minuteHeight,
                (event.duration * minuteHeight).toInt())
            eventViews.add(adapterView)
            eventContainer.addView(adapterView)
        }
    }

    private fun setBackgroundTouchListener() {
        editModeBackground.setOnTouchListener { _, ev ->
            val action = ev.actionMasked
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                fsm.fire(Event.CompleteEditRequest)
            }
            true
        }
    }

    private fun addAndPositionDragView(adapterView: View): View {
        val yPosition = adapterView.topLocationOnScreen - topLocationOnScreen.toFloat()
        return addAndPositionDragView(yPosition, adapterView.height)
    }

    private fun addAndPositionDragView(yPosition: Float, height: Int): View {
        TransitionManager.beginDelayedTransition(this)
        val dragView = inflater.inflate(R.layout.item_calendar_drag, this, false)
        dragView.setPositionAndHeight(
            yPosition,
            height
        )
        addView(dragView)
        return dragView
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        lastY = ev.rawY
        return super.dispatchTouchEvent(ev)
    }

    private fun setupDragViews(dragView: View) {
        setupTopDragView(dragView)
        setupBottomDragView(dragView)
    }

    private fun setupBottomDragView(editView: View) {
        bottomDragView.elevation = editView.elevation
        bottomDragView.bringToFront()
        positionBottomDragView(editView)
        setBottomDragViewListener()
    }

    private fun setupTopDragView(editView: View) {
        topDragView.elevation = editView.elevation
        topDragView.bringToFront()
        positionTopDragView(editView)
        setTopDragViewListener()
    }

    private fun positionBottomDragView(editView: View) {
        val lp = bottomDragView.layoutParams as MarginLayoutParams
        lp.topMargin = editView.bottom - dragImageSize / 2
        lp.marginStart = editView.left + editView.width / 2 - dragImageSize / 2
        bottomDragView.layoutParams = lp
    }

    private fun positionTopDragView(editView: View) {
        val lp = topDragView.layoutParams as MarginLayoutParams
        lp.topMargin = editView.top - dragImageSize / 2
        lp.marginStart = editView.left + editView.width / 2 - dragImageSize / 2
        topDragView.layoutParams = lp
    }

    private fun setDragViewTouchListener() {
        var initialOffset = lastY!! - dragView!!.topLocationOnScreen
        dragView!!.setOnTouchListener { _, e ->
            val action = e.actionMasked
            if (action == MotionEvent.ACTION_DOWN) {
                initialOffset = e.rawY - dragView!!.topLocationOnScreen
            }
            if (action == MotionEvent.ACTION_MOVE) {
                fsm.fire(Event.Drag(e.rawY - initialOffset))
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                fsm.fire(Event.Up)
            }
            true
        }
    }

    private fun setBottomDragViewListener() {
        bottomDragView.setOnTouchListener { _, e ->
            val action = e.actionMasked
            if (action == MotionEvent.ACTION_MOVE) {
                fsm.fire(Event.DragBottomIndicator(e.rawY))
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                fsm.fire(Event.Up)
            }
            true
        }
    }

    private fun setTopDragViewListener() {
        topDragView.setOnTouchListener { _, e ->
            val action = e.actionMasked
            if (action == MotionEvent.ACTION_MOVE) {
                fsm.fire(Event.DragTopIndicator(e.rawY))
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                fsm.fire(Event.Up)
            }
            true
        }
    }

    override fun onStateChanged(state: State) {
        scrollView.isLocked = state.isScrollLocked

        when (state.type) {
            State.Type.EDIT -> {
                showViews(editModeBackground, topDragView, bottomDragView)
            }
//
            State.Type.DRAG -> {
                showViews(editModeBackground, topDragView, bottomDragView)
                dragView?.setPositionAndHeight(state.topDragViewPosition!!, state.height!!)
                topDragView.setTopPosition(state.topDragIndicatorPosition!!)
                bottomDragView.setTopPosition(state.bottomDragIndicatorPosition!!)
//                dragView?.startTime!!.changeHeight(state.height!! / 4)
            }
//
//            State.Type.VIEW -> {
//                hideViews(editModeBackground, topDragView, bottomDragView)
//            }
        }
    }

    private fun roundPositionToMinutes(position: Int, roundedToMinutes: Int = 5) =
        roundPositionToMinutes(position.toFloat(), roundedToMinutes)

    private fun roundPositionToMinutes(position: Float, roundedToMinutes: Int = 5) =
        PositionToTimeMapper(fsm.state.minuteHeight).timeAt(position, roundedToMinutes).toPosition(fsm.state.minuteHeight)

    private fun View.setPositionAndHeight(yPosition: Float, height: Int) =
        changeLayoutParams<MarginLayoutParams> {
            it.topMargin = yPosition.toInt()
            it.height = height
        }

    private fun View.setTopPosition(yPosition: Float) =
        changeLayoutParams<MarginLayoutParams> { it.topMargin = yPosition.toInt() }

    private fun View.addToTopPosition(delta: Float) =
        changeLayoutParams<MarginLayoutParams> { it.topMargin += delta.toInt() }

    private fun <T : ViewGroup.LayoutParams> View.changeLayoutParams(cb: (layoutParams: T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val lp = layoutParams as T
        cb(lp)
        layoutParams = lp
    }

    private val View.topLocationOnScreen: Int
        get() {
            val location = IntArray(2)
            getLocationOnScreen(location)
            return location[1]
        }

    private fun Time.toPosition(minuteHeight: Float) =
        toMinuteOfDay() * minuteHeight

    private fun isValidHeightForEvent(height: Int): Boolean =
        getMinutesFor(height) in MIN_EVENT_DURATION..MAX_EVENT_DURATION

    private fun getScreenHeight(): Int {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
    }

    private fun getMinutesFor(height: Int): Int =
        (height / fsm.state.minuteHeight).toInt()

    private fun showViews(vararg views: View) =
        views.forEach { it.visibility = View.VISIBLE }

    private fun hideViews(vararg views: View) =
        views.forEach { it.visibility = View.GONE }

    private fun toPx(dp: Int): Int =
        (dp * Resources.getSystem().displayMetrics.density).toInt()

    fun startEventRescheduling(calendarEvent: CalendarEvent) {
        val position = scheduledEventsAdapter!!.events.indexOf(calendarEvent)
        val adapterView = eventViews[position]
        val dragView = addAndPositionDragView(adapterView)
        dragView.post {
            this.dragView = dragView
            fsm.fire(Event.StartCalendarEventEdit(adapterView, position))
        }
    }

    fun startEventRescheduling(unscheduledEvent: UnscheduledEvent) {
        val position = unscheduledEventsAdapter!!.events.indexOf(unscheduledEvent)
        val adapterView = unscheduledEvents.layoutManager.findViewByPosition(position)
        val dragView = addAndPositionDragView(adapterView)
        dragView.post {
            this.dragView = dragView
            fsm.fire(Event.StartUnscheduledEventEdit(adapterView, position))
        }
    }

    fun scrollToNow() {
        scrollTo(Time.now())
    }

    fun scrollTo(time: Time) {
        scrollView.post {
            scrollView.scrollY = scrollPositionFrom(time)
        }
    }

    fun smoothScrollTo(time: Time) {
        val animationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        ObjectAnimator.ofInt(scrollView, "scrollY", scrollPositionFrom(time))
            .setDuration(animationDuration.toLong())
            .start()
    }

    private fun scrollPositionFrom(time: Time): Int {
        val hourOffset = Math.floor((fsm.state.visibleHours * .3)).toInt()
        val scrollHours = max(hourOffset, time.hours - hourOffset)
        return Time.at(scrollHours, time.getMinutes()).toPosition(fsm.state.minuteHeight).toInt()
    }

    override fun onDetachedFromWindow() {
        eventViews.clear()
        hourCellViews.clear()
        minuteChangeHandler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }

    fun setHourAdapter(adapter: HourCellAdapter) {
        hourCellViews.forEachIndexed { h, v ->
            adapter.bind(v, h)
        }
    }

    interface CalendarChangeListener {
        fun onStartEditScheduledEvent(dragView: View, startTime: Time, endTime: Time, name: String, color: AndroidColor, adapterPosition: Int)
        fun onStartEditNewScheduledEvent(dragView: View, startTime: Time, endTime: Time, name: String, color: AndroidColor)
        fun onStartEditUnscheduledEvent(dragView: View, name: String, color: AndroidColor, adapterPosition: Int)
        fun onDragViewClick(dragView: View)
        fun onDragViewColorChange(dragView: View, color: AndroidColor)
        fun onEventValidationError(dragView: View)
        fun onRescheduleScheduledEvent(position: Int, startTime: Time, duration: Int)
        fun onScheduleUnscheduledEvent(position: Int, startTime: Time)
        fun onUnscheduleScheduledEvent(position: Int)
        fun onCancelRescheduleUnscheduledEvent()
        fun onMoveEvent(dragView: View, startTime: Time?, endTime: Time?)
        fun onZoomEvent(adapterView: View)
        fun onAddEvent(event: CalendarEvent)
        fun onEditCalendarEvent(event: CalendarEvent, adapterPosition: Int)
        fun onEditUnscheduledCalendarEvent(event: CalendarEvent, adapterPosition: Int)
        fun onEditUnscheduledEvent(event: UnscheduledEvent)
        fun onRemoveEvent(eventId: String)
    }

    interface HourCellAdapter {
        fun bind(view: View, hour: Int)
    }
}

data class EditedEvent(val isScheduled: Boolean, val adapterPosition: Int)

interface StateChangeListener {
    fun onStateChanged(state: CalendarDayView.State)
}