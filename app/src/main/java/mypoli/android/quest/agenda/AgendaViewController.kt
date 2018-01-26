package mypoli.android.quest.agenda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import mypoli.android.R

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/26/18.
 */
class AgendaViewController : RestoreViewOnCreateController() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_agenda, container, false)
        return view
    }

}