package io.ipoli.android.tag.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.redux.android.ReduxViewController

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/03/2018.
 */
class TagListViewController(args: Bundle? = null) :
    ReduxViewController<TagListAction, TagListViewState, TagListReducer>(
        args = args,
        renderDuplicateStates = true
    ) {
    override val reducer = TagListReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        return inflater.inflate(R.layout.controller_tag_list, container, false)
    }

    override fun render(state: TagListViewState, view: View) {

    }

}