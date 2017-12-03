package io.ipoli.android.pet

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.Toast
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.ionicons_typeface_library.Ionicons
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.mvi.MviViewController
import io.ipoli.android.common.view.TextPickerDialogController
import io.ipoli.android.common.view.intRes
import io.ipoli.android.pet.PetViewState.StateType.*
import kotlinx.android.synthetic.main.controller_pet.view.*
import kotlinx.android.synthetic.main.item_pet_food.view.*
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/24/17.
 */
class PetViewController(args: Bundle? = null) : MviViewController<PetViewState, PetViewController, PetPresenter, PetIntent>(args) {

    private val presenter by required { petPresenter }

    override fun createPresenter() = presenter

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
//        view.foodList.adapter = PetFoodAdapter(
//            Food.values().map {
//                PetFoodViewModel(it.image, it.price, it)
//            }
//        )
        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(LoadDataIntent)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_pet).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.pet_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.actionStore) {
            return true
        }

        if (item.itemId == R.id.actionRenamePet) {
            send(RenamePetRequest)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun render(state: PetViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                view.fab.setOnClickListener {
                    send(ShowFoodList)
                }
            }
            FOOD_LIST_SHOWN -> {
                playShowFoodListAnimation(view)
                view.fab.setImageResource(R.drawable.ic_close_white_24dp)
                view.fab.setOnClickListener {
                    send(HideFoodList)
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
                    send(ShowFoodList)
                }
            }

            PET_FED -> {
                val responseRes = if (state.wasFoodTasty)
                    R.string.pet_tasty_food_response
                else
                    R.string.pet_not_tasty_food_response
                view.petResponse.setText(responseRes)
                playFeedPetAnimation(view, state)
            }

            FOOD_TOO_EXPENSIVE -> {
                Toast.makeText(view.context, "Food too expensive", Toast.LENGTH_SHORT).show()
            }

            PET_CHANGED -> {
                view.foodList.adapter = PetFoodAdapter(state.foodViewModels)

                displayPetName(state.petName)

                playProgressAnimation(view.healthProgress, view.healthProgress.progress, state.hp)
                view.healthPoints.text = state.hp.toString() + "/" + state.maxHP
                view.healthProgress.max = state.maxHP

                playProgressAnimation(view.moodProgress, view.moodProgress.progress, state.mp)
                view.moodPoints.text = state.mp.toString() + "/" + state.maxMP
                view.moodProgress.max = state.maxMP

                view.coinBonus.text = "+ %.2f".format(state.coinsBonus) + "%"
                view.xpBonus.text = "+ %.2f".format(state.xpBonus) + "%"
                view.unlockChanceBonus.text = "+ %.2f".format(state.unlockChanceBonus) + "%"

                val avatar = AndroidPetAvatar.valueOf(state.avatar!!.name)

                view.pet.setImageResource(avatar.image)
                view.petState.setImageResource(avatar.moodImage[state.mood]!!)
                view.stateName.text = state.stateName
            }

            RENAME_PET ->
                TextPickerDialogController({ text ->
                    send(RenamePet(text))
                }, "Give me a name", state.petName, hint = "Rename your pet")
                    .showDialog(router, "text-picker-tag")

            PET_RENAMED ->
                displayPetName(state.petName)

        }
    }

    private fun displayPetName(name: String) {
        val toolbar = activity!!.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = name
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
        val avatar = AndroidPetAvatar.valueOf(state.avatar!!.name)
        val stateImage = avatar.moodImage[state.mood]!!
        val responseStateImage = if (state.wasFoodTasty)
            avatar.moodImage[PetMood.AWESOME]!!
        else
            avatar.deadStateImage
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                selectedFood.setImageResource(state.food!!.image)
                selectedFood.alpha = 1f
            }

            override fun onAnimationEnd(animation: Animator?) {
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

    data class PetFoodViewModel(@DrawableRes val image: Int, val price: Int, val food: Food, val quantity: Int = 0)

    inner class PetFoodAdapter(private val foodItems: List<PetFoodViewModel>) : RecyclerView.Adapter<PetFoodAdapter.ViewHolder>() {
        override fun getItemCount() = foodItems.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = foodItems[position]
            holder.itemView.foodImage.setImageResource(vm.image)

            val foodPrice = holder.itemView.foodPrice
            if(vm.quantity > 0) {
                foodPrice.text = vm.quantity.toString()
                foodPrice.setCompoundDrawables(null, null, null, null)
            } else {
                foodPrice.text = vm.price.toString()
                foodPrice.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_life_coin_16dp),
                    null, null, null)
            }
            holder.itemView.setOnClickListener {
                send(Feed(vm.food))
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pet_food, parent, false))

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

}