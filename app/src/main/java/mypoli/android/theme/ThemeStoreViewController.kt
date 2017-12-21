package mypoli.android.theme

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.PagerAdapter
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.calendar_hour_cell.view.*
import kotlinx.android.synthetic.main.calendar_time_line.view.*
import kotlinx.android.synthetic.main.controller_theme_store.view.*
import kotlinx.android.synthetic.main.item_theme_store.view.*
import kotlinx.android.synthetic.main.view_inventory_toolbar.view.*
import mypoli.android.Constants
import mypoli.android.MainActivity
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.view.*
import mypoli.android.player.Theme
import mypoli.android.quest.calendar.dayview.view.widget.CalendarDayView
import mypoli.android.theme.ThemeStoreViewState.StateType.*
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/12/17.
 */
class ThemeStoreViewController(args: Bundle? = null) :
    MviViewController<ThemeStoreViewState, ThemeStoreViewController, ThemeStorePresenter, ThemeStoreIntent>(args) {

    private lateinit var inventoryToolbar: ViewGroup

    private val presenter by required { themeStorePresenter }

    override fun createPresenter() = presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.controller_theme_store, container, false)

        inventoryToolbar = addToolbarView(R.layout.view_inventory_toolbar) as ViewGroup
        inventoryToolbar.toolbarTitle.setText(R.string.themes)

        view.themePager.clipToPadding = false
        view.themePager.pageMargin = ViewUtils.dpToPx(16f, view.context).toInt()
        return view
    }

    override fun onAttach(view: View) {
        showBackButton()
        super.onAttach(view)
        send(LoadDataIntent)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.actionThemes).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            router.popCurrentController()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun render(state: ThemeStoreViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                val themeAdapter = ThemePagerAdapter(state.viewModels)
                view.themePager.adapter = themeAdapter
            }

            PLAYER_CHANGED -> {
                inventoryToolbar.playerDiamonds.text = state.playerDiamonds.toString()
                (view.themePager.adapter as ThemePagerAdapter).updateAll(state.viewModels)
            }

            THEME_CHANGED -> {
                val pm = PreferenceManager.getDefaultSharedPreferences(activity!!)
                pm.registerOnSharedPreferenceChangeListener { _, key ->
                    if (key == Constants.KEY_THEME) {
                        val intent = Intent(activity!!, MainActivity::class.java)
                        activity!!.startActivity(intent)
                        activity!!.finish()
                    }
                }
                pm.edit().putString(Constants.KEY_THEME, state.theme!!.name).apply()
            }

            THEME_BOUGHT -> {

            }

            THEME_TOO_EXPENSIVE -> {
                Toast.makeText(view.context, "Theme too expensive", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView(view: View) {
        removeToolbarView(inventoryToolbar)
        super.onDestroyView(view)
    }

    inner class ThemePagerAdapter(private var viewModels: List<ThemeViewModel>) : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflater = LayoutInflater.from(container.context)
            val view = inflater.inflate(R.layout.item_theme_store, container, false)
            val vm = viewModels[position]

            val theme = AndroidTheme.valueOf(vm.theme.name)

            val attrs = intArrayOf(R.attr.colorPrimary, R.attr.colorPrimaryDark, R.attr.colorAccent).sortedArray()
            val a = activity!!.theme.obtainStyledAttributes(
                theme.style,
                attrs)

            val primaryColor = a.getResourceId(a.getIndex(attrs.indexOf(R.attr.colorPrimary)), 0)
            val primaryDarkColor = a.getResourceId(a.getIndex(attrs.indexOf(R.attr.colorPrimaryDark)), 0)
            val accentColor = a.getColor(a.getIndex(attrs.indexOf(R.attr.colorAccent)), 0)

            a.recycle()

            view.themeToolbar.setBackgroundResource(primaryColor)
            view.themeNavigationBar.setBackgroundResource(primaryColor)
            view.themeStatusBar.setBackgroundResource(primaryDarkColor)
            view.themeFab.backgroundTintList = ColorStateList.valueOf(accentColor)

            view.themeName.setText(theme.title)

            view.themePrice.text = if (vm.theme.price == 0) {
                stringRes(R.string.free)
            } else {
                vm.theme.price.toString()
            }

            val action = view.themeAction
            val current = view.themeCurrent

            when {
                vm.isCurrent -> {
                    action.visible = false
                    current.visible = true
                }
                vm.isBought -> {
                    action.visible = true
                    current.visible = false
                    action.text = stringRes(R.string.store_theme_in_inventory)
                    action.setOnClickListener {
                        send(ChangeThemeIntent(vm.theme))
                    }
                }
                else -> {
                    action.visible = true
                    current.visible = false
                    action.text = stringRes(R.string.store_buy_theme)

                    action.setOnClickListener {
                        send(BuyThemeIntent(vm.theme))
                    }
                }
            }

            view.themeCalendar.setHourAdapter(object : CalendarDayView.HourCellAdapter {
                override fun bind(view: View, hour: Int) {
                    if (hour > 0) {
                        view.timeLabel.text = hour.toString() + ":00"
                    }
                }
            })

            view.themeCalendar.timeLine.setBackgroundColor(accentColor)
            view.themeCalendar.timeLineIndicator.backgroundTintList = ColorStateList.valueOf(accentColor)

            view.themeCalendar.scrollToNow()

            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            container.removeView(view as View)
        }

        override fun isViewFromObject(view: View, `object`: Any) = view == `object`

        override fun getCount() = viewModels.size

        override fun getItemPosition(`object`: Any) = PagerAdapter.POSITION_NONE

        fun updateAll(viewModels: List<ThemeViewModel>) {
            this.viewModels = viewModels
            notifyDataSetChanged()
        }
    }

}

data class ThemeViewModel(
    val theme: Theme,
    val isBought: Boolean = false,
    val isCurrent: Boolean = false
)