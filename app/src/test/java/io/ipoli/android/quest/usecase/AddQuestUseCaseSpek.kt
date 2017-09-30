package io.ipoli.android.quest.usecase

import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.quest.data.Category
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.quest.persistence.QuestRepository
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/30/17.
 */
object AddQuestUseCaseSpek : Spek({

    describe("AddQuestUseCase") {

        it("should give validation error when quest name is empty") {
            val repo = mock<QuestRepository>()
            val result = execute(repo, Quest("", Category.WELLNESS))
            result `should be instance of` Result.Invalid::class
            (result as Result.Invalid).errors `should equal` listOf(Result.ValidationError.EMPTY_NAME)
        }
    }
})

private fun execute(repo: QuestRepository, quest: Quest) =
    AddQuestUseCase(repo).execute(quest).blockingIterable().first()