package io.ipoli.android.growth.persistence

import android.annotation.SuppressLint
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.graphics.Palette
import io.ipoli.android.common.datetime.*
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.common.sumByLong
import io.ipoli.android.quest.Entity
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters
import kotlin.math.roundToInt

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 06/06/2018.
 */

data class AppUsageStat(
    /**
     * package name of the app
     */
    override val id: String,
    val name: String,
    val color: Int,
    val usagePercent: Int,
    val usageDuration: Duration<Second>,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : Entity

interface AppUsageStatRepository : Repository<AppUsageStat> {

    fun findForDate(date: LocalDate = LocalDate.now()): List<AppUsageStat>

    fun findForWeek(
        weekStart: LocalDate = LocalDate.now().with(
            TemporalAdjusters.previousOrSame(
                DateUtils.firstDayOfWeek
            )
        )
    ): List<AppUsageStat>

    fun findForMonth(
        monthStart: LocalDate = LocalDate.now().withDayOfMonth(1)
    ): List<AppUsageStat>
}

class AndroidAppUsageStatRepository(private val context: Context) : AppUsageStatRepository {

    @SuppressLint("WrongConstant")
    private val usageStats: UsageStatsManager =
        context.getSystemService("usagestats") as UsageStatsManager

    private val packageManager = context.packageManager

    override fun findForDate(date: LocalDate): List<AppUsageStat> {
        val stats = usageStats.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            date.startOfDay(),
            System.currentTimeMillis()
        ).groupBy { it.packageName }.map {
            val first = it.value.first()
            it.value.drop(1).forEach {
                first.add(it)
            }
            first
        }
        return toAppUsageStats(
            stats
        )
    }

    override fun findForWeek(weekStart: LocalDate): List<AppUsageStat> {
        val stats = usageStats.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            weekStart.startOfDay(),
            System.currentTimeMillis()
        ).groupBy { it.packageName }.map {
            val first = it.value.first()
            it.value.drop(1).forEach {
                first.add(it)
            }
            first
        }
        return toAppUsageStats(
            stats
        )
    }

    override fun findForMonth(monthStart: LocalDate): List<AppUsageStat> {
        val stats = usageStats.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            monthStart.startOfDay(),
            System.currentTimeMillis()
        ).groupBy { it.packageName }.map {
            val first = it.value.first()
            it.value.drop(1).forEach {
                first.add(it)
            }
            first
        }
        return toAppUsageStats(
            stats
        )
    }

    private fun toAppUsageStats(stats: List<UsageStats>): List<AppUsageStat> {

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val list = packageManager.queryIntentActivities(intent, 0)
        val launchableApps = list.map { it.activityInfo.applicationInfo.packageName }.toSet()
        val filtered = stats.filter {
            launchableApps.contains(it.packageName) && it.packageName != context.packageName && it.totalTimeInForeground.milliseconds.asMinutes.intValue > 0
        }.sortedByDescending {
            it.totalTimeInForeground
        }

        val totalTimeSpent = filtered.sumByLong { it.totalTimeInForeground }

        return filtered.map {
            val ai = packageManager.getApplicationInfo(it.packageName, 0)
            val bitmap = packageManager.getApplicationIcon(ai).toBitmap()

            val p = Palette.from(bitmap).generate()
            val color = p.dominantSwatch?.rgb ?: p.swatches.firstOrNull()?.rgb ?: Color.BLUE

            packageManager.getApplicationIcon(ai)
            AppUsageStat(
                id = it.packageName,
                name = packageManager.getApplicationLabel(ai).toString(),
                color = color,
                usagePercent = Math.min(
                    ((it.totalTimeInForeground / totalTimeSpent.toDouble()) * 100).roundToInt(),
                    100
                ),
                usageDuration = it.totalTimeInForeground.milliseconds.asSeconds
            )
        }
    }

    override fun save(entity: AppUsageStat): AppUsageStat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun save(entities: List<AppUsageStat>): List<AppUsageStat> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) {
            return bitmap
        }

        val width = if (bounds.isEmpty) intrinsicWidth else bounds.width()
        val height = if (bounds.isEmpty) intrinsicHeight else bounds.height()

        return Bitmap.createBitmap(Math.max(width, 0), Math.max(height, 0), Bitmap.Config.ARGB_8888)
            .also {
                val canvas = Canvas(it)
                setBounds(0, 0, canvas.width, canvas.height)
                draw(canvas)
            }
    }

}