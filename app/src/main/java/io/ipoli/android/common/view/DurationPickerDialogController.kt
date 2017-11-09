package io.ipoli.android.common.view

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.text.DurationFormatter.formatShort
import kotlinx.android.synthetic.main.dialog_duration_picker.view.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */

class DurationPickerDialogController : BaseDialogController {
    interface DurationPickedListener {
        fun onDurationPicked(minutes: Int)
    }

    private var listener: DurationPickedListener? = null
    private var selectedDuration: Int? = null

    constructor(listener: DurationPickedListener, selectedDuration: Int? = null) : this() {
        this.listener = listener
        this.selectedDuration = selectedDuration
    }

    constructor(args: Bundle? = null) : super(args)

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_duration_picker, null)


        val croller = contentView.croller
        croller.labelSize = ViewUtils.spToPx(14, contentView.context)
        croller.max = 25
        var minutes: Int = Constants.QUEST_MIN_DURATION
        croller.setOnProgressChangedListener { progress ->
            if(progress <= 11) {
                minutes = progress * 5 + 5
            } else if(progress <= 17) {
                minutes = 60 + (progress % 11) * 10
            } else {
                minutes = 120 + (progress % 17) * 15
            }
            croller.label = formatShort(minutes)

        }

        return AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle("Pick minutes")
            .setIcon(R.drawable.pet_5_head)
            .setPositiveButton(R.string.dialog_ok, { _, _ ->
                listener!!.onDurationPicked(minutes)
            })
            .setNegativeButton(R.string.cancel, null)
            .create()
    }
}