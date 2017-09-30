package io.ipoli.android.store.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import io.ipoli.android.R
import io.ipoli.android.common.navigation.Navigator
import kotlinx.android.synthetic.main.controller_store_home.view.*

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/20/17.
 */
class StoreHomeController : Controller() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.controller_store_home, container, false)
        view.avatarsContainer.setOnClickListener({ Navigator(router).showAvatarList() })
        return view
    }
}