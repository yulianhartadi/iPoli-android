package io.ipoli.android.pet.shop

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.mvi.MviViewController
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.shop.PetShopViewState.StateType.DATA_LOADED
import kotlinx.android.synthetic.main.controller_pet_shop.view.*
import kotlinx.android.synthetic.main.item_pet_shop.view.*
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/4/17.
 */
class PetShopViewController(args: Bundle? = null) : MviViewController<PetShopViewState, PetShopViewController, PetShopPresenter, PetShopIntent>(args) {
    private val presenter by required { petShopPresenter }

    override fun createPresenter() = presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.controller_pet_shop, container, false)
        val petGrid = view.petGrid
        petGrid.layoutManager = GridLayoutManager(view.context, 2)
        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(LoadDataIntent)
    }

    override fun render(state: PetShopViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                view.petGrid.adapter = PetAdapter(state.petViewModels)
            }
        }

    }

    data class PetViewModel(val avatar: AndroidPetAvatar, val isBought: Boolean = false, val isSelected: Boolean = false)

    inner class PetAdapter(private val pets: List<PetViewModel>) : RecyclerView.Adapter<PetAdapter.ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val vm = pets[position]
            holder.itemView.pet.setImageResource(vm.avatar.image)

        }

        override fun getItemCount() = pets.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pet_shop, parent, false))

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    }

}