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
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
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
import mypoli.android.store.GemStoreViewController
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
    val convertGems: Int = 0,
    val enableConvert: Boolean = false,
    val exchangeRateCoins: Int = 0
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
                    convertGems = 0,
                    enableConvert = false,
                    exchangeRateCoins = Constants.GEM_COINS_PRICE

                )
            }

            is ChangeConvertDeal -> {

                state.copy(
                    type = CONVERT_DEAL_CHANGED,
                    convertCoins = state.playerCoins - intent.progress * Constants.GEM_COINS_PRICE,
                    convertGems = intent.progress,
                    enableConvert = intent.progress > 0
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

class CurrencyConverterDialogController :
    MviDialogController<CurrencyConverterViewState, CurrencyConverterDialogController, CurrencyConverterPresenter, CurrencyConverterIntent>() {

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
                view.exchangeRateCoins.text = state.exchangeRateCoins.toString()

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

                enableConvertButton(view, state.enableConvert)

                view.convert.setOnClickListener {
                    playConvertAnimation(view, { send(Convert(view.seekBar.progress)) })
                }

            }

            CONVERT_DEAL_CHANGED -> {
                view.coins.text = state.convertCoins.toString()
                view.gems.text = state.convertGems.toString()
                enableConvertButton(view, state.enableConvert)
            }

            GEMS_CONVERTED -> {

            }

            GEMS_TOO_EXPENSIVE -> {
                Toast.makeText(view.context, stringRes(R.string.iconvert_gems_not_enough_coins), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enableConvertButton(view: View, enabled: Boolean) {
        view.convert.isEnabled = enabled
        if (enabled) {
            view.convert.setTextColor(colorRes(R.color.md_white))
        } else {
            view.convert.setTextColor(colorRes(R.color.md_dark_text_26))
        }
    }

    private fun playConvertAnimation(view: View, endListener: () -> Unit) {
        view.convert.visibility = View.INVISIBLE

        val duration: Long = 1600

        val rotationAnim = createRotationAnimation(view, duration)
        val lifeCoinAnim = createLifeCoinAnimation(view, duration / 2, duration / 4)
        lifeCoinAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                playGemAlphaAnimation(view, duration / 4)
            }
        })

        val set = AnimatorSet()
        set.playTogether(rotationAnim, lifeCoinAnim)

        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.rotationImage.visibility = View.VISIBLE
                view.lifeCoin.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                view.rotationImage.visibility = View.INVISIBLE
                view.lifeCoin.visibility = View.INVISIBLE
                view.lifeCoin.alpha = 1f

                playGemTranslationAnimation(view, endListener)
            }
        })

        set.start()
    }

    private fun playGemAlphaAnimation(view: View, duration: Long) {
        val gemAlphaAnim = ObjectAnimator.ofFloat(view.gem, "alpha", 0f, 1f)
        gemAlphaAnim.duration = duration
        gemAlphaAnim.start()
    }

    private fun createLifeCoinAnimation(view: View, duration: Long, delay: Long): ObjectAnimator {
        val lifeCoinAlphaAnim = ObjectAnimator.ofFloat(view.lifeCoin, "alpha", 0f, 1f, 0f, 1f, 0f)
        lifeCoinAlphaAnim.startDelay = delay
        lifeCoinAlphaAnim.duration = duration
        lifeCoinAlphaAnim.interpolator = AccelerateDecelerateInterpolator()
        return lifeCoinAlphaAnim
    }

    private fun createRotationAnimation(view: View, duration: Long): ObjectAnimator {
        val rotationAnim = ObjectAnimator.ofFloat(view.rotationImage, "rotation", 0f, 720f)
        rotationAnim.duration = duration
        return rotationAnim
    }

    private fun playGemTranslationAnimation(view: View, endListener: () -> Unit) {
        val originalX = view.gem.x
        val originalY = view.gem.y

        val x = ObjectAnimator.ofFloat(view.gem, "x", view.width.toFloat() - 2 * dialog.findViewById<TextView>(R.id.headerGems)!!.width)
        val y = ObjectAnimator.ofFloat(view.gem, "y", 0f)
        val alpha = ObjectAnimator.ofFloat(view.gem, "alpha", 1f, 0f)

        val set = AnimatorSet()
        set.playTogether(x, y, alpha)
        set.duration = longAnimTime

        set.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                endListener()
                view.convert.visibility = View.VISIBLE
                view.gem.x = originalX
                view.gem.y = originalY
            }
        })
        set.start()
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_currency_converter, null)
        view.basicPlanContainer.setOnClickListener {
            showGemStore()
        }
        view.smartestPlanContainer.setOnClickListener {
            showGemStore()
        }
        view.mostExpensivePlanContainer.setOnClickListener {
            showGemStore()
        }
        return view
    }

    private fun showGemStore() {
        val handler = FadeChangeHandler()
        router.pushController(
            RouterTransaction.with(GemStoreViewController())
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )
    }

    override fun onCreateDialog(dialogBuilder: AlertDialog.Builder, contentView: View, savedViewState: Bundle?): AlertDialog =
        dialogBuilder
            .setNegativeButton(R.string.done, null)
            .create()

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.currency_converter_title)
    }

    override fun createHeaderView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.view_currency_converter_dialog_header, null)
}