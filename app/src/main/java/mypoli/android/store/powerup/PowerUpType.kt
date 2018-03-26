package mypoli.android.store.powerup

import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/15/2018.
 */

data class PowerUp(
    val type: Type,
    val coinPrice: Int,
    val expirationDate: LocalDate
) {

    companion object {
        fun fromType(type: PowerUp.Type, expirationDate: LocalDate): PowerUp {
            return PowerUp(type, type.coinPrice, expirationDate)
        }
    }

    enum class Type(val coinPrice: Int) {
//        REMINDERS(130),
        CHALLENGES(220),
//        CALENDAR_SYNC(450),
        TIMER(130),
//        SUB_QUESTS(180),
//        NOTES(90),
//        CUSTOM_DURATION(130)
    }
}
