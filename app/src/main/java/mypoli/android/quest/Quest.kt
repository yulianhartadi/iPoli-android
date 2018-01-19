package mypoli.android.quest

import mypoli.android.common.datetime.Time
import mypoli.android.common.datetime.milliseconds
import mypoli.android.common.datetime.toMillis
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/15/17.
 */

interface Entity {
    val id: String
}

data class Reminder(
    val message: String,
    val remindTime: Time,
    val remindDate: LocalDate
) {
    fun toMillis() =
        LocalDateTime.of(
            remindDate,
            LocalTime.of(remindTime.hours, remindTime.getMinutes())
        ).toMillis()

}

data class Category(
    val name: String,
    val color: Color
)

enum class ColorPack(val gemPrice: Int) {
    FREE(0),
    BASIC(4)
}

enum class Color(val pack: ColorPack) {
    GREEN(ColorPack.FREE),
    BLUE_GREY(ColorPack.FREE),
    BLUE(ColorPack.BASIC),
    RED(ColorPack.BASIC),
    INDIGO(ColorPack.FREE),
    ORANGE(ColorPack.FREE),
    PINK(ColorPack.BASIC),
    TEAL(ColorPack.BASIC),
    DEEP_ORANGE(ColorPack.BASIC),
    PURPLE(ColorPack.BASIC),
    BROWN(ColorPack.BASIC),
    LIME(ColorPack.BASIC)
}

enum class Icon(val pack: IconPack) {
    HOME(IconPack.FREE),
    FRIENDS(IconPack.BASIC),
    RESTAURANT(IconPack.BASIC),
    PAW(IconPack.BASIC),
    BRIEFCASE(IconPack.FREE),
    BOOK(IconPack.FREE),
    HEART(IconPack.BASIC),
    RUN(IconPack.FREE),
    MED_KIT(IconPack.BASIC),
    TREE(IconPack.BASIC),
    BEER(IconPack.BASIC),
    PLANE(IconPack.FREE),
    COMPASS(IconPack.BASIC),
    LIGHT_BULB(IconPack.BASIC),
    CAR(IconPack.BASIC),
    WRENCH(IconPack.BASIC),
    STAR(IconPack.FREE),
    FITNESS(IconPack.BASIC),
    COFFEE(IconPack.BASIC),
    BUS(IconPack.BASIC),
    ACADEMIC(IconPack.BASIC),
    CAKE(IconPack.BASIC),
    GAME_CONTROLLER(IconPack.BASIC),
    FLASK(IconPack.BASIC),
    SHOPPING_CART(IconPack.BASIC),
    BIKE(IconPack.BASIC),
    TRAIN(IconPack.FREE),
    PIZZA(IconPack.FREE),
    PHONE(IconPack.BASIC),
    CLOUD(IconPack.FREE),
    SUN(IconPack.BASIC),
    AMERICAN_FOOTBALL(IconPack.BASIC),
    TROPHY(IconPack.BASIC),
    FOOTBALL(IconPack.BASIC),
    MONEY(IconPack.BASIC),
    CAMERA(IconPack.BASIC)
}

enum class IconPack(val gemPrice: Int) {
    FREE(0),
    BASIC(6)
}

data class Quest(
    override val id: String = "",
    val name: String,
    val color: Color,
    val icon: Icon? = null,
    val category: Category,
    val startTime: Time? = null,
    val scheduledDate: LocalDate,
    val duration: Int,
    val reminder: Reminder? = null,
    val actualStart: LocalDateTime? = null,
    val pomodoroTimeRanges: List<TimeRange> = listOf(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAtDate: LocalDate? = null,
    val completedAtTime: Time? = null,
    val experience: Int? = null,
    val coins: Int? = null,
    val bounty: Bounty? = null
) : Entity {

    sealed class Bounty {
        object None : Bounty()
        data class Food(val food: mypoli.android.pet.Food) : Bounty()
    }

    val isCompleted = completedAtDate != null
    val endTime: Time?
        get() = startTime?.let { Time.of(it.toMinuteOfDay() + duration) }
    val isScheduled = startTime != null

    val actualDuration : Int
        get() {
            if(actualStart != null) {
                return if(isCompleted) {
                    val completedDateTime = LocalDateTime.of(completedAtDate, LocalTime.of(completedAtTime!!.hours, completedAtTime.getMinutes()))
                    (completedDateTime.toMillis() - actualStart.toMillis()).milliseconds.asMinutes.longValue.toInt()
                } else {
                    (LocalDateTime.now().toMillis() - actualStart.toMillis()).milliseconds.asMinutes.longValue.toInt()
                }
            }

            if(pomodoroTimeRanges.isNotEmpty()) {
                return pomodoroTimeRanges.sumBy { it.actualDuration().toInt() }
            }
            return  duration
        }
}

data class TimeRange(
    val type: Type,
    val duration: Int,
    val start: LocalDateTime? = null,
    val end: LocalDateTime? = null
) {
    enum class Type {
        WORK, BREAK
    }

    fun actualDuration(): Long {
        if (start == null || end == null) {
            return duration.toLong()
        }
        return end.toMillis() - start.toMillis()
    }
}
