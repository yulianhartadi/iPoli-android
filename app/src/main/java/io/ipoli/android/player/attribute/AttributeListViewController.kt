package io.ipoli.android.player.attribute

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.ListPopupWindow
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.pager.BasePagerAdapter
import io.ipoli.android.common.view.recyclerview.BaseRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.RecyclerViewViewModel
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.player.attribute.AttributeListViewState.StateType.*
import io.ipoli.android.player.data.AndroidAttribute
import io.ipoli.android.player.data.Player
import io.ipoli.android.tag.Tag
import kotlinx.android.synthetic.main.controller_attribute_list.view.*
import kotlinx.android.synthetic.main.item_attribute_bonus.view.*
import kotlinx.android.synthetic.main.item_attribute_list.view.*
import kotlinx.android.synthetic.main.view_default_toolbar.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 9/13/18.
 */
class AttributeListViewController(args: Bundle? = null) :
    ReduxViewController<AttributeListAction, AttributeListViewState, AttributeListReducer>(args) {

    override val reducer = AttributeListReducer

    private var attribute: Player.AttributeType? = null

    override var helpConfig: HelpConfig? =
        HelpConfig(
            io.ipoli.android.R.string.help_dialog_attribute_list_title,
            io.ipoli.android.R.string.help_dialog_attribute_list_message
        )

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {}

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            val vm = (view!!.attributePager.adapter as AttributeAdapter).itemAt(position)
            colorLayout(vm)
        }
    }

    constructor(attribute: Player.AttributeType) : this() {
        this.attribute = attribute
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_attribute_list, container, false)

        view.attributePager.addOnPageChangeListener(onPageChangeListener)
        view.attributePager.adapter = AttributeAdapter()

        view.attributePager.clipToPadding = false
        view.attributePager.pageMargin = ViewUtils.dpToPx(8f, view.context).toInt()

        return view
    }

    override fun onCreateLoadAction() = AttributeListAction.Load(attribute)

    override fun onAttach(view: View) {
        super.onAttach(view)
        setToolbar(view.toolbar)
        showBackButton()
        toolbarTitle = stringRes(R.string.attributes)
    }

    override fun onDestroyView(view: View) {
        view.attributePager.removeOnPageChangeListener(onPageChangeListener)
        super.onDestroyView(view)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            return router.handleBack()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun render(state: AttributeListViewState, view: View) {
        when (state.type) {
            LOADING -> {

            }

            DATA_LOADED -> {
                val vms = state.attributeViewModels
                (view.attributePager.adapter as AttributeAdapter).updateAll(vms)
                view.attributePager.currentItem = state.firstSelectedIndex!!
                colorLayout(vms[state.firstSelectedIndex])
            }

            DATA_CHANGED -> {
                (view.attributePager.adapter as AttributeAdapter).updateAll(state.attributeViewModels)
            }
        }
    }

    private fun colorLayout(vm: AttributeViewModel) {
        view!!.toolbar.setBackgroundColor(vm.backgroundColor)
        activity?.window?.navigationBarColor = vm.backgroundColor
        activity?.window?.statusBarColor = vm.darkBackgroundColor
    }

    data class AttributeViewModel(
        val name: String,
        val attributeType: Player.AttributeType,
        val description: String,
        val level: String,
        val isActive: Boolean,
        val progress: Int,
        val max: Int,
        val currentProgress: String,
        @ColorInt val progressColor: Int,
        @ColorInt val secondaryProgressColor: Int,
        @ColorInt val backgroundColor: Int,
        @ColorInt val darkBackgroundColor: Int,
        @DrawableRes val background: Int,
        @DrawableRes val icon: Int,
        val attributeTags: List<Tag>,
        val tags: List<Tag>,
        val showTag1: Boolean,
        val showTag2: Boolean,
        val showTag3: Boolean,
        val showAddTag: Boolean,
        val bonuses: List<BonusViewModel>
    )

    data class BonusViewModel(
        override val id: String,
        val title: String,
        val description: String,
        val isLocked: Boolean
    ) : RecyclerViewViewModel

    inner class BonusAdapter :
        BaseRecyclerViewAdapter<BonusViewModel>(R.layout.item_attribute_bonus) {

        override fun onBindViewModel(vm: BonusViewModel, view: View, holder: SimpleViewHolder) {
            view.bonusIcon.setImageResource(
                if (vm.isLocked) R.drawable.ic_lock_red_24dp
                else R.drawable.ic_done_green_24dp
            )

            view.bonusTitle.text = vm.title
            view.bonusDescription.text = vm.description
        }

    }

    inner class AttributeAdapter : BasePagerAdapter<AttributeViewModel>() {

        override fun layoutResourceFor(item: AttributeViewModel) = R.layout.item_attribute_list

        override fun bindItem(item: AttributeViewModel, view: View) {
            view.attributeName.text = item.name
            view.attributeDescription.text = item.description
            view.attributeLevel.text = item.level

            view.attributeIconBack.setBackgroundResource(item.background)
            view.attributeIcon.setImageResource(item.icon)

            val square = view.attributeLevel.background as GradientDrawable
            square.mutate()
            square.color = ColorStateList.valueOf(item.darkBackgroundColor)
            view.attributeLevel.background = square

            if (item.isActive) {
                view.progressGroup.visible()
                view.hintGroup.invisible()

                view.attributeProgress.progressTintList =
                    ColorStateList.valueOf(item.progressColor)

                view.attributeProgress.secondaryProgressTintList =
                    ColorStateList.valueOf(item.secondaryProgressColor)

                view.attributeProgress.progress = item.progress
                view.attributeProgress.max = item.max
                view.attributeProgressText.text = item.currentProgress
            } else {
                view.progressGroup.invisible()
                view.hintGroup.visible()
                view.attributeHintIcon.setImageDrawable(
                    IconicsDrawable(view.context)
                        .icon(GoogleMaterial.Icon.gmd_info_outline)
                        .color(attrData(R.attr.colorPrimary))
                        .sizeDp(24)
                )
            }

            renderTags(item, view)

            view.attributeBonusList.layoutManager = LinearLayoutManager(view.context)
            view.attributeBonusList.adapter = BonusAdapter()
            view.attributeBonusList.isNestedScrollingEnabled = false
            (view.attributeBonusList.adapter as BonusAdapter).updateAll(item.bonuses)
        }

        private fun renderTags(
            item: AttributeViewModel,
            view: View
        ) {
            if (item.showTag1) {
                view.tag1.visible()
                view.tag1.text = item.attributeTags[0].name
                renderTagIndicatorColor(view.tag1, item.attributeTags[0])
            } else view.tag1.gone()

            if (item.showTag2) {
                view.tag2.visible()
                view.tag2.text = item.attributeTags[1].name
                renderTagIndicatorColor(view.tag2, item.attributeTags[1])
            } else view.tag2.gone()

            if (item.showTag3) {
                view.tag3.visible()
                view.tag3.text = item.attributeTags[2].name
                renderTagIndicatorColor(view.tag3, item.attributeTags[2])
            } else view.tag3.gone()

            view.tag1.onDebounceClick {
                dispatch(
                    AttributeListAction.RemoveTag(
                        item.attributeType,
                        item.attributeTags[0]
                    )
                )
            }
            view.tag2.onDebounceClick {
                dispatch(
                    AttributeListAction.RemoveTag(
                        item.attributeType,
                        item.attributeTags[1]
                    )
                )
            }
            view.tag3.onDebounceClick {
                dispatch(
                    AttributeListAction.RemoveTag(
                        item.attributeType,
                        item.attributeTags[2]
                    )
                )
            }

            if (item.showAddTag) {
                view.addTag.visible()
                val background = view.addTag.background as GradientDrawable
                background.setColor(item.backgroundColor)

                val popupWindow = ListPopupWindow(activity!!)
                popupWindow.anchorView = view.addTag
                popupWindow.width = ViewUtils.dpToPx(200f, activity!!).toInt()
                popupWindow.setAdapter(TagAdapter(item.tags))
                popupWindow.setOnItemClickListener { _, _, position, _ ->
                    dispatch(AttributeListAction.AddTag(item.attributeType, item.tags[position]))
                    popupWindow.dismiss()
                }


                view.addTag.onDebounceClick {
                    popupWindow.show()
                }
            } else view.addTag.gone()
        }

        private fun renderTagIndicatorColor(view: TextView, tag: Tag) {
            val indicator = view.compoundDrawablesRelative[0] as GradientDrawable
            indicator.mutate()
            indicator.setColor(colorRes(AndroidColor.valueOf(tag.color.name).color500))
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(
                indicator,
                null,
                view.compoundDrawablesRelative[2],
                null
            )
        }


        inner class TagAdapter(tags: List<Tag>) :
            ArrayAdapter<Tag>(
                activity!!,
                R.layout.item_tag_popup,
                tags
            ) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup) =
                bindView(position, convertView, parent)

            override fun getView(position: Int, convertView: View?, parent: ViewGroup) =
                bindView(position, convertView, parent)

            private fun bindView(position: Int, convertView: View?, parent: ViewGroup): View {

                val view = if (convertView == null) {
                    val inflater = LayoutInflater.from(context)
                    inflater.inflate(R.layout.item_tag_popup, parent, false) as TextView
                } else {
                    convertView as TextView
                }

                val item = getItem(position)
                view.text = item.name

                val color = AndroidColor.valueOf(item.color.name).color500
                val indicator = view.compoundDrawablesRelative[0] as GradientDrawable
                indicator.mutate()
                val size = ViewUtils.dpToPx(8f, view.context).toInt()
                indicator.setSize(size, size)
                indicator.setColor(colorRes(color))
                view.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    indicator,
                    null,
                    null,
                    null
                )
                return view
            }
        }
    }

    private val AttributeListViewState.attributeViewModels: List<AttributeViewModel>
        get() {
            val tagToCount = tags!!.map { it to 0 }.toMap().toMutableMap()
            attributes!!.forEach {
                it.tags.forEach { t ->
                    tagToCount[t] = tagToCount[t]!! + 1
                }
            }

            return attributes.map {
                val attr = AndroidAttribute.valueOf(it.type.name)
                val attributeTags = it.tags
                val tagCount = attributeTags.size

                val availableTags =
                    tags.filter { t ->
                        tagToCount[t]!! < 3 && !attributeTags.contains(t)
                    }

                val attrRank = AttributeRank.of(it.level, rank!!)
                val lastRankIndex = Math.min(attrRank.ordinal + 2, Player.Rank.values().size)

                AttributeViewModel(
                    name = stringRes(attr.title),
                    attributeType = it.type,
                    description = stringRes(attr.description),
                    level = it.level.toString(),
                    isActive = tagCount > 0,
                    progress = it.points,
                    max = it.pointsForNextLevel,
                    currentProgress = "${it.points}/${it.pointsForNextLevel}",
                    progressColor = colorRes(attr.colorPrimaryDark),
                    secondaryProgressColor = colorRes(attr.colorPrimary),
                    backgroundColor = colorRes(attr.colorPrimary),
                    darkBackgroundColor = colorRes(attr.colorPrimaryDark),
                    background = attr.background,
                    icon = attr.whiteIcon,
                    attributeTags = attributeTags,
                    tags = availableTags,
                    showTag1 = tagCount > 0,
                    showTag2 = tagCount > 1,
                    showTag3 = tagCount > 2,
                    showAddTag = tagCount < 3 && availableTags.isNotEmpty(),
                    bonuses = Player.Rank.values().toList().subList(
                        1,
                        lastRankIndex
                    ).mapIndexed { i, r ->
                        BonusViewModel(
                            id = r.name,
                            title = stringRes(attr.bonusNames[r]!!),
                            description = stringRes(attr.bonusDescriptions[r]!!),
                            isLocked = !(attrRank == Player.Rank.DIVINITY || i + 1 < lastRankIndex - 1)
                        )
                    }.reversed()
                )
            }
        }
}