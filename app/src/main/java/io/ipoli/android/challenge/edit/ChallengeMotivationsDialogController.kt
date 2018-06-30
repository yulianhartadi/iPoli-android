package io.ipoli.android.challenge.edit

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.common.view.BaseDialogController
import kotlinx.android.synthetic.main.dialog_challenge_motivations.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/12/18.
 */
class ChallengeMotivationsDialogController(args: Bundle? = null) : BaseDialogController(args) {

    private lateinit var motivation1: String
    private lateinit var motivation2: String
    private lateinit var motivation3: String

    private var listener: (String, String, String) -> Unit = { _, _, _ -> }

    constructor(
        motivation1: String,
        motivation2: String,
        motivation3: String,
        listener: (String, String, String) -> Unit
    ) : this() {
        this.motivation1 = motivation1
        this.motivation2 = motivation2
        this.motivation3 = motivation3
        this.listener = listener
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_challenge_motivations, null)
        view.motivation1.setText(motivation1)
        view.motivation2.setText(motivation2)
        view.motivation3.setText(motivation3)
        return view
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setPositiveButton(R.string.dialog_ok, { _, _ ->
                listener(
                    contentView.motivation1.text.toString(),
                    contentView.motivation2.text.toString(),
                    contentView.motivation3.text.toString()
                )
            })
            .setNegativeButton(R.string.cancel, null)
            .create()

    override fun onHeaderViewCreated(headerView: View?) {
        headerView!!.dialogHeaderTitle.setText(R.string.challenge_motivations_question)
        headerView.dialogHeaderIcon.setImageResource(R.drawable.logo)
    }

}