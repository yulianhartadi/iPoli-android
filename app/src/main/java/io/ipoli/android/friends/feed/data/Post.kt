package io.ipoli.android.friends.feed.data

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import io.ipoli.android.R
import io.ipoli.android.achievement.Achievement
import io.ipoli.android.common.datetime.Day
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Minute
import io.ipoli.android.player.data.Avatar
import io.ipoli.android.quest.Entity
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

data class Post(
    override val id: String = "",
    val playerId: String = "",
    val playerAvatar: Avatar,
    val playerDisplayName: String,
    val playerUsername: String,
    val playerLevel: Int,
    val description: String?,
    val data: Data,
    val reactions: List<Reaction>,
    val comments: List<Comment>,
    val commentCount: Int = 0,
    val imageUrl: String? = null,
    val status: Status,
    val isFromCurrentPlayer: Boolean,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : Entity {

    enum class Status {
        PENDING, APPROVED, REJECTED
    }

    data class Comment(
        val id: String,
        val playerId: String,
        val playerAvatar: Avatar,
        val playerDisplayName: String,
        val playerUsername: String,
        val playerLevel: Int,
        val text: String,
        val createdAt: Instant = Instant.now()
    )

    sealed class Data {
        data class DailyChallengeCompleted(val streak: Int, val bestStreak: Int) : Data()

        data class LevelUp(val level: Int) : Data()

        data class AchievementUnlocked(val achievement: Achievement) : Data()

        data class QuestShared(
            val questId: String,
            val questName: String,
            val durationTracked: Duration<Minute>
        ) : Data()

        data class QuestWithPomodoroShared(
            val questId: String,
            val questName: String,
            val pomodoroCount: Int
        ) : Data()

        data class ChallengeShared(val challengeId: String, val name: String) : Data()

        data class ChallengeCompleted(
            val challengeId: String,
            val name: String,
            val duration: Duration<Day>
        ) : Data()

        data class QuestFromChallengeCompleted(
            val questId: String,
            val challengeId: String,
            val questName: String,
            val challengeName: String,
            val durationTracked: Duration<Minute>
        ) : Data()

        data class QuestWithPomodoroFromChallengeCompleted(
            val questId: String,
            val challengeId: String,
            val questName: String,
            val challengeName: String,
            val pomodoroCount: Int
        ) : Data()

        data class HabitCompleted(
            val habitId: String,
            val habitName: String,
            val habitDate : LocalDate,
            val challengeId: String?,
            val challengeName: String?,
            val isGood: Boolean,
            val streak: Int,
            val bestStreak: Int
        ) : Data()
    }

    data class Reaction(
        val playerId: String,
        val reactionType: ReactionType,
        val createdAt: Instant
    )

    enum class ReactionType {
        LIKE, LOVE, COOL, WOW, BAD, ANGRY
    }

}

enum class AndroidReactionType(
    @StringRes val title: Int,
    @DrawableRes val image: Int,
    val animation: String
) {
    LIKE(
        R.string.like,
        R.drawable.react_like,
        "like.json"
    ),
    LOVE(
        R.string.love,
        R.drawable.react_love,
        "heart.json"
    ),
    COOL(
        R.string.cool,
        R.drawable.react_cool,
        "cool.json"
    ),
    WOW(
        R.string.wow,
        R.drawable.react_wow,
        "wow.json"
    ),
    BAD(
        R.string.bad,
        R.drawable.react_bad,
        "bad.json"
    ),
    ANGRY(
        R.string.angry,
        R.drawable.react_angry,
        "angry.json"
    )
}