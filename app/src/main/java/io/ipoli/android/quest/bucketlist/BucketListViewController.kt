package io.ipoli.android.quest.bucketlist

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import io.ipoli.android.R
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.recyclerview.MultiViewRecyclerViewAdapter
import io.ipoli.android.quest.CompletedQuestViewController
import io.ipoli.android.quest.show.QuestViewController
import kotlinx.android.synthetic.main.controller_bucket_list.view.*
import kotlinx.android.synthetic.main.item_agenda_quest.view.*

class BucketListViewController(args: Bundle? = null) :
    ReduxViewController<BucketListAction, BucketListViewState, BucketListReducer>(args) {

    override val reducer = BucketListReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = container.inflate(R.layout.controller_bucket_list)

        view.questList.layoutManager = LinearLayoutManager(activity!!)
        view.questList.adapter = QuestAdapter()

        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        toolbarTitle = stringRes(R.string.title_bucket_list)
    }

    override fun render(state: BucketListViewState, view: View) {
    }

    sealed class ItemViewModel {

        data class SectionItem(val text: String) : ItemViewModel()

        data class QuestItem(
            val id: String,
            val name: String,
            val startTime: String,
            @ColorRes val color: Int,
            val icon: IIcon,
            val isRepeating: Boolean,
            val isFromChallenge: Boolean
        ) : ItemViewModel()

        data class CompletedQuestItem(
            val id: String,
            val name: String,
            val startTime: String,
            @ColorRes val color: Int,
            val icon: IIcon,
            val isRepeating: Boolean,
            val isFromChallenge: Boolean
        ) : ItemViewModel()
    }

    enum class ViewType(val value: Int) {
        SECTION(0),
        QUEST(1),
        COMPLETED_QUEST(2)
    }

    inner class QuestAdapter : MultiViewRecyclerViewAdapter() {

        override fun onRegisterItemBinders() {

            registerBinder<ItemViewModel.SectionItem>(
                ViewType.SECTION.value,
                R.layout.item_list_section,
                { vm, view ->
                    (view as TextView).text = vm.text
                }
            )

            registerBinder<ItemViewModel.QuestItem>(
                ViewType.QUEST.value,
                R.layout.item_agenda_quest,
                { vm, view ->
                    view.questName.text = vm.name
                    view.questTagName.gone()

                    view.questIcon.backgroundTintList =
                            ColorStateList.valueOf(colorRes(vm.color))
                    view.questIcon.setImageDrawable(
                        IconicsDrawable(view.context)
                            .icon(vm.icon)
                            .colorRes(R.color.md_white)
                            .paddingDp(3)
                            .sizeDp(24)
                    )

                    view.questStartTime.text = vm.startTime

                    view.questRepeatIndicator.visibility =
                            if (vm.isRepeating) View.VISIBLE else View.GONE
                    view.questChallengeIndicator.visibility =
                            if (vm.isFromChallenge) View.VISIBLE else View.GONE

                    view.setOnClickListener {
                        val handler = FadeChangeHandler()
                        rootRouter.pushController(
                            RouterTransaction
                                .with(QuestViewController(vm.id))
                                .pushChangeHandler(handler)
                                .popChangeHandler(handler)
                        )
                    }
                }
            )

            registerBinder<ItemViewModel.CompletedQuestItem>(
                ViewType.COMPLETED_QUEST.value,
                R.layout.item_agenda_quest,
                { vm, view ->
                    view.questName.text = vm.name

                    view.questIcon.backgroundTintList =
                            ColorStateList.valueOf(colorRes(vm.color))
                    view.questIcon.setImageDrawable(
                        IconicsDrawable(view.context)
                            .icon(vm.icon)
                            .colorRes(R.color.md_white)
                            .paddingDp(3)
                            .sizeDp(24)
                    )

                    view.questStartTime.text = vm.startTime

                    view.questRepeatIndicator.visibility =
                            if (vm.isRepeating) View.VISIBLE else View.GONE
                    view.questChallengeIndicator.visibility =
                            if (vm.isFromChallenge) View.VISIBLE else View.GONE

                    view.setOnClickListener {
                        val handler = FadeChangeHandler()
                        rootRouter.pushController(
                            RouterTransaction
                                .with(CompletedQuestViewController(vm.id))
                                .pushChangeHandler(handler)
                                .popChangeHandler(handler)
                        )
                    }
                })
        }
    }
}