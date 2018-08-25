package io.ipoli.android.habit.receiver

import android.content.Context
import android.content.Intent
import io.ipoli.android.Constants
import io.ipoli.android.common.AsyncBroadcastReceiver
import io.ipoli.android.common.view.AppWidgetUtil
import io.ipoli.android.habit.usecase.CompleteHabitUseCase
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 8/23/18.
 */
class CompleteHabitReceiver : AsyncBroadcastReceiver() {

    private val completeHabitUseCase by required { completeHabitUseCase }

    override suspend fun onReceiveAsync(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(Constants.HABIT_ID_EXTRA_KEY)
        completeHabitUseCase.execute(CompleteHabitUseCase.Params(habitId))
        AppWidgetUtil.updateHabitWidget(context)
    }

}