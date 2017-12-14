package io.ipoli.android.quest

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.toMillis
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
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
        LocalDateTime.of(remindDate, LocalTime.of(remindTime.hours, remindTime.getMinutes())).toMillis()

}

data class Category(
    val name: String,
    val color: Color
)

enum class ColorPack(val price: Int) {
    FREE(0),
    PACK_BASE(300)
}

enum class Color(val pack: ColorPack) {
    RED(ColorPack.FREE),
    GREEN(ColorPack.FREE),
    BLUE(ColorPack.FREE),
    PURPLE(ColorPack.FREE),
    BROWN(ColorPack.PACK_BASE),
    ORANGE(ColorPack.PACK_BASE),
    PINK(ColorPack.PACK_BASE),
    TEAL(ColorPack.PACK_BASE),
    DEEP_ORANGE(ColorPack.PACK_BASE),
    INDIGO(ColorPack.PACK_BASE),
    BLUE_GREY(ColorPack.PACK_BASE),
    LIME(ColorPack.PACK_BASE)
}

enum class Icon {
    HOME,
    FRIENDS,
    RESTAURANT,
    PAW,
    BRIEFCASE,
    BOOK,
    HEART,
    RUN,
    MED_KIT,
    TREE,
    BEER,
    PLANE,
    COMPASS,
    LIGHT_BULB,
    CAR,
    WRENCH,
    STAR,
    FITNESS,
    COFFEE,
    BUS,
    ACADEMIC,
    CAKE,
    GAME_CONTROLLER,
    FLASK,
    SHOPPING_CART,
    BIKE,
    TRAIN,
    PIZZA,
    PHONE,
    CLOUD,
    SUN,
    AMERICAN_FOOTBALL,
    TROPHY,
    FOOTBALL,
    MONEY,
    CAMERA
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
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAtDate: LocalDate? = null,
    val completedAtTime: Time? = null,
    val experience: Int? = null,
    val coins: Int? = null,
    val bounty: Bounty? = null
) : Entity {

    sealed class Bounty {
        object None : Bounty()
        data class Food(val food: io.ipoli.android.pet.Food) : Bounty()
    }

    val isCompleted = completedAtDate != null
    val endTime: Time?
        get() = startTime?.let { Time.of(it.toMinuteOfDay() + duration) }
    val isScheduled = startTime != null
}

