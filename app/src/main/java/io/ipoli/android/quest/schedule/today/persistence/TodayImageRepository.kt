package io.ipoli.android.quest.schedule.today.persistence

import android.content.SharedPreferences
import io.ipoli.android.Constants
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.quest.schedule.today.TodayImageUrlProvider
import org.threeten.bp.LocalDate

interface TodayImageRepository {

    fun findForDay(date: LocalDate): String
}

class AndroidTodayImageRepository(private val sharedPreferences: SharedPreferences) :
    TodayImageRepository {

    override fun findForDay(date: LocalDate): String {
        val todayImageMillis = sharedPreferences.getLong(Constants.KEY_TODAY_IMAGE_DATE, -1)
        return if (todayImageMillis < 0 || todayImageMillis != date.startOfDayUTC()) {
            val imageUrl = TodayImageUrlProvider.getRandomImageUrl()
            sharedPreferences
                .edit()
                .putLong(Constants.KEY_TODAY_IMAGE_DATE, date.startOfDayUTC())
                .putString(Constants.KEY_TODAY_IMAGE_URL, imageUrl)
                .apply()
            imageUrl
        } else {
            sharedPreferences.getString(Constants.KEY_TODAY_IMAGE_URL, "")
        }
    }

}