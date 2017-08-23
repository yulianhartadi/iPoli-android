package io.ipoli.android.home

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import io.ipoli.android.MainActivity
import io.ipoli.android.R
import io.ipoli.android.challenge.list.ui.ChallengeListController
import io.ipoli.android.common.BaseController
import io.ipoli.android.navigator
import kotlinx.android.synthetic.main.controller_home.view.*

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/19/17.
 */
class HomeController : BaseController<HomeController, HomePresenter>(), NavigationView.OnNavigationItemSelectedListener {

    private var navigationItemSelected: MenuItem? = null

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        navigationItemSelected = item
        view?.drawerLayout?.closeDrawer(GravityCompat.START)
        return false
    }

    override fun createPresenter(): HomePresenter = HomePresenter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.controller_home, container, false)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        val activity = activity as MainActivity
        activity.setSupportActionBar(view.toolbar)
        val actionBar = activity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        view.navigationView.setNavigationItemSelectedListener(this)

        val actionBarDrawerToggle = object : ActionBarDrawerToggle(activity, view.drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            override fun onDrawerOpened(drawerView: View) {
                navigationItemSelected = null
            }

            override fun onDrawerClosed(drawerView: View) {
                if (navigationItemSelected == null) {
                    return
                }
                onItemSelectedFromDrawer()
            }
        }

        view.drawerLayout.addDrawerListener(actionBarDrawerToggle)

        val handler = FadeChangeHandler()
        val childRouter = getChildRouter(view.controllerContainer, null)
        childRouter.setRoot(
            RouterTransaction.with(ChallengeListController())
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )

        actionBarDrawerToggle.syncState()
    }

    private fun onItemSelectedFromDrawer() {
        when (navigationItemSelected?.itemId) {
            R.id.store -> navigator.showStore()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            view?.drawerLayout?.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {
    }

}