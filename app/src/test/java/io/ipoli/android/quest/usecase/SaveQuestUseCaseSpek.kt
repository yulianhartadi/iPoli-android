package io.ipoli.android.quest.usecase

import io.ipoli.android.quest.data.Category
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.reactivex.Single
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/30/17.
 */
object SaveQuestUseCaseSpek : Spek({

    describe("SaveQuestUseCase") {

        it("should give validation error when quest name is empty") {
            val repo = mock<QuestRepository>()
            val result = execute(repo, Quest("", Category.WELLNESS))
            result `should be instance of` Result.Invalid::class
            (result as Result.Invalid).errors `should equal` listOf(Result.ValidationError.EMPTY_NAME)
        }

        it("should save new quest") {
            val q = Quest("name", Category.WELLNESS)
            val repo = mock(QuestRepository::class)
            When calling repo.save(q) `it returns` Single.just(q)

            val result = execute(repo, q)
            result `should be instance of` Result.Added::class
            Verify on repo that repo.save(q) was called
        }
    }
})

private fun execute(repo: QuestRepository, quest: Quest) =
    SaveQuestUseCase(repo).execute(quest).blockingIterable().first()