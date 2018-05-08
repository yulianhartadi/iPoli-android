package io.ipoli.android.onboarding.scenes

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.R
import io.ipoli.android.common.redux.android.BaseViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.RecyclerViewViewModel
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.onboarding.OnboardAction
import io.ipoli.android.onboarding.OnboardReducer
import io.ipoli.android.onboarding.OnboardViewController
import io.ipoli.android.onboarding.OnboardViewState
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.Reminder
import io.ipoli.android.quest.RepeatingQuest
import io.ipoli.android.repeatingquest.entity.RepeatPattern
import kotlinx.android.synthetic.main.controller_onboard_pick_repeating_quests.view.*
import kotlinx.android.synthetic.main.item_onboard_repeating_quest.view.*
import kotlinx.android.synthetic.main.view_default_toolbar.view.*

class PickRepeatingQuestsViewController(args: Bundle? = null) :
    BaseViewController<OnboardAction, OnboardViewState>(
        args
    ) {

    override val stateKey = OnboardReducer.stateKey

    private var repeatingQuestViewModels = mutableListOf<RepeatingQuestViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {

        setHasOptionsMenu(true)
        val view = container.inflate(R.layout.controller_onboard_pick_repeating_quests)

        setToolbar(view.toolbar)
        toolbarTitle = stringRes(R.string.onboard_pick_repeating_quests_title)

        repeatingQuestViewModels.clear()
        repeatingQuestViewModels.addAll(createViewModels())

        view.onboardRepeatingQuests.layoutManager =
                LinearLayoutManager(
                    container.context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
        val adapter = RepeatingQuestsAdapter()
        view.onboardRepeatingQuests.adapter = adapter
        adapter.updateAll(repeatingQuestViewModels)

        return view
    }

    private fun createViewModels() = listOf(
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_floss),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_floss),
                icon = Icon.FLOWER,
                color = Color.GREEN,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 15,
                repeatPattern = RepeatPattern.Daily()
            ),
            isSelected = true,
            tag = OnboardViewController.OnboardTag.WELLNESS
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_drink_water),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_drink_water),
                icon = Icon.DROP,
                color = Color.BLUE,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 15,
                repeatPattern = RepeatPattern.Daily()
            ),
            tag = OnboardViewController.OnboardTag.WELLNESS
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_workout),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_workout),
                icon = Icon.FITNESS,
                color = Color.GREEN,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 60,
                repeatPattern = RepeatPattern.Flexible.Weekly(
                    3
                )
            ),
            isSelected = true,
            tag = OnboardViewController.OnboardTag.WELLNESS
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_meditate),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_meditate),
                icon = Icon.TREE,
                color = Color.GREEN,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 20,
                repeatPattern = RepeatPattern.Flexible.Weekly(
                    3
                )
            ),
            tag = OnboardViewController.OnboardTag.WELLNESS
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_email),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_email),
                icon = Icon.MAIL,
                color = Color.RED,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 30,
                repeatPattern = RepeatPattern.Flexible.Weekly(
                    5
                )
            ),
            tag = OnboardViewController.OnboardTag.WORK
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_read),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_read),
                icon = Icon.BOOK,
                color = Color.BLUE,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 30,
                repeatPattern = RepeatPattern.Flexible.Weekly(
                    5
                )
            ),
            isSelected = true
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_bike),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_bike),
                icon = Icon.BIKE,
                color = Color.GREEN,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 60,
                repeatPattern = RepeatPattern.Flexible.Weekly(
                    3
                )
            ),
            tag = OnboardViewController.OnboardTag.WELLNESS
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_family_dinner),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_family_dinner),
                icon = Icon.RESTAURANT,
                color = Color.PURPLE,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 60,
                repeatPattern = RepeatPattern.Flexible.Weekly(
                    3
                )
            ),
            tag = OnboardViewController.OnboardTag.PERSONAL
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_call_friend),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_call_friend),
                icon = Icon.PHONE,
                color = Color.ORANGE,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 30,
                repeatPattern = RepeatPattern.Flexible.Weekly(
                    2
                )
            ),
            isSelected = true,
            tag = OnboardViewController.OnboardTag.PERSONAL
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_date_night),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_date_night),
                icon = Icon.HEART,
                color = Color.PINK,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 90,
                repeatPattern = RepeatPattern.Flexible.Weekly(
                    1
                )
            ),
            tag = OnboardViewController.OnboardTag.PERSONAL
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_learn_new_language),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_learn_new_language),
                icon = Icon.ACADEMIC,
                color = Color.BLUE,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 30,
                repeatPattern = RepeatPattern.Flexible.Weekly(
                    5
                )
            )
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_call_parent),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_call_parent),
                icon = Icon.PHONE,
                color = Color.ORANGE,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 30,
                repeatPattern = RepeatPattern.Flexible.Weekly(
                    2
                )
            ),
            tag = OnboardViewController.OnboardTag.PERSONAL
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_walk_the_dog),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_walk_the_dog),
                icon = Icon.PAW,
                color = Color.TEAL,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 60,
                repeatPattern = RepeatPattern.Daily()
            )
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_take_walk),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_take_walk),
                icon = Icon.TREE,
                color = Color.GREEN,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 60,
                repeatPattern = RepeatPattern.Flexible.Weekly(
                    3
                )
            ),
            tag = OnboardViewController.OnboardTag.WELLNESS
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_run),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_run),
                icon = Icon.RUN,
                color = Color.GREEN,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 60,
                repeatPattern = RepeatPattern.Flexible.Weekly(
                    2
                )
            ),
            tag = OnboardViewController.OnboardTag.WELLNESS
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_play_with_cat),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_play_with_cat),
                icon = Icon.PAW,
                color = Color.TEAL,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 30,
                repeatPattern = RepeatPattern.Daily()
            )
        ),
        RepeatingQuestViewModel(
            name = stringRes(R.string.predefined_rq_stretch),
            repeatingQuest = RepeatingQuest(
                name = stringRes(R.string.predefined_rq_stretch),
                icon = Icon.FLOWER,
                color = Color.GREEN,
                reminders = listOf(Reminder.Relative("", 0)),
                duration = 30,
                repeatPattern = RepeatPattern.Flexible.Weekly(
                    2
                )
            ),
            tag = OnboardViewController.OnboardTag.WELLNESS
        )
    )

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.onboard_pick_repeating_quests_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.actionDone) {
            dispatch(OnboardAction.Done)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateLoadAction(): OnboardAction? {
        val rqs = mutableMapOf<Int, Pair<RepeatingQuest, OnboardViewController.OnboardTag?>>()
        repeatingQuestViewModels.forEachIndexed { i, vm ->
            if (vm.isSelected) {
                rqs[i] = vm.repeatingQuest to vm.tag
            }
        }
        return OnboardAction.LoadRepeatingQuests(rqs)
    }


    override fun render(state: OnboardViewState, view: View) {
    }

    data class RepeatingQuestViewModel(
        val name: String,
        val repeatingQuest: RepeatingQuest,
        val isSelected: Boolean = false,
        val tag: OnboardViewController.OnboardTag? = null
    ) :
        RecyclerViewViewModel {
        override val id: String
            get() = name
    }

    inner class RepeatingQuestsAdapter :
        BaseRecyclerViewAdapter<RepeatingQuestViewModel>(
            R.layout.item_onboard_repeating_quest
        ) {

        override fun onBindViewModel(
            vm: RepeatingQuestViewModel,
            view: View,
            holder: SimpleViewHolder
        ) {
            view.rqName.text = vm.name
            view.rqRepeatPattern.text = "${vm.repeatingQuest.repeatPattern.periodCount} x week"

            view.rqIcon.backgroundTintList =
                    ColorStateList.valueOf(colorRes(vm.repeatingQuest.color.androidColor.color500))
            view.rqIcon.setImageDrawable(
                IconicsDrawable(view.context)
                    .icon(vm.repeatingQuest.icon!!.androidIcon.icon)
                    .colorRes(R.color.md_white)
                    .paddingDp(3)
                    .sizeDp(24)
            )

            view.rqCheck.setOnCheckedChangeListener(null)
            view.rqCheck.isChecked = vm.isSelected

            view.setOnClickListener {
                view.rqCheck.toggle()
            }

            view.rqCheck.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    dispatch(
                        OnboardAction.SelectRepeatingQuest(
                            holder.adapterPosition,
                            vm.repeatingQuest,
                            vm.tag
                        )
                    )
                } else {
                    dispatch(OnboardAction.DeselectRepeatingQuest(holder.adapterPosition))
                }
            }
        }

    }
}