package mypoli.android.challenge.edit

import android.app.DatePickerDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.AdapterView
import android.widget.TextView
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.controller_edit_challenge.view.*
import mypoli.android.R
import mypoli.android.challenge.edit.EditChallengeViewState.StateType.*
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.text.DateFormatter
import mypoli.android.common.view.*
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/12/18.
 */
class EditChallengeViewController(args : Bundle? = null) :
    ReduxViewController<EditChallengeAction, EditChallengeViewState, EditChallengeReducer>(args) {

    override val reducer = EditChallengeReducer

    private lateinit var challengeId: String

    constructor(
        challengeId: String
    ) : this() {
        this.challengeId = challengeId
    }

    override fun onCreateLoadAction() =
        EditChallengeAction.Load(challengeId)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_edit_challenge, container, false)
        setToolbar(view.toolbar)
        toolbarTitle = ""
        view.toolbarTitle.text = stringRes(R.string.title_edit_challenge)
        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_challenge_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                router.popCurrentController()
                true
            }
            R.id.actionSave -> {
                dispatch(
                    EditChallengeAction.Validate(
                        view!!.challengeName.text.toString(),
                        view!!.challengeDifficultyValue.selectedItemPosition
                    )
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun render(state: EditChallengeViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                view.challengeName.setText(state.name)
                renderMotivations(view, state)
                renderEndDate(view, state)
                renderDifficulty(view, state)
                renderIcon(view, state)
                renderColor(view, state)
            }

            ICON_CHANGED -> {
                renderIcon(view, state)
            }

            COLOR_CHANGED -> {
                renderColor(view, state)
            }

            END_DATE_CHANGED -> {
                renderEndDate(view, state)
            }

            MOTIVATIONS_CHANGED -> {
                renderMotivations(view, state)
            }

            VALIDATION_ERROR_EMPTY_NAME -> {
                view.challengeName.error = "Think of a name"
            }

            VALIDATION_SUCCESSFUL -> {
                dispatch(EditChallengeAction.Save)
                router.popCurrentController()
            }
        }
    }

    private fun renderColor(
        view: View,
        state: EditChallengeViewState
    ) {
        colorLayout(view, state)
        view.challengeColorContainer.setOnClickListener {
            ColorPickerDialogController({
                dispatch(EditChallengeAction.ChangeColor(it.color))
            }, state.color.androidColor).showDialog(
                router,
                "pick_color_tag"
            )
        }
    }

    private fun renderIcon(
        view: View,
        state: EditChallengeViewState
    ) {
        view.challengeIconIcon.setImageDrawable(state.iconDrawable)
        view.challengeIconContainer.setOnClickListener {
            IconPickerDialogController({ icon ->
                dispatch(EditChallengeAction.ChangeIcon(icon))
            }, state.icon?.androidIcon).showDialog(
                router,
                "pick_icon_tag"
            )
        }
    }

    private fun renderDifficulty(
        view: View,
        state: EditChallengeViewState
    ) {
        view.challengeDifficultyValue.setSelection(state.difficultyIndex)
        styleSelectedDifficulty(view)

        view.challengeDifficultyValue.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int,
                    id: Long
                ) {
                    styleSelectedDifficulty(view)
                }
            }
    }

    private fun styleSelectedDifficulty(view: View) {
        val item = view.challengeDifficultyValue.selectedView as TextView
        item.setTextAppearance(item.context, R.style.TextAppearance_AppCompat_Caption)
        item.setPadding(0, 0, 0, 0)
    }

    private fun renderEndDate(
        view: View,
        state: EditChallengeViewState
    ) {
        view.challengeEndDateValue.text = state.formattedDate
        val date = state.end
        view.challengeEndDateContainer.setOnClickListener {
            DatePickerDialog(
                view.context, R.style.Theme_myPoli_AlertDialog,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    dispatch(
                        EditChallengeAction.ChangeEndDate(
                            LocalDate.of(year, month + 1, dayOfMonth)
                        )
                    )
                }, date.year, date.month.value - 1, date.dayOfMonth
            ).show()
        }
    }

    private fun renderMotivations(
        view: View,
        state: EditChallengeViewState
    ) {
        if (state.motivation1.isNotEmpty()) {
            view.challengeMotivation1Value.visibility = View.VISIBLE
            view.challengeMotivation1Value.text = state.motivation1
        } else {
            view.challengeMotivation1Value.visibility = View.GONE
        }
        if (state.motivation2.isNotEmpty()) {
            view.challengeMotivation2Value.visibility = View.VISIBLE
            view.challengeMotivation2Value.text = state.motivation2
        } else {
            view.challengeMotivation2Value.visibility = View.GONE
        }
        if (state.motivation3.isNotEmpty()) {
            view.challengeMotivation3Value.visibility = View.VISIBLE
            view.challengeMotivation3Value.text = state.motivation3
        } else {
            view.challengeMotivation3Value.visibility = View.GONE
        }

        view.challengeMotivationsContainer.setOnClickListener {
            ChallengeMotivationsDialogController(
                state.motivation1,
                state.motivation2,
                state.motivation3,
                { m1, m2, m3 ->
                    dispatch(EditChallengeAction.ChangeMotivations(m1, m2, m3))
                }
            ).show(router, "motivations")
        }
    }

    private fun colorLayout(
        view: View,
        state: EditChallengeViewState
    ) {
        val color500 = colorRes(state.color500)
        val color700 = colorRes(state.color700)
        view.appbar.setBackgroundColor(color500)
        view.toolbar.setBackgroundColor(color500)
        view.toolbarCollapsingContainer.setContentScrimColor(color500)
        activity?.window?.navigationBarColor = color500
        activity?.window?.statusBarColor = color700
    }

    private val EditChallengeViewState.color500: Int
        get() = color.androidColor.color500

    private val EditChallengeViewState.color700: Int
        get() = color.androidColor.color700

    private val EditChallengeViewState.formattedDate: String
        get() = DateFormatter.format(view!!.context, end)

    private val EditChallengeViewState.difficultyIndex: Int
        get() = difficulty.ordinal

    private val EditChallengeViewState.iconDrawable: Drawable
        get() =
            if (icon == null) {
                ContextCompat.getDrawable(view!!.context, R.drawable.ic_icon_black_24dp)!!
            } else {
                val androidIcon = icon.androidIcon
                IconicsDrawable(view!!.context)
                    .icon(androidIcon.icon)
                    .colorRes(androidIcon.color)
                    .sizeDp(24)
            }
}