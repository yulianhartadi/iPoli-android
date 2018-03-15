package mypoli.android.challenge.add

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import kotlinx.android.synthetic.main.controller_add_challenge_name.view.*
import mypoli.android.R
import mypoli.android.challenge.add.AddChallengeNameViewState.StateType.*
import mypoli.android.challenge.entity.Challenge
import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.Validator
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.view.ColorPickerDialogController
import mypoli.android.common.view.IconPickerDialogController
import mypoli.android.common.view.colorRes
import mypoli.android.quest.Color
import mypoli.android.quest.Icon

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/8/18.
 */

sealed class AddChallengeNameAction : Action {
    object Load : AddChallengeNameAction()
    data class ChangeColor(val color: Color) : AddChallengeNameAction()
    data class ChangeIcon(val icon: Icon?) : AddChallengeNameAction()
    data class ChangeDifficulty(val position: Int) : AddChallengeNameAction()
    object Next : AddChallengeNameAction()
    data class Validate(val name: String) : AddChallengeNameAction()
}

object AddChallengeNameReducer : BaseViewStateReducer<AddChallengeNameViewState>() {

    override val stateKey = key<AddChallengeNameViewState>()


    override fun reduce(
        state: AppState,
        subState: AddChallengeNameViewState,
        action: Action
    ) =
        when (action) {
            AddChallengeNameAction.Load -> {
                val parentState = state.stateFor(AddChallengeViewState::class.java)
                subState.copy(
                    type = DATA_LOADED,
                    name = parentState.name,
                    color = parentState.color,
                    icon = parentState.icon,
                    difficulty = parentState.difficulty
                )
            }

            is AddChallengeNameAction.ChangeColor ->
                subState.copy(
                    type = COLOR_CHANGED,
                    color = action.color
                )

            is AddChallengeNameAction.ChangeIcon ->
                subState.copy(
                    type = ICON_CHANGED,
                    icon = action.icon
                )

            is AddChallengeNameAction.ChangeDifficulty ->
                subState.copy(
                    type = DIFFICULTY_CHANGED,
                    difficulty = Challenge.Difficulty.values()[action.position]
                )

            is AddChallengeNameAction.Validate -> {
                val errors = Validator.validate(action).check<ValidationError> {
                    "name" {
                        given { name.isEmpty() } addError ValidationError.EMPTY_NAME
                    }
                }
                subState.copy(
                    type = if (errors.isEmpty()) {
                        VALIDATION_SUCCESSFUL
                    } else {
                        VALIDATION_ERROR_EMPTY_NAME
                    },
                    name = action.name
                )
            }
            else -> subState
    }

    override fun defaultState() =
        AddChallengeNameViewState(
            type = INITIAL,
            name = "",
            color = Color.GREEN,
            icon = null,
            difficulty = Challenge.Difficulty.NORMAL
        )

    enum class ValidationError {
        EMPTY_NAME
    }
}



data class AddChallengeNameViewState(
    val type: AddChallengeNameViewState.StateType,
    val name: String,
    val color: Color,
    val icon: Icon?,
    val difficulty: Challenge.Difficulty
) : ViewState {
    enum class StateType {
        INITIAL,
        DATA_LOADED,
        COLOR_CHANGED,
        ICON_CHANGED,
        DIFFICULTY_CHANGED,
        VALIDATION_ERROR_EMPTY_NAME,
        VALIDATION_SUCCESSFUL
    }
}

class AddChallengeNameViewController(args: Bundle? = null) :
    ReduxViewController<AddChallengeNameAction, AddChallengeNameViewState, AddChallengeNameReducer>(
        args
    ) {
    override val reducer = AddChallengeNameReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(false)
        val view = inflater.inflate(R.layout.controller_add_challenge_name, container, false)

        view.challengeDifficulty.background.setColorFilter(
            colorRes(R.color.md_white),
            PorterDuff.Mode.SRC_ATOP
        )
        view.challengeDifficulty.adapter = ArrayAdapter<String>(
            view.context,
            R.layout.item_add_challenge_difficulty_item,
            R.id.spinnerItemId,
            view.resources.getStringArray(R.array.difficulties)
        )
        return view
    }

    override fun onCreateLoadAction() =
        AddChallengeNameAction.Load

    override fun colorLayoutBars() {}

    override fun render(state: AddChallengeNameViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                renderColor(view, state)
                renderIcon(view, state)

                view.challengeDifficulty.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            dispatch(AddChallengeNameAction.ChangeDifficulty(position))
                        }

                    }

                view.challengeNext.setOnClickListener {
                    dispatch(AddChallengeNameAction.Validate(view.challengeName.text.toString()))
                }
            }

            COLOR_CHANGED -> {
                renderColor(view, state)
            }

            ICON_CHANGED -> {
                renderIcon(view, state)
            }

            DIFFICULTY_CHANGED -> {
            }

            VALIDATION_ERROR_EMPTY_NAME -> {
                view.challengeName.error = "Think of a name"
            }

            VALIDATION_SUCCESSFUL -> {
                dispatch(AddChallengeNameAction.Next)
            }
        }
    }

    private fun renderIcon(
        view: View,
        state: AddChallengeNameViewState
    ) {
        view.challengeIcon.setCompoundDrawablesWithIntrinsicBounds(
            IconicsDrawable(view.context)
                .icon(state.iicon)
                .colorRes(R.color.md_white)
                .sizeDp(24),
            null, null, null
        )

        view.challengeIcon.setOnClickListener {
            IconPickerDialogController({ icon ->
                dispatch(AddChallengeNameAction.ChangeIcon(icon))
            }, state.icon?.androidIcon).showDialog(
                router,
                "pick_icon_tag"
            )

        }
    }

    private fun renderColor(
        view: View,
        state: AddChallengeNameViewState
    ) {
        colorLayout(view, state)
        view.challengeColor.setOnClickListener {
            ColorPickerDialogController({
                dispatch(AddChallengeNameAction.ChangeColor(it.color))
            }, state.color.androidColor).showDialog(
                router,
                "pick_color_tag"
            )
        }
    }

    private fun colorLayout(
        view: View,
        state: AddChallengeNameViewState
    ) {
        view.challengeDifficulty.setPopupBackgroundResource(state.color.androidColor.color500)

    }

    private val AddChallengeNameViewState.iicon: IIcon
        get() = icon?.androidIcon?.icon ?: GoogleMaterial.Icon.gmd_local_florist

}