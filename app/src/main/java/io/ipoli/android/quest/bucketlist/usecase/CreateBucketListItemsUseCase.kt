package io.ipoli.android.quest.bucketlist.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest

class CreateBucketListItemsUseCase :
    UseCase<CreateBucketListItemsUseCase.Params, List<CreateBucketListItemsUseCase.BucketListItem>> {

    override fun execute(parameters: Params): List<BucketListItem> {
        return emptyList()
    }

    data class Params(val quests: List<Quest>)

    sealed class BucketListItem {

    }
}