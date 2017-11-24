package io.ipoli.android.pet

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
        view.foodList.adapter = PetFoodAdapter(listOf(
            PetFoodViewModel(R.drawable.food_beer_1, 12),
            PetFoodViewModel(R.drawable.food_candy_1, 32),
            PetFoodViewModel(R.drawable.food_candy_2, 32),
            PetFoodViewModel(R.drawable.food_candy_3, 32),
            PetFoodViewModel(R.drawable.food_candy_4, 32),
            PetFoodViewModel(R.drawable.food_junk_1, 23),
            PetFoodViewModel(R.drawable.food_junk_2, 23),
            PetFoodViewModel(R.drawable.food_junk_3, 23),
            PetFoodViewModel(R.drawable.food_junk_4, 23),
            PetFoodViewModel(R.drawable.food_poop_1, 12),
            PetFoodViewModel(R.drawable.food_veggie_1, 12),
            PetFoodViewModel(R.drawable.food_veggie_2, 12),
            PetFoodViewModel(R.drawable.food_veggie_3, 12),
            PetFoodViewModel(R.drawable.food_fruit_1, 12),
            PetFoodViewModel(R.drawable.food_fruit_2, 12),
            PetFoodViewModel(R.drawable.food_fruit_3, 12),
            PetFoodViewModel(R.drawable.food_fruit_4, 12),
            PetFoodViewModel(R.drawable.food_meat_1, 12),
            PetFoodViewModel(R.drawable.food_meat_2, 12),
            PetFoodViewModel(R.drawable.food_meat_3, 12),
            PetFoodViewModel(R.drawable.food_meat_4, 12),
            PetFoodViewModel(R.drawable.food_empty, -1)

        ))

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
                val anim = AnimatorSet()
                anim.playTogether(
                    ObjectAnimator.ofFloat(view.foodList, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(view.foodList, "x", view.width.toFloat(), 0f)
                )
                anim.duration = intRes(android.R.integer.config_mediumAnimTime).toLong()
                anim.start()
                view.fab.setImageResource(R.drawable.ic_close_white_24dp)
                view.fab.setOnClickListener {
                    send(HideFoodList)
                }
            }

            FOOD_LIST_HIDDEN -> {
                val anim = AnimatorSet()
                anim.playTogether(
                    ObjectAnimator.ofFloat(view.foodList, "alpha", 1f, 0f),
                    ObjectAnimator.ofFloat(view.foodList, "x", 0f, view.width.toFloat())
                )
                anim.duration = intRes(android.R.integer.config_mediumAnimTime).toLong()
                anim.start()
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
        }
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
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pet_food, parent, false))

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

}