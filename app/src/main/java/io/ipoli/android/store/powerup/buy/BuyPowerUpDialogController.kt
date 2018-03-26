package io.ipoli.android.store.powerup.buy

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.view.ReduxDialogController
import io.ipoli.android.common.view.colorRes
import io.ipoli.android.common.view.stringRes
import io.ipoli.android.store.powerup.AndroidPowerUp
import io.ipoli.android.store.powerup.PowerUp
import kotlinx.android.synthetic.main.dialog_buy_power_up.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/20/2018.
 */
class BuyPowerUpDialogController :
    ReduxDialogController<BuyPowerUpAction, BuyPowerUpViewState, BuyPowerUpReducer> {

    sealed class Result {
        object UnlockAll : Result()
        data class Bought(val powerUp: PowerUp.Type) : Result()
        object TooExpensive : Result()
    }

    private lateinit var powerUp: PowerUp.Type

    private lateinit var listener: (Result) -> Unit

    override val reducer = BuyPowerUpReducer

    constructor(args: Bundle? = null) : super(args)

    constructor(
        powerUp: PowerUp.Type,
        listener: (Result) -> Unit
    ) : this() {
        this.powerUp = powerUp
        this.listener = listener
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_buy_power_up, null)
        val ap = AndroidPowerUp.valueOf(powerUp.name)
        view.title.text = stringRes(R.string.power_up_dialog_title, stringRes(ap.title))
        view.description.setText(ap.longDescription)
        view.price.text = stringRes(
            R.string.power_up_dialog_price_message,
            powerUp.coinPrice,
            Constants.POWER_UP_PURCHASE_DURATION_DAYS
        )
        return view
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setPositiveButton(R.string.buy_now, null)
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.unlock_all, null)
            .create()

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            setPositiveButtonListener {
                dismiss()
                dispatch(BuyPowerUpAction.Buy(powerUp))
            }
            setNegativeButtonListener {
                dismiss()
            }
            setNeutralButtonListener {
                dismiss()
                listener(Result.UnlockAll)
            }
        }
    }

    override fun onHeaderViewCreated(headerView: View) {
        val ap = AndroidPowerUp.valueOf(powerUp.name)

        headerView.dialogHeaderTitle.setText(R.string.ready_for_power_up)

        val progressViewEmptyBackground =
            headerView.dialogHeaderIcon.background as GradientDrawable

        progressViewEmptyBackground.setColor(colorRes(ap.backgroundColor))

        headerView.dialogHeaderIcon.setImageDrawable(
            IconicsDrawable(headerView.context)
                .icon(ap.icon)
                .paddingDp(8)
                .colorRes(R.color.md_white)
                .sizeDp(24)
        )
    }

    override fun onCreateLoadAction() = BuyPowerUpAction.Load

    override fun render(state: BuyPowerUpViewState, view: View) {

        when (state) {
            is BuyPowerUpViewState.CoinsChanged ->
                changeLifeCoins(state.lifeCoins)

            is BuyPowerUpViewState.Bought -> {
                listener(Result.Bought(powerUp))
                dismiss()
            }

            BuyPowerUpViewState.TooExpensive -> {
                listener(Result.TooExpensive)
                dismiss()
            }
        }
    }
}