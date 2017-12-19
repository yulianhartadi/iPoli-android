package mypoli.android.quest.data

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/19/17.
 */
//open class Reminder {
//
//    var message: String? = null
//
//    private var minutesFromStart: Long? = null
//
//    var notificationId: String? = null
//
//    var start: Long? = null
//
//    constructor()
//
//    @JvmOverloads constructor(minutesFromStart: Long, notificationId: String = Random().nextInt().toString()) {
//        this.minutesFromStart = minutesFromStart
//        this.notificationId = notificationId
//        message = ""
//    }
//
//    fun getMinutesFromStart(): Long {
//        return minutesFromStart!!
//    }
//
//    fun setMinutesFromStart(minutesFromStart: Long) {
//        this.minutesFromStart = minutesFromStart
//    }
//
//    val notificationNum: Int
//        get() = Integer.valueOf(notificationId)!!
//
//    fun calculateStartTime(realmQuest: RealmQuest) {
//        val questStartTime = realmQuest.getStartDateTimeMillis()
//        if (questStartTime == null) {
//            start = null
//            return
//        }
//        start = questStartTime + TimeUnit.MINUTES.toMillis(getMinutesFromStart())
//    }
//
//    var startTime: Date?
//        get() = if (start != null) Date(start!!) else null
//        set(startTime) {
//            start = startTime?.time
//        }
//}
