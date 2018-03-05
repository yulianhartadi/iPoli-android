package mypoli.android

import com.couchbase.lite.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthProvider
import kotlinx.coroutines.experimental.runBlocking
import mypoli.android.auth.error.SignInError
import mypoli.android.common.datetime.Time
import mypoli.android.common.datetime.instant
import mypoli.android.common.datetime.startOfDayUTC
import mypoli.android.common.di.Module
import mypoli.android.pet.Food
import mypoli.android.quest.*
import mypoli.android.quest.data.persistence.DbBounty
import mypoli.android.quest.data.persistence.DbQuest
import mypoli.android.quest.data.persistence.DbReminder
import mypoli.android.quest.data.persistence.DbTimeRange
import org.threeten.bp.Instant
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/22/17.
 */
class Migration : Injects<Module> {

    private val cbDatabase by required { database }
    private val firestoreDatabase by required { firestoreDatabase }
    private val sharedPreferences by required { sharedPreferences }
    private val questRepository by required { questRepository }
    private val playerRepository by required { playerRepository }

    init {
        inject(myPoliApp.module(myPoliApp.instance))
    }

    fun run(onStart : () -> Unit) {
        runBlocking {
            try {
                val playerSchema = playerRepository.findSchemaVersion()

                if (playerSchema == null || playerSchema != Constants.SCHEMA_VERSION) {
                    onStart()
                    // migrate from older ver
                } else {
                    //result
                }

            } catch (e: Exception) {
                onStart()
                migrateFromCouchbase()
            }
        }
    }

    private suspend fun migrateFromCouchbase() {
        val resultSet = Query.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(cbDatabase))
            .where(Expression.property("type").equalTo(Expression.value("Player")))
            .limit(Expression.value(1)).execute()

        val playerId = resultSet.next().getString("id")
        var doc = cbDatabase.getDocument(playerId).toMutable()

        if (!doc.contains("schemaVersion")) {
            doc.setInt("schemaVersion", 1)
            doc.setInt("gems", 0)
            cbDatabase.save(doc)
            doc = cbDatabase.getDocument(playerId).toMutable()
        }

        if (doc.getInt("schemaVersion") == 1) {
            val inventoryPets = doc.getDictionary("inventory").getArray("pets")
            inventoryPets.forEach {
                (it as MutableDictionary).setArray("items", MutableArray())
            }
            val pet = doc.getDictionary("pet")
            val equipment = MutableDictionary()
            equipment.setString("hat", null)
            equipment.setString("mask", null)
            equipment.setString("bodyArmor", null)
            pet.setDictionary("equipment", equipment)
            doc.setInt("schemaVersion", 2)
            cbDatabase.save(doc)
            doc = cbDatabase.getDocument(playerId).toMutable()
        }

        if (doc.getInt("schemaVersion") == 2) {
            val inventory = doc.getDictionary("inventory")
            inventory.setArray("challenges", MutableArray())
            doc.setInt("schemaVersion", 3)
            cbDatabase.save(doc)
            doc = cbDatabase.getDocument(playerId).toMutable()
        }

        if (doc.getInt("schemaVersion") == 3) {
            val questResultSet = Query.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(cbDatabase))
                .where(
                    Expression.property("type")
                        .equalTo(Expression.value("Quest"))
                )
                .execute()

