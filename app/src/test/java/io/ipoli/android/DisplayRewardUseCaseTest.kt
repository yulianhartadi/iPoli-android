package io.ipoli.android

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.player.Player
import io.ipoli.android.player.PlayerRepository
import io.ipoli.android.reward.DisplayRewardsUseCase
import io.ipoli.android.reward.Reward
import io.ipoli.android.reward.RewardRepository
import io.ipoli.android.reward.RewardsLoadedPartialChange
import io.ipoli.android.util.RxSchedulersTestRule
import io.reactivex.Observable
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test


/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 7/10/17.
 */
class DisplayRewardUseCaseTest {

    @Rule
    @JvmField
    val rxRule = RxSchedulersTestRule()

    @Test
    fun listIsDisplayed() {

        val playerRepoMock = mock<PlayerRepository> {
            on { get() } doReturn Observable.just(Player())
        }

        val rewardRepoMock = mock<RewardRepository> {
            on { loadRewards() } doReturn Observable.just(listOf(Reward()))
        }

        val result = DisplayRewardsUseCase(rewardRepoMock, playerRepoMock).execute(Unit).blockingIterable()
        assertThat(result.count(), `is`(2))

        val loadedState = result.elementAt(1) as RewardsLoadedPartialChange
        assertThat(loadedState.data.size, `is`(1))
    }
}