package mypoli.android.pet

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.controller_pet.view.*
import kotlinx.android.synthetic.main.item_pet_food.view.*
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

        view.fab.setImageDrawable(
            IconicsDrawable(view.context)
                .icon(Ionicons.Icon.ion_pizza)
                .colorRes(R.color.md_white)
                .sizeDp(24)
        )

        view.foodList.layoutManager = LinearLayoutManager(activity!!, LinearLayoutManager.HORIZONTAL, false)
        view.foodList.post {
            view.foodList.x = view.width.toFloat()
        }

        inventoryToolbar = addToolbarView(R.layout.view_inventory_toolbar) as ViewGroup
        inventoryToolbar.playerGems.setOnClickListener {
            send(ShowCurrencyConverter)
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

    override fun render(state: PetViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                view.foodList.adapter = PetFoodAdapter(state.foodViewModels)
                view.fab.setOnClickListener {
                    send(ShowFoodListIntent)
                }
                renderPet(state, view)

                if (!state.isDead) {
                    playEnterAnimation(view)
                }
                inventoryToolbar.playerGems.text = state.playerGems.toString()
            }
            FOOD_LIST_SHOWN -> {
                playShowFoodListAnimation(view)
                view.fab.setImageResource(R.drawable.ic_close_white_24dp)
                view.fab.setOnClickListener {
                    send(HideFoodListIntent)
                }
            }

            FOOD_LIST_HIDDEN -> {
                playHideFoodListAnimation(view)
                view.fab.setImageDrawable(
                    IconicsDrawable(view.context)
                        .icon(Ionicons.Icon.ion_pizza)
                        .colorRes(R.color.md_white)
                        .sizeDp(24)
                )
                view.fab.setOnClickListener {
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
                Toast.makeText(view.context, "Food too expensive", Toast.LENGTH_SHORT).show()
            }

            PET_CHANGED -> {

                inventoryToolbar.playerGems.text = state.playerGems.toString()

                (view.foodList.adapter as PetFoodAdapter).updateAll(state.foodViewModels)

                renderPet(state, view)
            }

            RENAME_PET ->
                TextPickerDialogController({ text ->
                    send(RenamePetIntent(text))
                }, "Give me a name", state.petName, hint = "Rename your pet")
                    .showDialog(router, "text-picker-tag")

            PET_RENAMED ->
                renderPetName(state.petName)

            REVIVE_TOO_EXPENSIVE -> {
                Toast.makeText(view.context, "Revive too expensive", Toast.LENGTH_SHORT).show()
            }

            PET_REVIVED -> {
                view.fab.visible = true
                view.foodList.visible = true
            }

            SHOW_CURRENCY_CONVERTER -> {
                CurrencyConverterController().showDialog(router, "currency-converter")
            }
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

            view.fab.visible = false
            view.foodList.visible = false

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

    private fun playHideFoodListAnimation(view: View) {
        val foodListAnim = AnimatorSet()
        foodListAnim.playTogether(
            ObjectAnimator.ofFloat(view.foodList, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(view.foodList, "x", 0f, view.width.toFloat())
        )
        val animator = AnimatorSet()
        animator.playSequentially(
            foodListAnim,
            ObjectAnimator.ofFloat(view.fab, "y", view.fab.y, view.foodList.y + ViewUtils.dpToPx(8f, view.context))
        )
        animator.duration = intRes(android.R.integer.config_shortAnimTime).toLong()
        animator.start()
    }

    private fun playShowFoodListAnimation(view: View) {
        val foodListAnim = AnimatorSet()
        foodListAnim.playTogether(
            ObjectAnimator.ofFloat(view.foodList, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(view.foodList, "x", view.width.toFloat(), 0f)
        )
        val animator = AnimatorSet()
        animator.playSequentially(
            ObjectAnimator.ofFloat(view.fab, "y", view.fab.y, view.foodList.y - view.fab.height - ViewUtils.dpToPx(8f, view.context)),
            foodListAnim
        )
        animator.duration = intRes(android.R.integer.config_shortAnimTime).toLong()
        animator.start()
    }

    override fun onDestroyView(view: View) {
        removeToolbarView(inventoryToolbar)
        super.onDestroyView(view)
    }

    data class PetFoodViewModel(@DrawableRes val image: Int, val price: Food.Price, val food: Food, val quantity: Int = 0)

    inner class PetFoodAdapter(private var foodItems: List<PetFoodViewModel>) : RecyclerView.Adapter<PetFoodAdapter.ViewHolder>() {
        override fun getItemCount() = foodItems.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = foodItems[position]
            holder.itemView.foodImage.setImageResource(vm.image)

            val foodPrice = holder.itemView.foodPrice
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(foodPrice, 10, 14, 1, TypedValue.COMPLEX_UNIT_SP)
            if (vm.quantity > 0) {
                foodPrice.text = "x" + vm.quantity.toString()
                foodPrice.setCompoundDrawables(null, null, null, null)
            } else {
                foodPrice.text = "${vm.price.gems} = x${vm.price.quantity}"
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

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}