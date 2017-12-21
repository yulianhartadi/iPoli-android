package mypoli.android.common.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_currency_converter.view.*
import kotlinx.android.synthetic.main.view_currency_converter_dialog_header.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.Constants
import mypoli.android.R
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.common.view.CurrencyConverterIntent.*
import mypoli.android.common.view.CurrencyConverterViewState.Type.*
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.pet.PetAvatar
import mypoli.android.player.Player
import mypoli.android.player.usecase.ConvertCoinsToGemsUseCase
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import space.traversal.kapsule.required
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/21/17.
 */

sealed class CurrencyConverterIntent : Intent {
    object LoadData : CurrencyConverterIntent()
    data class ChangePlayer(val player: Player) : CurrencyConverterIntent()
    data class ChangeConvertDeal(val progress: Int) : CurrencyConverterIntent()
    data class Convert(val gems: Int) : CurrencyConverterIntent()
}

data class CurrencyConverterViewState(
    val type: Type,
    val petAvatar: PetAvatar? = null,
    val playerCoins: Int = 0,
    val playerGems: Int = 0,
    val maxGemsToConvert: Int = 0,
    val convertCoins: Int = 0,
    val convertGems: Int = 0
) : ViewState {
    enum class Type {
        LOADING,
        DATA_CHANGED,
        CONVERT_DEAL_CHANGED,
        GEMS_CONVERTED,
        GEMS_TOO_EXPENSIVE
    }
}

class CurrencyConverterPresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    private val convertCoinsToGemsUseCase: ConvertCoinsToGemsUseCase,
    coroutineContext: CoroutineContext) :
    BaseMviPresenter<ViewStateRenderer<CurrencyConverterViewState>, CurrencyConverterViewState, CurrencyConverterIntent>(
        CurrencyConverterViewState(LOADING),
        coroutineContext
    ) {
    override fun reduceState(intent: CurrencyConverterIntent, state: CurrencyConverterViewState) =
        when (intent) {
            is CurrencyConverterIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(ChangePlayer(it))
                    }
                }
                state
            }

            is ChangePlayer -> {
                val player = intent.player

                state.copy(
                    type = DATA_CHANGED,
                    petAvatar = player.pet.avatar,
                    playerCoins = player.coins,
                    playerGems = player.gems,
                    maxGemsToConvert = player.coins / Constants.GEM_COINS_PRICE,
                    convertCoins = player.coins,
                    convertGems = 0
                )
            }

            is ChangeConvertDeal -> {

                state.copy(
                    type = CONVERT_DEAL_CHANGED,
                    convertCoins = state.playerCoins - intent.progress * Constants.GEM_COINS_PRICE,
                    convertGems = intent.progress
                )
            }

            is Convert -> {
                val result = convertCoinsToGemsUseCase.execute(ConvertCoinsToGemsUseCase.Params(intent.gems))
                val type = when (result) {
                    is ConvertCoinsToGemsUseCase.Result.TooExpensive -> GEMS_TOO_EXPENSIVE
                    is ConvertCoinsToGemsUseCase.Result.GemsConverted -> GEMS_CONVERTED
                }
                state.copy(
                    type = type
                )
            }
        }
}

class CurrencyConverterController :
    MviDialogController<CurrencyConverterViewState, CurrencyConverterController, CurrencyConverterPresenter, CurrencyConverterIntent>() {

    private val presenter by required { currencyConverterPresenter }

    override fun createPresenter() = presenter

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(LoadData)
    }

    override fun render(state: CurrencyConverterViewState, view: View) {
        when (state.type) {
            DATA_CHANGED -> {
                changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
                dialog.findViewById<TextView>(R.id.headerCoins)!!.text = state.playerCoins.toString()
                dialog.findViewById<TextView>(R.id.headerGems)!!.text = state.playerGems.toString()
                view.coins.text = state.convertCoins.toString()
                view.gems.text = state.convertGems.toString()

                view.seekBar.max = state.maxGemsToConvert
                view.seekBar.progress = 0

                view.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            send(ChangeConvertDeal(progress))
                        }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                    }

                })

                view.convert.setOnClickListener {
                    view.convert.visibility = View.INVISIBLE

                    val duration = 1600

                    val a = ObjectAnimator.ofFloat(view.arrows, "rotation", 0f, 720f)
                    a.duration = duration.toLong()


                    val b = ObjectAnimator.ofFloat(view.lifeCoin, "alpha", 0f, 1f, 0f, 1f, 0f)
                    b.startDelay = (duration / 4).toLong()
                    b.duration = (duration / 2).toLong()
                    b.interpolator = AccelerateDecelerateInterpolator()
                    b.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            val c = ObjectAnimator.ofFloat(view.gem, "alpha", 0f, 1f)
                            c.duration = (duration / 4).toLong()
                            c.start()
                        }
                    })


                    val set = AnimatorSet()
                    set.playTogether(a, b)

                    set.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {
                            view.arrows.visibility = View.VISIBLE
                            view.lifeCoin.visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            view.arrows.visibility = View.INVISIBLE
                            view.lifeCoin.visibility = View.INVISIBLE
                            view.lifeCoin.alpha = 1f

                            val originalX = view.gem.x
                            val originalY = view.gem.y

                            val x = ObjectAnimator.ofFloat(view.gem, "x", view.width.toFloat() - 2 * dialog.findViewById<TextView>(R.id.headerGems)!!.width)
                            val y = ObjectAnimator.ofFloat(view.gem, "y", 0f)
                            val alpha = ObjectAnimator.ofFloat(view.gem, "alpha", 1f, 0f)

                            val s = AnimatorSet()
                            s.playTogether(x, y, alpha)
                            s.duration = longAnimTime

                            s.addListener(object : AnimatorListenerAdapter() {

                                override fun onAnimationEnd(animation: Animator?) {
                                    send(Convert(view.seekBar.progress))
                                    view.convert.visibility = View.VISIBLE
                                    view.gem.x = originalX
                                    view.gem.y = originalY
                                }
                            })
                            s.start()
                        }
                    })

                    set.start()
                }

            }

            CONVERT_DEAL_CHANGED -> {
                view.coins.text = state.convertCoins.toString()
                view.gems.text = state.convertGems.toString()
            }

            GEMS_CONVERTED -> {

            }

            GEMS_TOO_EXPENSIVE -> {
                Toast.makeText(view.context, stringRes(R.string.iconvert_gems_not_enough_coins), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_currency_converter, null)
    }

    override fun onCreateDialog(dialogBuilder: AlertDialog.Builder, contentView: View, savedViewState: Bundle?): AlertDialog =
        dialogBuilder
            .setNegativeButton("Cancel", null)
            .create()

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.currency_converter_title)
    }

    override fun createHeaderView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.view_currency_converter_dialog_header, null)
}