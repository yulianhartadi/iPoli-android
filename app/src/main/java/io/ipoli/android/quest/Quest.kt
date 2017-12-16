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
    BASIC(300)
}

enum class Color(val pack: ColorPack) {
    RED(ColorPack.FREE),
    GREEN(ColorPack.FREE),
    BLUE(ColorPack.FREE),
    PURPLE(ColorPack.FREE),
    BROWN(ColorPack.BASIC),
    ORANGE(ColorPack.BASIC),
    PINK(ColorPack.BASIC),
    TEAL(ColorPack.BASIC),
    DEEP_ORANGE(ColorPack.BASIC),
    INDIGO(ColorPack.BASIC),
    BLUE_GREY(ColorPack.BASIC),
    LIME(ColorPack.BASIC)
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
    TREE(IconPack.BASIC),
    BEER(IconPack.BASIC),
    PLANE(IconPack.BASIC),
    COMPASS(IconPack.BASIC),
    LIGHT_BULB(IconPack.BASIC),
    CAR(IconPack.BASIC),
    WRENCH(IconPack.BASIC),
    STAR(IconPack.BASIC),
    FITNESS(IconPack.BASIC),
    COFFEE(IconPack.BASIC),
    BUS(IconPack.BASIC),
    ACADEMIC(IconPack.BASIC),
    CAKE(IconPack.BASIC),
    GAME_CONTROLLER(IconPack.BASIC),
    FLASK(IconPack.BASIC),
    SHOPPING_CART(IconPack.BASIC),
    BIKE(IconPack.BASIC),
    TRAIN(IconPack.BASIC),
    PIZZA(IconPack.BASIC),
    PHONE(IconPack.BASIC),
    CLOUD(IconPack.BASIC),
    SUN(IconPack.BASIC),
    AMERICAN_FOOTBALL(IconPack.BASIC),
    TROPHY(IconPack.BASIC),
    FOOTBALL(IconPack.BASIC),
    MONEY(IconPack.BASIC),
    CAMERA(IconPack.BASIC)
}

enum class IconPack(val price: Int) {
    FREE(0),
    BASIC(300)
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

