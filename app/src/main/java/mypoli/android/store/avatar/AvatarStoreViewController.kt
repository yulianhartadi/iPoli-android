package mypoli.android.store.avatar

import android.os.Bundle
import android.support.annotation.DrawableRes
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.controller_avatar_store.view.*
import kotlinx.android.synthetic.main.item_store_avatar.view.*
import kotlinx.android.synthetic.main.view_inventory_toolbar.view.*
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.view.*
import mypoli.android.common.view.pager.BasePagerAdapter
import mypoli.android.player.data.AndroidAvatar
import mypoli.android.player.data.Avatar
import mypoli.android.player.inventory.InventoryViewController

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/24/2018.
 */
class AvatarStoreViewController(args: Bundle? = null) :
    ReduxViewController<AvatarStoreAction, AvatarStoreViewState, AvatarStoreReducer>(args) {

    override val reducer = AvatarStoreReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(
            R.layout.controller_avatar_store, container, false
        )

        view.avatarPager.adapter = AvatarAdapter()

        view.avatarPager.clipToPadding = false
        view.avatarPager.pageMargin = ViewUtils.dpToPx(8f, view.context).toInt()

        setChildController(
            view.playerGems,
            InventoryViewController(
                showCurrencyConverter = true,
                showCoins = false,
                showGems = true
            )
        )

        return view
    }

    override fun onCreateLoadAction() = AvatarStoreAction.Load

    override fun onAttach(view: View) {
        super.onAttach(view)
        setToolbar(view.toolbar)
        showBackButton()
        view.toolbarTitle.text = stringRes(R.string.controller_avatar_store_title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            return router.handleBack()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun render(state: AvatarStoreViewState, view: View) {
        when (state) {
            is AvatarStoreViewState.Changed ->
                (view.avatarPager.adapter as AvatarAdapter).updateAll(state.avatarViewModels)

            AvatarStoreViewState.AvatarBought ->
                showLongToast(R.string.avatar_bought)

            AvatarStoreViewState.AvatarTooExpensive -> {
                CurrencyConverterDialogController().showDialog(router, "currency-converter")
                showShortToast(R.string.avatar_too_expensive)
            }
        }
    }

    sealed class AvatarViewModel(
        open val name: String,
        @DrawableRes open val image: Int,
        open val gemPrice: Int
    ) {

        data class Current(
            override val name: String,
            @DrawableRes override val image: Int,
            override val gemPrice: Int
        ) : AvatarViewModel(name, image, gemPrice)

        data class Bought(
            val avatar: Avatar,
            override val name: String,
            @DrawableRes override val image: Int,
            override val gemPrice: Int
        ) : AvatarViewModel(name, image, gemPrice)

        data class ForSale(
            val avatar: Avatar,
            override val name: String,
            @DrawableRes override val image: Int,
            override val gemPrice: Int
        ) : AvatarViewModel(name, image, gemPrice)
    }

    inner class AvatarAdapter : BasePagerAdapter<AvatarViewModel>() {

        override fun layoutResourceFor(item: AvatarViewModel) =
            R.layout.item_store_avatar

        override fun bindItem(item: AvatarViewModel, view: View) {

            view.name.text = item.name
            view.image.setImageResource(item.image)
            view.price.text = item.gemPrice.toString()

            when (item) {

                is AvatarViewModel.Current ->
                    view.buy.gone()

                is AvatarViewModel.Bought -> {
                    view.currentAvatar.gone()
                    view.buy.setText(R.string.pick_me)
                    view.buy.dispatchOnClick(AvatarStoreAction.Change(item.avatar))
                }

                is AvatarViewModel.ForSale -> {
                    view.currentAvatar.gone()
                    view.buy.dispatchOnClick(AvatarStoreAction.Buy(item.avatar))
                }

            }
        }
    }

    private val AvatarStoreViewState.Changed.avatarViewModels
        get() = avatars.map {
            val avatar = it.avatar
            val androidAvatar = AndroidAvatar.valueOf(avatar.name)
            when (it) {
                is AvatarItem.Current ->
                    AvatarViewModel.Current(
                        name = stringRes(androidAvatar.avatarName),
                        image = androidAvatar.image,
                        gemPrice = avatar.gemPrice
                    )

                is AvatarItem.Bought ->
                    AvatarViewModel.Bought(
                        avatar = avatar,
                        name = stringRes(androidAvatar.avatarName),
                        image = androidAvatar.image,
                        gemPrice = avatar.gemPrice
                    )

                is AvatarItem.ForSale ->
                    AvatarViewModel.ForSale(
                        avatar = avatar,
                        name = stringRes(androidAvatar.avatarName),
                        image = androidAvatar.image,
                        gemPrice = avatar.gemPrice
                    )
            }
        }
}