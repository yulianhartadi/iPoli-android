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

enum class Icon(val pack: IconPack) {
    HOME(IconPack.FREE),
    FRIENDS(IconPack.FREE),
    RESTAURANT(IconPack.FREE),
    PAW(IconPack.FREE),
    BRIEFCASE(IconPack.FREE),
    BOOK(IconPack.FREE),
    HEART(IconPack.FREE),
    RUN(IconPack.FREE),
    MED_KIT(IconPack.FREE),
    TREE(IconPack.PACK_BASE),
    BEER(IconPack.PACK_BASE),
    PLANE(IconPack.PACK_BASE),
    COMPASS(IconPack.PACK_BASE),
    LIGHT_BULB(IconPack.PACK_BASE),
    CAR(IconPack.PACK_BASE),
    WRENCH(IconPack.PACK_BASE),
    STAR(IconPack.PACK_BASE),
    FITNESS(IconPack.PACK_BASE),
    COFFEE(IconPack.PACK_BASE),
    BUS(IconPack.PACK_BASE),
    ACADEMIC(IconPack.PACK_BASE),
    CAKE(IconPack.PACK_BASE),
    GAME_CONTROLLER(IconPack.PACK_BASE),
    FLASK(IconPack.PACK_BASE),
    SHOPPING_CART(IconPack.PACK_BASE),
    BIKE(IconPack.PACK_BASE),
    TRAIN(IconPack.PACK_BASE),
    PIZZA(IconPack.PACK_BASE),
    PHONE(IconPack.PACK_BASE),
    CLOUD(IconPack.PACK_BASE),
    SUN(IconPack.PACK_BASE),
    AMERICAN_FOOTBALL(IconPack.PACK_BASE),
    TROPHY(IconPack.PACK_BASE),
    FOOTBALL(IconPack.PACK_BASE),
    MONEY(IconPack.PACK_BASE),
    CAMERA(IconPack.PACK_BASE)
}

enum class IconPack(val price: Int) {
    FREE(0),
    PACK_BASE(300)
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

