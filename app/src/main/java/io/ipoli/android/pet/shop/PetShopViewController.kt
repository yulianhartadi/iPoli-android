package io.ipoli.android.pet.shop

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.mvi.MviViewController
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.pet.PetMood
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
        view.petPager.clipToPadding = false
        view.petPager.pageMargin = ViewUtils.dpToPx(32f, view.context).toInt()
        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(LoadDataIntent)
    }

    override fun render(state: PetShopViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                view.petPager.adapter = PetPagerAdapter(state.petViewModels)
            }
        }

    }

    data class PetViewModel(val avatar: AndroidPetAvatar, val isBought: Boolean = false, val isSelected: Boolean = false)

    inner class PetPagerAdapter(private val viewModels: List<PetViewModel>) : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflater = LayoutInflater.from(container.context)
            val view = inflater.inflate(R.layout.item_pet_shop, container, false)
            val vm = viewModels[position]
            val avatar = vm.avatar
            view.petName.setText(avatar.petName)
            view.pet.setImageResource(avatar.image)
            view.petState.setImageResource(avatar.moodImage[PetMood.HAPPY]!!)
            view.petPrice.text = PetAvatar.valueOf(avatar.name).price.toString()
            view.petDescription.setText(avatar.description)
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            container.removeView(view as View)
        }

        override fun isViewFromObject(view: View, `object`: Any) = view == `object`

        override fun getCount() = viewModels.size

    }
}