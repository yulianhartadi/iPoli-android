package io.ipoli.android.habit.predefined

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.recyclerview.MultiViewRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.RecyclerViewViewModel
import io.ipoli.android.habit.edit.EditHabitViewController
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import kotlinx.android.synthetic.main.controller_predefined_habit_list.view.*
import kotlinx.android.synthetic.main.item_predefined_habit.view.*
import kotlinx.android.synthetic.main.view_default_toolbar.view.*
import org.threeten.bp.DayOfWeek

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/21/18.
 */
class PredefinedHabitListViewController(args: Bundle? = null) :
    RestoreViewOnCreateController(
        args
    ) {

    private val predefinedHabits: List<PredefinedHabit> = listOf(
        PredefinedHabit(
            name = "Brush teeth",
            color = Color.GREEN,
            icon = Icon.TOOTH,
            isGood = true,
            timesADay = 2,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Make the bed",
            color = Color.BLUE_GREY,
            icon = Icon.HOTEL,
            isGood = true,
            timesADay = 1,
            days = DayOfWeek.values().toSet() - DayOfWeek.SUNDAY
        ),
        PredefinedHabit(
            name = "Plan my day",
            color = Color.PURPLE,
            icon = Icon.EVENT_NOTE,
            isGood = true,
            timesADay = 1,
            days = Constants.DEFAULT_PLAN_DAYS
        ),
        PredefinedHabit(
            name = "Floss",
            color = Color.DEEP_ORANGE,
            icon = Icon.TOOTH,
            isGood = true,
            timesADay = 1,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Use mouthwash",
            color = Color.INDIGO,
            icon = Icon.TOOTH,
            isGood = true,
            timesADay = 1,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Drink a glass of water",
            color = Color.BLUE,
            icon = Icon.GLASS_WATER,
            isGood = true,
            timesADay = 8,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Eat a healthy meal",
            color = Color.ORANGE,
            icon = Icon.RESTAURANT,
            isGood = true,
            timesADay = 1,
            days = DayOfWeek.values().toSet() - DayOfWeek.SUNDAY
        ),
        PredefinedHabit(
            name = "Drink a smoothie",
            color = Color.PURPLE,
            icon = Icon.APPLE,
            isGood = true,
            timesADay = 1,
            days = DayOfWeek.values().toSet() - DayOfWeek.SUNDAY
        ),
        PredefinedHabit(
            name = "Record weight",
            color = Color.GREEN,
            icon = Icon.SCALE,
            isGood = true,
            timesADay = 1,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Record blood pressure",
            color = Color.RED,
            icon = Icon.MED_KIT,
            isGood = true,
            timesADay = 1,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Record heart rate",
            color = Color.RED,
            icon = Icon.HEART,
            isGood = true,
            timesADay = 1,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Take a photo",
            color = Color.PURPLE,
            icon = Icon.PHOTO_CAMERA,
            isGood = true,
            timesADay = 1,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Stretch",
            color = Color.GREEN,
            icon = Icon.STRETCH,
            isGood = true,
            timesADay = 5,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Take a vitamin",
            color = Color.GREEN,
            icon = Icon.MED_KIT,
            isGood = true,
            timesADay = 1,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Just smile",
            color = Color.PINK,
            icon = Icon.SMILE,
            isGood = true,
            timesADay = 3,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Shower",
            color = Color.BLUE,
            icon = Icon.DROP,
            isGood = true,
            timesADay = 2,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Hug a friend",
            color = Color.PURPLE,
            icon = Icon.PEOPLE,
            isGood = true,
            timesADay = 1,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Close eyes and relax",
            color = Color.PURPLE,
            icon = Icon.PAUSE,
            isGood = true,
            timesADay = 4,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "No smoking",
            color = Color.BLUE_GREY,
            icon = Icon.SMOKE,
            isGood = false,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Don't bite nails",
            color = Color.TEAL,
            icon = Icon.HAND,
            isGood = false,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "No sweets",
            color = Color.BROWN,
            icon = Icon.ICE_CREAM,
            isGood = false,
            days = DayOfWeek.values().toSet() - DayOfWeek.SUNDAY
        ),
        PredefinedHabit(
            name = "Don't buy unnecessary things",
            color = Color.GREEN,
            icon = Icon.MONEY,
            isGood = false,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "Too much alcohol",
            color = Color.DEEP_ORANGE,
            icon = Icon.DRINK,
            isGood = false,
            days = DayOfWeek.values().toSet()
        ),
        PredefinedHabit(
            name = "No swearing",
            color = Color.LIME,
            icon = Icon.BIOHAZARD,
            isGood = false,
            days = DayOfWeek.values().toSet()
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_predefined_habit_list, container, false)
        setToolbar(view.toolbar)
        toolbarTitle = stringRes(R.string.predefined_habits_title)

        view.habitList.layoutManager = LinearLayoutManager(view.context)
        val adapter = HabitListAdapter()
        view.habitList.adapter = adapter
        adapter.updateAll(viewModels)

        return view
    }

    override fun onAttach(view: View) {
        colorStatusBars()
        super.onAttach(view)
        showBackButton()
    }

    private fun colorStatusBars() {
        activity?.window?.statusBarColor = attrData(io.ipoli.android.R.attr.colorPrimaryDark)
        activity?.window?.navigationBarColor = attrData(io.ipoli.android.R.attr.colorPrimary)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home ->
                router.handleBack()

            else -> super.onOptionsItemSelected(item)
        }

    sealed class ItemViewModel(override val id: String) : RecyclerViewViewModel {
        data class SectionItem(val text: String) : ItemViewModel(text)

        data class HabitItem(
            override val id: String,
            val name: String,
            val icon: IIcon,
            @ColorRes val color: Int,
            val habit: PredefinedHabit
        ) : ItemViewModel(id)
    }

    enum class ViewType(val value: Int) {
        SECTION(0),
        HABIT(1)
    }

    inner class HabitListAdapter :
        MultiViewRecyclerViewAdapter<ItemViewModel>() {

        override fun onRegisterItemBinders() {
            registerBinder<ItemViewModel.SectionItem>(
                ViewType.SECTION.value,
                R.layout.item_list_section
            ) { vm, view, _ ->
                (view as TextView).text = vm.text
            }

            registerBinder<ItemViewModel.HabitItem>(
                ViewType.HABIT.value,
                R.layout.item_predefined_habit
            ) { vm, view, _ ->
                view.habitName.text = vm.name
                view.habitIcon.backgroundTintList =
                    ColorStateList.valueOf(colorRes(vm.color))

                val icon = IconicsDrawable(view.context).listItemIcon(vm.icon)

                view.habitIcon.setImageDrawable(icon)
                view.setOnClickListener(Debounce.clickListener {
                    val habit = vm.habit
                    navigate().toAddHabit(
                        EditHabitViewController.Params(
                            name = habit.name,
                            color = habit.color,
                            icon = habit.icon,
                            isGood = habit.isGood,
                            timesADay = habit.timesADay,
                            days = habit.days
                        )
                    )
                })
            }
        }
    }

    val viewModels: List<ItemViewModel>
        get() {
            val (good, bad) = predefinedHabits.partition { it.isGood }
            val vms = mutableListOf<ItemViewModel>()
            vms.add(ItemViewModel.SectionItem("Good habits"))
            vms.addAll(good.map {
                ItemViewModel.HabitItem(
                    id = "",
                    name = it.name,
                    icon = it.icon.androidIcon.icon,
                    color = it.color.androidColor.color500,
                    habit = it
                )
            })
            vms.add(ItemViewModel.SectionItem("Bad habits"))
            vms.addAll(bad.map {
                ItemViewModel.HabitItem(
                    id = "",
                    name = it.name,
                    icon = it.icon.androidIcon.icon,
                    color = it.color.androidColor.color500,
                    habit = it
                )
            })
            return vms
        }


    private val Color.androidColor: AndroidColor
        get() = AndroidColor.valueOf(this.name)

    private val Icon.androidIcon: AndroidIcon
        get() = AndroidIcon.valueOf(this.name)
}