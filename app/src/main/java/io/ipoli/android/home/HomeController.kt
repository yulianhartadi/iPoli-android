package io.ipoli.android.home

import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import io.ipoli.android.MainActivity
import io.ipoli.android.R
import io.ipoli.android.common.navigation.Navigator
import io.ipoli.android.quest.calendar.ui.CalendarController
import kotlinx.android.synthetic.main.controller_home.view.*


/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/19/17.
 */
class HomeController : Controller(), NavigationView.OnNavigationItemSelectedListener {

    private var navigationItemSelected: MenuItem? = null

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        navigationItemSelected = item
        view?.drawerLayout?.closeDrawer(GravityCompat.START)
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
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

        val actionBarDrawerToggle = object : ActionBarDrawerToggle(activity, view.drawerLayout, view.toolbar, R.string.drawer_open, R.string.drawer_close) {

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
            RouterTransaction.with(CalendarController())
                .pushChangeHandler(handler)
                .popChangeHandler(handler)
        )

        actionBarDrawerToggle.syncState()

    }

    private fun onItemSelectedFromDrawer() {
        when (navigationItemSelected?.itemId) {
            R.id.store -> Navigator(router).showStore()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            view?.drawerLayout?.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}