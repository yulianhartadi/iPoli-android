package io.ipoli.android.pet

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
            PetFoodViewModel(R.drawable.ic_add_accent_24dp, 12),
            PetFoodViewModel(R.drawable.ic_coins_green_24dp, 32),
            PetFoodViewModel(R.drawable.ic_star_yellow_24dp, 23),
            PetFoodViewModel(R.drawable.ic_add_accent_24dp, 12),
            PetFoodViewModel(R.drawable.ic_add_accent_24dp, 12),
            PetFoodViewModel(R.drawable.ic_coins_green_24dp, 32),
            PetFoodViewModel(R.drawable.ic_star_yellow_24dp, 23)

            ))

        return view
    }

    override fun render(state: PetViewState, view: View) {

    }

    data class PetFoodViewModel(@DrawableRes val image: Int, val price: Int)

    inner class PetFoodAdapter(private val foodItems: List<PetFoodViewModel>) : RecyclerView.Adapter<PetFoodAdapter.ViewHolder>() {
        override fun getItemCount() = foodItems.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = foodItems[position]
            holder.itemView.foodImage.setImageResource(vm.image)
            holder.itemView.foodPrice.text = vm.price.toString() + " coins"
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pet_food, parent, false))

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

}