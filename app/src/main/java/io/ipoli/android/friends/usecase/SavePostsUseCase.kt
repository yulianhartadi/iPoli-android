package io.ipoli.android.friends.usecase

import io.ipoli.android.achievement.Achievement
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.entity.SharingPreference
import io.ipoli.android.challenge.persistence.ChallengeRepository
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.days
import io.ipoli.android.common.datetime.daysBetween
import io.ipoli.android.common.datetime.minutes
import io.ipoli.android.friends.feed.data.Post
import io.ipoli.android.friends.feed.persistence.ImageRepository
import io.ipoli.android.friends.feed.persistence.PostRepository
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.quest.Quest
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 07/16/2018.
 */
open class SavePostsUseCase(
    private val postRepository: PostRepository,
    private val playerRepository: PlayerRepository,
    private val challengeRepository: ChallengeRepository,
    private val imageRepository: ImageRepository
) : UseCase<SavePostsUseCase.Params, Unit> {

    override fun execute(parameters: Params) {

        val player = parameters.player ?: playerRepository.find()!!
        if (!player.isLoggedIn()) {
            return
        }

        val isAutoPostingEnabled = player.preferences.isAutoPostingEnabled

        when (parameters) {
            is Params.LevelUp -> {
                if (isAutoPostingEnabled && parameters.newLevel % 5 == 0) {
                    postRepository.save(
                        Post(
                            playerId = player.id,
                            playerAvatar = player.avatar,
                            playerDisplayName = player.displayName ?: "Unknown Hero",
                            playerUsername = player.username!!,
                            playerLevel = parameters.newLevel,
                            data = Post.Data.LevelUp(parameters.newLevel),
                            description = null,
                            reactions = emptyList(),
                            comments = emptyList(),
                            status = Post.Status.APPROVED,
                            isFromCurrentPlayer = true
                        )
                    )
                }
            }

            is Params.DailyChallengeComplete -> {
                val stats = player.statistics
                if (isAutoPostingEnabled && stats.dailyChallengeCompleteStreak.count > 1 && stats.dailyChallengeCompleteStreak.count % 5 == 0L) {
                    savePost(
                        player = player,
                        data = Post.Data.DailyChallengeCompleted(
                            streak = stats.dailyChallengeCompleteStreak.count.toInt(),
                            bestStreak = stats.dailyChallengeBestStreak.toInt()
                        )
                    )
                }
            }

            is Params.AchievementUnlocked -> {
                if (isAutoPostingEnabled && parameters.achievement.level > 1) {
                    savePost(
                        player = player,
                        data = Post.Data.AchievementUnlocked(parameters.achievement)
                    )
                }
            }

            is Params.QuestComplete -> {
                val quest = parameters.quest
                savePost(
                    player = player,
                    data = if (quest.hasPomodoroTimer) {
                        Post.Data.QuestWithPomodoroShared(
                            quest.id,
                            quest.name,
                            quest.totalPomodoros!!
                        )
                    } else {
                        Post.Data.QuestShared(
                            quest.id,
                            quest.name,
                            if (quest.hasTimer) quest.actualDuration.asMinutes else 0.minutes
                        )
                    },
                    imageUrl = saveImageIfAdded(parameters.imageData),
                    description = parameters.description
                )
            }

            is Params.QuestFromChallengeComplete -> {
                val q = parameters.quest
                val challenge = parameters.challenge
                savePost(
                    player = player,
                    data = if (q.hasPomodoroTimer) {
                        Post.Data.QuestWithPomodoroFromChallengeCompleted(
                            questId = q.id,
                            challengeId = challenge.id,
                            questName = q.name,
                            challengeName = challenge.name,
                            pomodoroCount = q.totalPomodoros!!
                        )
                    } else {
                        Post.Data.QuestFromChallengeCompleted(
                            questId = q.id,
                            challengeId = challenge.id,
                            questName = q.name,
                            challengeName = challenge.name,
                            durationTracked = if (q.hasTimer) q.actualDuration.asMinutes else 0.minutes
                        )
                    },
                    imageUrl = saveImageIfAdded(parameters.imageData),
                    description = parameters.description
                )
            }

            is Params.ChallengeShared -> {
                val challenge = parameters.challenge
                challengeRepository.save(challenge.copy(sharingPreference = SharingPreference.FRIENDS))
                savePost(
                    player = player,
                    data = Post.Data.ChallengeShared(challenge.id, challenge.name),
                    imageUrl = saveImageIfAdded(parameters.imageData),
                    description = parameters.description
                )
            }

            is Params.ChallengeComplete -> {
                val c = parameters.challenge
                savePost(
                    player = player,
                    data = Post.Data.ChallengeCompleted(
                        c.id,
                        c.name,
                        c.startDate.daysBetween(c.completedAtDate!!).days
                    ),
                    imageUrl = saveImageIfAdded(parameters.imageData),
                    description = parameters.description
                )
            }

            is Params.HabitCompleted -> {
                val habit = parameters.habit
                val challenge = parameters.challenge
                savePost(
                    player = player,
                    data = Post.Data.HabitCompleted(
                        habitId = habit.id,
                        habitName = habit.name,
                        habitDate = LocalDate.now(),
                        challengeId = challenge?.id,
                        challengeName = challenge?.name,
                        isGood = habit.isGood,
                        streak = habit.streak.current,
                        bestStreak = habit.streak.best
                    ),
                    imageUrl = saveImageIfAdded(parameters.imageData),
                    description = parameters.description
                )
            }

        }
    }

    private fun saveImageIfAdded(imageData: ByteArray?) =
        imageData?.let {
            imageRepository.savePostImage(it)
        }

    private fun savePost(
        player: Player,
        data: Post.Data,
        imageUrl: String? = null,
        description: String? = null
    ) {
        postRepository.save(
            Post(
                playerId = player.id,
                playerAvatar = player.avatar,
                playerDisplayName = player.displayName ?: "Unknown Hero",
                playerUsername = player.username!!,
                playerLevel = player.level,
                data = data,
                imageUrl = imageUrl,
                description = description,
                reactions = emptyList(),
                comments = emptyList(),
                status = imageUrl?.let { Post.Status.PENDING } ?: Post.Status.APPROVED,
                isFromCurrentPlayer = true
            )
        )
    }

    sealed class Params {
        abstract val player: Player?

        data class LevelUp(val newLevel: Int, override val player: Player?) : Params()

        data class DailyChallengeComplete(override val player: Player? = null) : Params()

        data class AchievementUnlocked(val achievement: Achievement, override val player: Player?) :
            Params()

        data class QuestComplete(
            val quest: Quest,
            val description: String?,
            val imageData: ByteArray?,
            override val player: Player?
        ) : Params()

        data class QuestFromChallengeComplete(
            val quest: Quest,
            val challenge: Challenge,
            val description: String?,
            val imageData: ByteArray?,
            override val player: Player?
        ) : Params()

        data class ChallengeShared(
            val challenge: Challenge,
            val description: String?,
            val imageData: ByteArray?,
            override val player: Player?
        ) : Params()

        data class HabitCompleted(
            val habit: Habit,
            val challenge: Challenge?,
            val description: String?,
            val imageData: ByteArray?,
            override val player: Player?
        ) : Params()

        data class ChallengeComplete(
            val challenge: Challenge,
            val description: String?,
            val imageData: ByteArray?,
            override val player: Player? = null
        ) : Params()
    }
}