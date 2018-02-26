package mypoli.android.repeatingquest.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import mypoli.android.R
import mypoli.android.common.redux.android.ReduxViewController

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/26/18.
 */
class EditRepeatingQuestViewController(args: Bundle? = null) :
    ReduxViewController<EditRepeatingQuestAction, EditRepeatingQuestViewState, EditRepeatingQuestReducer>(
        args
    ) {
    override val reducer = EditRepeatingQuestReducer

    private lateinit var repeatingQuestId: String

    constructor(
        repeatingQuestId: String
    ) : this() {
        this.repeatingQuestId = repeatingQuestId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.controller_edit_repeating_quest, container, false
        )
        return view
    }

    override fun onCreateLoadAction() =
        EditRepeatingQuestAction.Load(repeatingQuestId)

    override fun render(state: EditRepeatingQuestViewState, view: View) {
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
//        showBackButton()
    }
}