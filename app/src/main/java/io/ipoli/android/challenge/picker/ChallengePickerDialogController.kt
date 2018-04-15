package io.ipoli.android.challenge.picker

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import io.ipoli.android.R
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.picker.ChallengePickerViewState.StateType.CHALLENGE_SELECTED
import io.ipoli.android.challenge.picker.ChallengePickerViewState.StateType.DATA_LOADED
import io.ipoli.android.common.view.ReduxDialogController
import io.ipoli.android.common.view.invisible
import io.ipoli.android.common.view.visible
import io.ipoli.android.pet.AndroidPetAvatar
import kotlinx.android.synthetic.main.dialog_challenge_picker.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 4/11/18.
 */
class ChallengePickerDialogController(args: Bundle? = null) :
    ReduxDialogController<ChallengePickerAction, ChallengePickerViewState, ChallengePickerReducer>(
        args
    ) {
    override val reducer = ChallengePickerReducer

    private var challenge: Challenge? = null

    private lateinit var listener: (Challenge?) -> Unit

    constructor(challenge: Challenge? = null, listener: (Challenge?) -> Unit) : this() {
        this.challenge = challenge
        this.listener = listener
    }

    override fun onCreateLoadAction() =
        ChallengePickerAction.Load(challenge)

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View =
        inflater.inflate(R.layout.dialog_challenge_picker, null)

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.challenge_picker_title)
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setPositiveButton(R.string.dialog_ok, null)
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.none, null)
            .create()

    override fun render(state: ChallengePickerViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                changeIcon(state.petHeadImage)

                if (state.showEmpty) {
                    view.challengeGroup.invisible()
                    view.challengeEmpty.visible()
                    return
                }

                view.challengeGroup.visible()
                view.challengeEmpty.invisible()

                renderChallenges(view, state)
            }

            CHALLENGE_SELECTED -> {
                listener(state.selectedChallenge)
                dismiss()
            }

        }
    }

    private fun renderChallenges(
        view: View,
        state: ChallengePickerViewState
    ) {
        view.challengeGroup.removeAllViews()
        state.viewModels.forEach {
            val item = LayoutInflater.from(view.context).inflate(
                R.layout.item_challenge_dialog,
                null
            ) as RadioButton
            item.text = it.name
            item.isChecked = it.isSelected

            item.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    dispatch(ChallengePickerAction.ChangeSelected(it.challenge))
                }
            }

            view.challengeGroup.addView(item)
        }
    }

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            setPositiveButtonListener {
                dispatch(ChallengePickerAction.Select)
            }

            setNeutralButtonListener {
                listener(null)
                dismiss()
            }
        }
    }

    data class ChallengeViewModel(
        val name: String,
        val isSelected: Boolean,
        val challenge: Challenge
    )

    private val ChallengePickerViewState.viewModels: List<ChallengeViewModel>
        get() = challenges.map {
            ChallengeViewModel(it.name, it.id == selectedChallenge?.id, it)
        }

    private val ChallengePickerViewState.petHeadImage
        get() = AndroidPetAvatar.valueOf(petAvatar.name).headImage

}