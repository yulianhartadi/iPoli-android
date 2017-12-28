package mypoli.android.pet.store

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import kotlinx.android.synthetic.main.controller_pet_store.view.*
import kotlinx.android.synthetic.main.item_pet_store.view.*
import kotlinx.android.synthetic.main.view_inventory_toolbar.view.*
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.view.*
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.pet.PetAvatar
import mypoli.android.pet.PetMood
import mypoli.android.pet.store.PetStoreIntent.*
import mypoli.android.pet.store.PetStoreViewState.StateType.*
import mypoli.android.store.GemStoreViewController
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 12/4/17.
 */
class PetStoreViewController(args: Bundle? = null) : MviViewController<PetStoreViewState, PetStoreViewController, PetStorePresenter, PetStoreIntent>(args) {
    private val presenter by required { petStorePresenter }

    override fun createPresenter() = presenter

    private lateinit var inventoryToolbar: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_pet_store, container, false)

        inventoryToolbar = addToolbarView(R.layout.view_inventory_toolbar) as ViewGroup
        inventoryToolbar.toolbarTitle.setText(R.string.store)
        inventoryToolbar.playerGems.setOnClickListener {
            send(ShowCurrencyConverter)
        }

        view.petPager.clipToPadding = false
        view.petPager.pageMargin = ViewUtils.dpToPx(16f, view.context).toInt()
        return view
    }

    override fun onAttach(view: View) {
        showBackButton()
        super.onAttach(view)
        send(LoadData)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            router.popCurrentController()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun render(state: PetStoreViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                view.petPager.adapter = PetPagerAdapter(listOf())
            }

            PLAYER_CHANGED -> {
                inventoryToolbar.playerGems.text = state.playerGems.toString()
                (view.petPager.adapter as PetPagerAdapter).updateAll(state.petViewModels)
            }

            PET_TOO_EXPENSIVE -> {
                CurrencyConverterDialogController().showDialog(router, "currency-converter")
                Toast.makeText(view.context, "Pet too expensive", Toast.LENGTH_SHORT).show()
            }

            SHOW_CURRENCY_CONVERTER -> {
                CurrencyConverterDialogController().showDialog(router, "currency-converter")
            }

            SHOW_UNLOCK_PET -> {
                showGemStore()
            }
        }
    }

    override fun onDestroyView(view: View) {
        removeToolbarView(inventoryToolbar)
        super.onDestroyView(view)
    }

    private fun showGemStore() {
        val handler = FadeChangeHandler()
        router.pushController(
            RouterTransaction.with(GemStoreViewController())
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )
    }

    data class PetViewModel(val avatar: AndroidPetAvatar, val isBought: Boolean = false, val isCurrent: Boolean = false, val isLocked: Boolean = false)

    inner class PetPagerAdapter(private var viewModels: List<PetViewModel>) : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflater = LayoutInflater.from(container.context)
            val view = inflater.inflate(R.layout.item_pet_store, container, false)
            val vm = viewModels[position]
            val avatar = vm.avatar
            view.petName.setText(avatar.petName)
            view.pet.setImageResource(avatar.image)
            view.petPrice.text = PetAvatar.valueOf(avatar.name).gemPrice.toString()
            view.petDescription.setText(avatar.description)
            val action = view.petAction
            val current = view.currentPet
            when {
                vm.isCurrent -> {
                    action.visible = false
                    current.visible = true
                    view.petState.setImageResource(avatar.moodImage[PetMood.HAPPY]!!)
                }
                vm.isBought -> {
                    action.visible = true
                    current.visible = false
                    action.text = stringRes(R.string.store_pet_in_inventory)
                    action.setOnClickListener {
                        send(ChangePet(PetAvatar.valueOf(vm.avatar.name)))
                    }
                    view.petState.setImageResource(avatar.moodImage[PetMood.GOOD]!!)
                }
                vm.isLocked -> {
                    action.visible = true
                    current.visible = false
                    action.text = stringRes(R.string.unlock)
                    action.setOnClickListener {
                        send(UnlockPet(PetAvatar.valueOf(vm.avatar.name)))
                    }
                    view.petState.setImageResource(avatar.moodImage[PetMood.GOOD]!!)
                }
                else -> {
                    action.visible = true
                    current.visible = false
                    action.text = stringRes(R.string.store_buy_pet)
                    action.setOnClickListener {
                        send(BuyPet(PetAvatar.valueOf(vm.avatar.name)))

                    }
                    view.petState.setImageResource(avatar.moodImage[PetMood.GOOD]!!)
                }
            }
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            container.removeView(view as View)
        }

        override fun isViewFromObject(view: View, `object`: Any) = view == `object`

        override fun getCount() = viewModels.size

        override fun getItemPosition(`object`: Any) = PagerAdapter.POSITION_NONE

        fun updateAll(viewModels: List<PetViewModel>) {
            this.viewModels = viewModels
            notifyDataSetChanged()
        }

    }
}