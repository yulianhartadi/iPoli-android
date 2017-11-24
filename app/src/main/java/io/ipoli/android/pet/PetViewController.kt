package io.ipoli.android.pet

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.ionicons_typeface_library.Ionicons
import io.ipoli.android.R
import io.ipoli.android.common.mvi.MviViewController
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
        view.fab.setImageDrawable(
            IconicsDrawable(view.context)
                .icon(Ionicons.Icon.ion_pizza)
                .colorRes(R.color.md_white)
                .sizeDp(24)
        )

        view.foodList.layoutManager = LinearLayoutManager(activity!!, LinearLayoutManager.HORIZONTAL, false)
        view.foodList.adapter = PetFoodAdapter(
            Food.values().map {
                PetFoodViewModel(it.image, it.price)
            }
        )
        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(LoadDataIntent)
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
                val anim = AnimatorSet()
                anim.playSequentially(
                    createA(view),
                    createB(view),
                    createA(view),
                    createB(view),
                    createA(view),
                    createB(view)
                )
                anim.start()
            }
        }
    }

    private fun createB(view: View): Animator {
        val b = ObjectAnimator.ofFloat(view.petState, "alpha", 0f, 1f)
        val c = ObjectAnimator.ofFloat(view.petResponse, "alpha", 0f, 1f)
        b.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                view.petState.setImageResource(R.drawable.pet_8_happy)
            }
        })
//        b.duration = 50
        val set = AnimatorSet()
        set.playTogether(b, c)

        return set
    }

    private fun createA(view: View): Animator {
        val a = ObjectAnimator.ofFloat(view.petState, "alpha", 1f, 0f)
        val c = ObjectAnimator.ofFloat(view.petResponse, "alpha", 1f, 0f)
        a.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                view.petState.setImageResource(R.drawable.pet_8_awesome)
                view.petResponse.alpha = 0f
            }
        })
//        a.duration = 50
        val set = AnimatorSet()
        set.playTogether(a, c)

        return set
    }

    private fun playHideFoodListAnimation(view: View) {
        val anim = AnimatorSet()
        anim.playTogether(
            ObjectAnimator.ofFloat(view.foodList, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(view.foodList, "x", 0f, view.width.toFloat())
        )
        anim.duration = intRes(android.R.integer.config_mediumAnimTime).toLong()
        anim.start()
    }

    private fun playShowFoodListAnimation(view: View) {
        val anim = AnimatorSet()
        anim.playTogether(
            ObjectAnimator.ofFloat(view.foodList, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(view.foodList, "x", view.width.toFloat(), 0f)
        )
        anim.duration = intRes(android.R.integer.config_mediumAnimTime).toLong()
        anim.start()
    }

    data class PetFoodViewModel(@DrawableRes val image: Int, val price: Int)

    inner class PetFoodAdapter(private val foodItems: List<PetFoodViewModel>) : RecyclerView.Adapter<PetFoodAdapter.ViewHolder>() {
        override fun getItemCount() = foodItems.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = foodItems[position]
            holder.itemView.foodImage.setImageResource(vm.image)
            if (position == itemCount - 1) {
                holder.itemView.foodPrice.text = ""
            } else {
                holder.itemView.foodPrice.text = vm.price.toString() + " coins"
                holder.itemView.setOnClickListener {
                    send(Feed)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pet_food, parent, false))

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

}