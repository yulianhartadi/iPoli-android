package mypoli.android.repeatingquest.show

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import mypoli.android.common.redux.android.ReduxViewController

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/21/2018.
 */
class RepeatingQuestViewController(args: Bundle? = null) :
    ReduxViewController<RepeatingQuestAction, RepeatingQuestViewState, RepeatingQuestReducer>(args) {

    override fun render(state: RepeatingQuestViewState, view: View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val reducer = RepeatingQuestReducer
}