            val iterator = questResultSet.iterator()
            while (iterator.hasNext()) {
                val questId = iterator.next().getString("id")
                val questDoc = cbDatabase.getDocument(questId).toMutable()
                questDoc.setArray("timeRanges", MutableArray())
                cbDatabase.save(questDoc)
            }
            doc.setInt("schemaVersion", 4)
            cbDatabase.save(doc)
        }

        if (doc.getInt("schemaVersion") == 4) {

            val playerResultSet = Query.select(SelectResult.all())
                .from(DataSource.database(cbDatabase))
                .where(Expression.property("type").equalTo(Expression.value("Player")))
                .limit(Expression.value(1)).execute()

            val playerMap = playerResultSet.next().toMap()["myPoli"] as MutableMap<String, Any?>

            val questResultSet = Query.select(SelectResult.all())
                .from(DataSource.database(cbDatabase))
                .where(Expression.property("type").equalTo(Expression.value("Quest")))
                .execute()

            val quests = questResultSet.map {
                val questMap = it.toMap()["myPoli"] as MutableMap<String, Any?>
                questMap.remove("type")
                toQuest(questMap)
            }


            val authResult = loginAnonymousUser()

            if (authResult.isSuccessful) {
                val fsPlayerId = FirebaseAuth.getInstance().currentUser!!.uid
                migratePlayer(fsPlayerId, playerMap)
                sharedPreferences.edit().putString(Constants.KEY_PLAYER_ID, fsPlayerId)
                    .commit()
                migrateQuests(quests)
                cbDatabase.path.deleteOnExit()
            } else {
                throw SignInError(
                    "Sign in anonymous user during migration failed",
                    authResult.exception
                )
            }

        }
    }

    private suspend fun loginAnonymousUser(): Task<AuthResult> =
        suspendCoroutine { continuation ->
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener {
                continuation.resume(it)
            }
        }

    private fun migrateQuests(quests: List<Quest>) {
        quests.forEach {
            questRepository.save(it)
        }
    }

    fun toQuest(dataMap: MutableMap<String, Any?>): Quest {
        dataMap["id"] = ""
        val cq = DbQuest(dataMap.withDefault {
            null
        })

        val plannedDate = cq.scheduledDate.startOfDayUTC
        val plannedTime = cq.startMinute?.let { Time.of(it.toInt()) }

        return Quest(
            id = cq.id,
            name = cq.name,
            color = Color.valueOf(cq.color),
            icon = cq.icon?.let {
                Icon.valueOf(it)
            },
            category = Category(cq.category, Color.GREEN),
            scheduledDate = plannedDate,
            startTime = plannedTime,
            duration = cq.duration,
            experience = cq.experience?.toInt(),
            coins = cq.coins?.toInt(),
            bounty = cq.bounty?.let {
                val cr = DbBounty(it)
                when {
                    cr.type == DbBounty.Type.NONE.name -> Quest.Bounty.None
                    cr.type == DbBounty.Type.FOOD.name -> Quest.Bounty.Food(Food.valueOf(cr.name!!))
                    else -> null
                }
            },
            completedAtDate = cq.completedAtDate?.startOfDayUTC,
            completedAtTime = cq.completedAtMinute?.let {
                Time.of(it.toInt())
            },
            reminder = cq.reminder?.let {
                val cr = DbReminder(it)
                Reminder(cr.message, Time.of(cr.minute), cr.date?.startOfDayUTC)
            },
            timeRanges = cq.timeRanges.map {
                val ctr = DbTimeRange(it)
                TimeRange(
                    TimeRange.Type.valueOf(ctr.type),
                    ctr.duration,
                    ctr.start?.instant,
                    ctr.end?.instant
                )
            }
        )
    }

    private fun migratePlayer(
        fsPlayerId: String,
        playerMap: MutableMap<String, Any?>
    ) {

        playerMap.remove("type")
        playerMap["authProvider"] = mapOf<String, Any?>(
            "userId" to FirebaseAuth.getInstance().currentUser!!.providerData.first().uid,
            "provider" to FirebaseAuthProvider.PROVIDER_ID
        )
        playerMap["displayName"] = ""
        playerMap["username"] = ""
        playerMap["schemaVersion"] = Constants.SCHEMA_VERSION
        playerMap["removedAt"] = null
        val now = Instant.now().toEpochMilli()
        playerMap["updatedAt"] = now
        playerMap["createdAt"] = now

        val doc = firestoreDatabase.collection("players").document(fsPlayerId)
        playerMap["id"] = doc.id

        doc.set(playerMap)
    }
}