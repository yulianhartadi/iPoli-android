package mypoli.android.pet

import android.animation.*
import android.app.WallpaperManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.controller_pet.view.*
import kotlinx.android.synthetic.main.item_pet_food.view.*
import kotlinx.android.synthetic.main.item_pet_item.view.*
import kotlinx.android.synthetic.main.view_inventory_toolbar.view.*
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.view.*
import mypoli.android.pet.PetViewState.StateType.*
import mypoli.android.pet.store.PetStoreViewController
import space.traversal.kapsule.required


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/24/17.
 */
class PetViewController(args: Bundle? = null) : MviViewController<PetViewState, PetViewController, PetPresenter, PetIntent>(args) {

    private val presenter by required { petPresenter }

    override fun createPresenter() = presenter

    private lateinit var inventoryToolbar: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_pet, container, false)
        setHasOptionsMenu(true)

        view.fabFood.setImageDrawable(
            IconicsDrawable(view.context)
                .icon(Ionicons.Icon.ion_pizza)
                .colorRes(R.color.md_white)
                .sizeDp(24)
        )

        view.itemList.layoutManager = LinearLayoutManager(activity!!, LinearLayoutManager.HORIZONTAL, false)
        view.itemList.post {
            view.itemList.x = view.width.toFloat()
        }

        inventoryToolbar = addToolbarView(R.layout.view_inventory_toolbar) as ViewGroup
        inventoryToolbar.playerGems.setOnClickListener {
            send(PetIntent.ShowCurrencyConverter)
        }

        return view
    }

    override fun onAttach(view: View) {
        showBackButton()
        super.onAttach(view)
        send(LoadDataIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.pet_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            router.popCurrentController()
            return true
        }

        if (item.itemId == R.id.actionStore) {
            val handler = FadeChangeHandler()
            router.pushController(
                RouterTransaction.with(PetStoreViewController())
                    .pushChangeHandler(handler)
                    .popChangeHandler(handler)
            )
            return true
        }

        if (item.itemId == R.id.actionRenamePet) {
            send(RenamePetRequestIntent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        val PET_TOP_BORDER_PERCENT = 0.33f
        val PET_BOTTOM_BORDER_PERCENT = 0.74f
    }

    override fun render(state: PetViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                view.itemList.adapter = PetFoodAdapter(state.foodViewModels)
                view.fabFood.setOnClickListener {
                    send(ShowFoodListIntent)
                }
                view.fabItems.setOnClickListener {
                    send(PetIntent.ShowItemList)
                }
                renderPet(state, view)

                view.post {
                    resizePet(view)
                    view.post {
                        if (!state.isDead) {
                            playEnterAnimation(view)
                        }
                    }
                }

                inventoryToolbar.playerGems.text = state.playerGems.toString()
            }
            FOOD_LIST_SHOWN -> {
                view.itemList.adapter = PetFoodAdapter(state.foodViewModels)
                view.fabItems.isClickable = false
                playShowItemsAnimation(view, view.fabFood, view.fabItems)

                view.fabFood.setImageResource(R.drawable.ic_close_white_24dp)
                view.fabFood.setOnClickListener {
                    send(HideFoodListIntent)
                }
            }

            FOOD_LIST_HIDDEN -> {
                view.fabItems.isClickable = true

                val heightOffset = view.fabFood.height + ViewUtils.dpToPx(16f, view.context)

                playHideFoodListAnimation(view, view.fabFood, view.fabItems, heightOffset)
                view.fabFood.setImageDrawable(
                    IconicsDrawable(view.context)
                        .icon(Ionicons.Icon.ion_pizza)
                        .colorRes(R.color.md_white)
                        .sizeDp(24)
                )
                view.fabFood.setOnClickListener {
                    send(ShowFoodListIntent)
                }
            }

            PET_FED -> {
                val responseRes = if (state.wasFoodTasty)
                    R.string.pet_tasty_food_response
                else
                    R.string.pet_not_tasty_food_response
                view.petResponse.setText(responseRes)
                val avatar = AndroidPetAvatar.valueOf(state.avatar!!.name)
                if (state.isDead) {
                    view.petState.setImageResource(avatar.deadStateImage)
                } else {
                    view.petState.setImageResource(avatar.moodImage[state.mood]!!)
                }
                playFeedPetAnimation(view, state)
            }

            FOOD_TOO_EXPENSIVE -> {
                CurrencyConverterController().showDialog(router, "currency-converter")
                Toast.makeText(view.context, stringRes(R.string.food_too_expensive), Toast.LENGTH_SHORT).show()
            }

            PET_CHANGED -> {

                inventoryToolbar.playerGems.text = state.playerGems.toString()

                (view.itemList.adapter as PetFoodAdapter).updateAll(state.foodViewModels)

                renderPet(state, view)
            }

            RENAME_PET ->
                TextPickerDialogController({ text ->
                    send(RenamePetIntent(text))
                }, stringRes(R.string.dialog_rename_pet_title), state.petName, hint = stringRes(R.string.dialog_rename_pet_hint))
                    .showDialog(router, "text-picker-tag")

            PET_RENAMED ->
                renderPetName(state.petName)

            REVIVE_TOO_EXPENSIVE -> {
                Toast.makeText(view.context, "Revive too expensive", Toast.LENGTH_SHORT).show()
            }

            PET_REVIVED -> {
                view.fabFood.visible = true
                view.itemList.visible = true
            }

            SHOW_CURRENCY_CONVERTER -> {
                CurrencyConverterController().showDialog(router, "currency-converter")
            }

            ITEM_LIST_SHOWN -> {
                view.itemList.adapter = PetItemAdapter(state.itemViewModels)
                view.fabFood.isClickable = false
                playShowItemsAnimation(view, view.fabItems, view.fabFood)
                view.fabItems.setImageResource(R.drawable.ic_close_white_24dp)
                view.fabItems.setOnClickListener {
                    send(PetIntent.HideItemList)
                }

                listOf(view.fabHeadItems, view.fabFaceItems, view.fabBodyItems)
                    .forEach {
                        it.visible = true
                        it.alpha = 0f
                    }

                val fabBodyAnim = createPopupAnimator(view.fabBodyItems)
                val fabFaceAnim = createPopupAnimator(view.fabFaceItems)
                val fabHeadAnim = createPopupAnimator(view.fabHeadItems)

                val animatorSet = AnimatorSet()
                animatorSet.playSequentially(fabBodyAnim, fabFaceAnim, fabHeadAnim)
                animatorSet.start()

                view.fabBodyItems.backgroundTintList = ColorStateList.valueOf(attr(R.attr.colorPrimaryDark))
            }

            ITEM_LIST_HIDDEN -> {
                ViewUtils.goneViews(view.fabHeadItems, view.fabFaceItems, view.fabBodyItems)
                view.fabFood.isClickable = true
                val heightOffset = (view.fabItems.height + ViewUtils.dpToPx(16f, view.context)) * 2
                playHideFoodListAnimation(view, view.fabItems, view.fabFood, heightOffset)
                view.fabItems.setImageResource(R.drawable.ic_sword_white_24dp)
                view.fabItems.setOnClickListener {
                    send(PetIntent.ShowItemList)
                }
            }
        }
    }

    private fun createPopupAnimator(view: View): Animator {
        val animator = AnimatorInflater.loadAnimator(view.context, R.animator.popup)
        animator.setTarget(view)
        animator.interpolator = OvershootInterpolator()
        animator.duration = shortAnimTime
        return animator
    }

    private fun setPetAsWallpaper(view: View) {
        val b = loadBitmapFromView(view)

        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        val desiredWidth = wallpaperManager.desiredMinimumWidth
        val desiredHeight = wallpaperManager.desiredMinimumHeight

        val originalWidth = b.width
        val originalHeight = b.height

        val hScaleFactor = originalWidth.toFloat() / desiredWidth.toFloat()
        val vScaleFactor = originalHeight.toFloat() / desiredHeight.toFloat()

        val scaleFactor = Math.max(hScaleFactor, vScaleFactor)

        val wallWidth = originalWidth / scaleFactor
        val wallHeight = originalHeight / scaleFactor

        val wallpaperBmp = Bitmap.createScaledBitmap(
            b,
            wallWidth.toInt(),
            wallHeight.toInt(),
            true
        )

        b.recycle()

        wallpaperManager.setBitmap(wallpaperBmp)

        wallpaperBmp.recycle()
    }

    private fun resizePet(view: View) {
        val petView = view.pet
        val hatView = view.hat

        val viewHeight = view.height
        val bottomY = PET_BOTTOM_BORDER_PERCENT * viewHeight
        val topY = PET_TOP_BORDER_PERCENT * viewHeight

        val newPetHeight = bottomY - topY
        val originalPetHeight = petView.height
        val scale = newPetHeight / originalPetHeight

        if (scale < 1) {
            val lp = petView.layoutParams as ConstraintLayout.LayoutParams
            lp.height = newPetHeight.toInt()
            lp.width = (petView.width * scale).toInt()
            lp.verticalBias = PET_BOTTOM_BORDER_PERCENT
            petView.layoutParams = lp

            val originalHatHeight = hatView.height
            val newHatHeight = originalHatHeight * scale

            val hlp = hatView.layoutParams as ConstraintLayout.LayoutParams
            hlp.height = newHatHeight.toInt()
            hlp.width = (hatView.width * scale).toInt()
            hatView.layoutParams = hlp
        } else {
            val lp = petView.layoutParams as ConstraintLayout.LayoutParams
            lp.verticalBias = PET_BOTTOM_BORDER_PERCENT
            petView.layoutParams = lp
        }
    }

    private fun playEnterAnimation(view: View) {
        val anims = listOf<ImageView>(
            view.pet,
            view.petState,
            view.body,
            view.face,
            view.hat)
            .map {
                ObjectAnimator.ofFloat(it, "y",
                    it.y, it.y - ViewUtils.dpToPx(30f, view.context), it.y, it.y - ViewUtils.dpToPx(24f, view.context), it.y)
            }

        val set = AnimatorSet()
        set.duration = intRes(android.R.integer.config_longAnimTime).toLong() + 100
        set.playTogether(anims)
        set.interpolator = AccelerateDecelerateInterpolator()
        set.start()
    }

    private fun renderPet(state: PetViewState, view: View) {
        renderPetName(state.petName)

        val avatar = AndroidPetAvatar.valueOf(state.avatar!!.name)

        view.pet.setImageResource(avatar.image)

        if (state.isDead) {
            view.reviveContainer.visibility = View.VISIBLE
            view.statsContainer.visibility = View.GONE
            view.petState.setImageResource(avatar.deadStateImage)

            view.reviveHint.text = stringRes(R.string.revive_hint, state.petName)
            view.reviveCost.text = state.reviveCost.toString()

            view.fabFood.visible = false
            view.itemList.visible = false

            view.revive.setOnClickListener {
                send(RevivePetIntent)
            }
        } else {
            view.statsContainer.visibility = View.VISIBLE
            view.reviveContainer.visibility = View.GONE
            playProgressAnimation(view.healthProgress, view.healthProgress.progress, state.hp)
            view.healthPoints.text = state.hp.toString() + "/" + state.maxHP
            view.healthProgress.max = state.maxHP

            playProgressAnimation(view.moodProgress, view.moodProgress.progress, state.mp)
            view.moodPoints.text = state.mp.toString() + "/" + state.maxMP
            view.moodProgress.max = state.maxMP

            view.coinBonus.text = "+ %.2f".format(state.coinsBonus) + "%"
            view.xpBonus.text = "+ %.2f".format(state.xpBonus) + "%"
            view.unlockChanceBonus.text = "+ %.2f".format(state.unlockChanceBonus) + "%"

            view.petState.setImageResource(avatar.moodImage[state.mood]!!)
        }

        view.stateName.text = state.stateName
    }

    private fun renderPetName(name: String) {
        inventoryToolbar.toolbarTitle.text = name
    }

    private fun playFeedPetAnimation(view: View, state: PetViewState) {
        val selectedFood = view.selectedFood
        val duration = 300L
        val slideAnim = ObjectAnimator.ofFloat(selectedFood, "x", 0f,
            (view.width / 2 - selectedFood.width).toFloat())
        val fadeAnim = ObjectAnimator.ofFloat(selectedFood, "alpha", 1f, 0f)
        fadeAnim.startDelay = duration / 2

        val anim = AnimatorSet()
        anim.playTogether(slideAnim, fadeAnim)
        anim.duration = duration
        anim.interpolator = AccelerateDecelerateInterpolator()

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                selectedFood.setImageResource(state.food!!.image)
                selectedFood.alpha = 1f
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (state.isDead) {
                    return
                }
                val avatar = AndroidPetAvatar.valueOf(state.avatar!!.name)
                val stateImage = avatar.moodImage[state.mood]!!
                val responseStateImage = if (state.wasFoodTasty)
                    avatar.moodImage[PetMood.AWESOME]!!
                else
                    avatar.deadStateImage
                playFeedPetResponseAnimation(view, stateImage, responseStateImage)
            }
        })
        anim.start()
    }

    private fun playProgressAnimation(view: ProgressBar, from: Int, to: Int) {
        val animator = ObjectAnimator.ofInt(view, "progress", from, to)
        animator.duration = intRes(android.R.integer.config_shortAnimTime).toLong()
        animator.start()
    }

    private fun playFeedPetResponseAnimation(view: View, @DrawableRes currentStateImage: Int, @DrawableRes responseStateImage: Int) {
        val anim = AnimatorSet()
        anim.playSequentially(
            createShowPetResponseAnimation(view, responseStateImage),
            createHidePetResponseAnimation(view, currentStateImage),
            createShowPetResponseAnimation(view, responseStateImage),
            createHidePetResponseAnimation(view, currentStateImage)
        )
        anim.start()
    }

    private fun createHidePetResponseAnimation(view: View, @DrawableRes petStateImage: Int): Animator {
        val anim = ObjectAnimator.ofFloat(view.petState, "alpha", 0f, 1f)
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                view.petState.setImageResource(petStateImage)
                view.petResponse.alpha = 0f
            }
        })
        anim.duration = 100
        anim.startDelay = 300
        return anim
    }

    private fun createShowPetResponseAnimation(view: View, @DrawableRes petAwesomeStateImage: Int): Animator {
        val anim = ObjectAnimator.ofFloat(view.petState, "alpha", 1f, 0f)
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                view.petState.setImageResource(petAwesomeStateImage)
                view.petState.alpha = 1f
                view.petResponse.alpha = 1f
            }
        })
        anim.duration = 100
        anim.startDelay = 300
        return anim
    }

    private fun playHideFoodListAnimation(view: View, moveFAB: FloatingActionButton, showFAB: FloatingActionButton, heightOffset: Float) {
        val foodListAnim = AnimatorSet()
        foodListAnim.playTogether(
            ObjectAnimator.ofFloat(view.itemList, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(view.itemList, "x", 0f, view.width.toFloat())
        )
        val animator = AnimatorSet()

        animator.playSequentially(
            foodListAnim,
            ObjectAnimator.ofFloat(moveFAB, "y", moveFAB.y, view.height - heightOffset),
            ObjectAnimator.ofFloat(showFAB, "alpha", 0f, 1f)
        )
        animator.duration = intRes(android.R.integer.config_shortAnimTime).toLong()
        animator.start()
    }

    private fun playShowItemsAnimation(view: View, moveView: View, hideView: View) {
        val foodListAnim = AnimatorSet()
        foodListAnim.playTogether(
            ObjectAnimator.ofFloat(view.itemList, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(view.itemList, "x", view.width.toFloat(), 0f)
        )
        val animator = AnimatorSet()
        animator.playSequentially(
            ObjectAnimator.ofFloat(hideView, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(moveView, "y", moveView.y, view.itemList.y - moveView.height - ViewUtils.dpToPx(8f, view.context)),
            foodListAnim
        )
        animator.duration = intRes(android.R.integer.config_shortAnimTime).toLong()
        animator.start()
    }

    override fun onDestroyView(view: View) {
        removeToolbarView(inventoryToolbar)
        super.onDestroyView(view)
    }

    fun loadBitmapFromView(v: View): Bitmap {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.layout(v.left, v.top, v.right, v.bottom)
        v.draw(c)
        return b
    }

    data class PetFoodViewModel(@DrawableRes val image: Int, val price: Food.Price, val food: Food, val quantity: Int = 0)

    inner class PetFoodAdapter(private var foodItems: List<PetFoodViewModel>) : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount() = foodItems.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = foodItems[position]
            holder.itemView.foodImage.setImageResource(vm.image)

            val foodPrice = holder.itemView.foodPrice
            if (vm.quantity > 0) {
                foodPrice.text = "x" + vm.quantity.toString()
                foodPrice.setCompoundDrawables(null, null, null, null)
            } else {
                foodPrice.text = "= x${vm.price.quantity}"
                foodPrice.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_gem_16dp),
                    null, null, null)
            }
            holder.itemView.setOnClickListener {
                send(FeedIntent(vm.food))
            }
        }

        fun updateAll(foodItems: List<PetFoodViewModel>) {
            this.foodItems = foodItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pet_food, parent, false))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    data class PetItemViewModel(@DrawableRes val image: Int, val gemPrice: Int?, val item: PetItem)

    inner class PetItemAdapter(private var petItems: List<PetItemViewModel>) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pet_item, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = petItems[position]
            holder.itemView.itemImage.setImageResource(vm.image)

            vm.gemPrice?.let {
                holder.itemView.itemPrice.text = it.toString()
            }
        }

        override fun getItemCount() = petItems.size


    }
}

