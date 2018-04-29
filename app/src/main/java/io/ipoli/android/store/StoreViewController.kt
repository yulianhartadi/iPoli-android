package io.ipoli.android.store

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler
import io.ipoli.android.R
import io.ipoli.android.common.view.*
import io.ipoli.android.pet.store.PetStoreViewController
import io.ipoli.android.store.avatar.AvatarStoreViewController
import io.ipoli.android.store.gem.GemStoreViewController
import io.ipoli.android.store.membership.MembershipViewController
import io.ipoli.android.store.powerup.PowerUpStoreViewController
import io.ipoli.android.store.theme.ThemeStoreViewController
import kotlinx.android.synthetic.main.item_store.view.*
import kotlinx.android.synthetic.main.view_default_toolbar.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/23/18.
 */

class StoreViewController(args: Bundle? = null) : RestoreViewOnCreateController(args) {

    private val fadeChangeHandler = FadeChangeHandler()

    private var itemHeight: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_store, container, false)

        view.post {
            itemHeight = view.height * 6 / 7 / VISIBLE_ITEMS_PER_SCREEN
            renderAll(view)
        }
        setToolbar(view.toolbar)
        return view
    }

    private fun renderAll(view: View) {
        StoreItem.values().forEach {
            renderItem(
                view = view.findViewById(it.id),
                color = it.color,
                icon = it.icon,
                title = it.title,
                open = open(it)
            )
        }
    }

    private fun open(item: StoreItem): () -> Unit {
        when (item) {
            StoreItem.MEMBERSHIP -> return { showController(MembershipViewController()) }
            StoreItem.POWER_UPS -> return { showController(PowerUpStoreViewController()) }
            StoreItem.AVATARS -> return { showController(AvatarStoreViewController()) }
            StoreItem.GEMS -> return { showController(GemStoreViewController()) }
            StoreItem.PETS -> return { showController(PetStoreViewController()) }
            StoreItem.THEMES -> return { showController(ThemeStoreViewController()) }
            StoreItem.COLORS -> return {
                ColorPickerDialogController({
                }).show(
                    router,
                    "pick_color_tag"
                )
            }
            StoreItem.ICONS -> return {
                IconPickerDialogController({
                }).showDialog(
                    router,
                    "pick_icon_tag"
                )
            }
        }
    }

    override fun onAttach(view: View) {
        colorLayoutBars()
        super.onAttach(view)
        toolbarTitle = stringRes(R.string.drawer_store)
        showBackButton()
    }

    private fun colorLayoutBars() {
        activity?.window?.navigationBarColor = attrData(io.ipoli.android.R.attr.colorPrimary)
        activity?.window?.statusBarColor = attrData(io.ipoli.android.R.attr.colorPrimaryDark)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            return router.handleBack()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun renderItem(
        view: View,
        @ColorRes color: Int,
        @DrawableRes icon: Int,
        @StringRes title: Int,
        open: () -> Unit
    ) {
        val colorRes = colorRes(color)
        view.layoutParams.height = itemHeight
        view.post {
            val width = view.width * 4 / 3
            view.storeItemBackground.layoutParams.width = width
            view.storeItemBackground.post {
                val xRadius = width / 2f
                val yRadius = view.storeItemBackground.height / 2f
                view.storeItemBackground.background =
                    createLeftRoundedDrawable(xRadius, yRadius, colorRes)
            }
        }
        view.storeItemIcon.setImageResource(icon)
        view.storeItemIcon.drawable.setTint(colorRes)
        view.storeItemTitle.text = stringRes(title)

        view.setOnClickListener {
            open()
        }
    }

    private fun createLeftRoundedDrawable(xRadius: Float, yRadius: Float, color: Int): Drawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.RECTANGLE
        d.cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, xRadius, yRadius, xRadius, yRadius)
        d.color = ColorStateList.valueOf(color)
        return d
    }

    private fun showController(controller: Controller) {
        rootRouter.pushController(
            RouterTransaction.with(controller)
                .pushChangeHandler(fadeChangeHandler)
                .popChangeHandler(fadeChangeHandler)
        )
    }

    enum class StoreItem(
        val id: Int,
        @ColorRes val color: Int,
        @DrawableRes val icon: Int,
        @StringRes val title: Int
    ) {
        MEMBERSHIP(
            id = R.id.storeMembership,
            color = R.color.md_blue_600,
            icon = R.drawable.ic_card_membership_black_24px,
            title = R.string.membership
        ),
        POWER_UPS(
            id = R.id.storePowerUps,
            color = R.color.md_orange_600,
            icon = R.drawable.ic_rocket_black_24dp,
            title = R.string.power_ups
        ),
        AVATARS(
            id = R.id.storeAvatars,
            color = R.color.md_green_600,
            icon = R.drawable.ic_ninja_black_24dp,
            title = R.string.avatars
        ),
        PETS(
            id = R.id.storePets,
            color = R.color.md_purple_400,
            icon = R.drawable.ic_pets_white_24dp,
            title = R.string.pets
        ),
        GEMS(
            id = R.id.storeGems,
            color = R.color.md_blue_grey_400,
            icon = R.drawable.ic_diamond_black_24dp,
            title = R.string.gems
        ),
        THEMES(
            id = R.id.storeThemes,
            color = R.color.md_purple_800,
            icon = R.drawable.ic_theme_black_24dp,
            title = R.string.themes
        ),
        COLORS(
            id = R.id.storeColors,
            color = R.color.md_pink_400,
            icon = R.drawable.ic_color_palette_white_24dp,
            title = R.string.colors
        ),
        ICONS(
            id = R.id.storeIcons,
            color = R.color.md_red_400,
            icon = R.drawable.ic_icon_white_24dp,
            title = R.string.icons
        )
    }

    companion object {

        const val VISIBLE_ITEMS_PER_SCREEN = 3

        fun routerTransaction() =
            RouterTransaction.with(StoreViewController())
                .pushChangeHandler(VerticalChangeHandler())
                .popChangeHandler(VerticalChangeHandler())
    }

}