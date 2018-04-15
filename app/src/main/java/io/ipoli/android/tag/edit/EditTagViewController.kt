package io.ipoli.android.tag.edit

import android.os.Bundle
import android.view.*
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import io.ipoli.android.R
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.tag.edit.EditTagViewState.StateType.*
import kotlinx.android.synthetic.main.controller_edit_tag.view.*
import kotlinx.android.synthetic.main.view_no_elevation_toolbar.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 4/4/18.
 */
class EditTagViewController(args: Bundle? = null) :
    ReduxViewController<EditTagAction, EditTagViewState, EditTagReducer>(args) {

    override val reducer = EditTagReducer

    private var tagId: String? = null

    constructor(tagId: String) : this() {
        this.tagId = tagId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_edit_tag, container, false)
        setToolbar(view.toolbar)
        return view
    }

    override fun onCreateLoadAction() = EditTagAction.Load(tagId)

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onDetach(view: View) {
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.onDetach(view)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_tag_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                router.handleBack()
            }

            R.id.actionSave -> {
                dispatch(EditTagAction.Validate(view!!.tagName.text.toString()))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    override fun render(state: EditTagViewState, view: View) {
        when (state.type) {
            DATA_LOADED -> {
                toolbarTitle = state.title
                renderName(view, state)
                colorLayout(view, state)
                renderColor(view, state)
                renderIcon(view, state)
            }

            COLOR_CHANGED -> {
                colorLayout(view, state)
                renderColor(view, state)
            }

            ICON_CHANGED -> {
                renderIcon(view, state)
            }

            VALIDATION_ERROR_EMPTY_NAME -> {
                view.tagName.error = stringRes(R.string.think_of_a_name)
            }

            VALIDATION_ERROR_EXISTING_NAME -> {
                view.tagName.error = stringRes(R.string.existing_tag_message)
            }

            VALIDATION_SUCCESSFUL -> {
                dispatch(EditTagAction.Save)
                router.handleBack()
            }
        }
    }

    private fun renderName(view: View, state: EditTagViewState) {
        view.tagName.setText(state.name)
        view.tagName.setSelection(state.name.length)
    }

    private fun renderIcon(
        view: View,
        state: EditTagViewState
    ) {
        view.tagSelectedIcon.setImageDrawable(
            IconicsDrawable(view.context)
                .largeIcon(state.iicon)
        )

        view.tagIcon.setOnClickListener {
            IconPickerDialogController({ icon ->
                dispatch(EditTagAction.ChangeIcon(icon))
            }, state.icon?.androidIcon).showDialog(
                router,
                "pick_icon_tag"
            )

        }
    }

    private fun renderColor(
        view: View,
        state: EditTagViewState
    ) {
        colorLayout(view, state)
        view.tagColor.setOnClickListener {
            ColorPickerDialogController({
                dispatch(EditTagAction.ChangeColor(it.color))
            }, state.color.androidColor).showDialog(
                router,
                "pick_color_tag"
            )
        }
    }

    private fun colorLayout(
        view: View,
        state: EditTagViewState
    ) {
        val color500 = colorRes(state.color.androidColor.color500)
        val color700 = colorRes(state.color.androidColor.color700)
        view.appbar.setBackgroundColor(color500)
        view.toolbar.setBackgroundColor(color500)
        view.rootContainer.setBackgroundColor(color500)
        activity?.window?.navigationBarColor = color500
        activity?.window?.statusBarColor = color700
    }

    private val EditTagViewState.title: String
        get() = stringRes(if (id == null) R.string.title_add_tag else R.string.title_edit_tag)

    private val EditTagViewState.iicon: IIcon
        get() = icon?.androidIcon?.icon ?: MaterialDesignIconic.Icon.gmi_label
}