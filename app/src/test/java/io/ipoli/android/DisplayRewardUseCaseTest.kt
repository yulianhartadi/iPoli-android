package io.ipoli.android

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.player.Player
import io.ipoli.android.player.PlayerRepository
import io.ipoli.android.reward.DisplayRewardsUseCase
import io.ipoli.android.reward.Reward
import io.ipoli.android.reward.RewardRepository
import io.ipoli.android.reward.RewardsLoadedPartialChange
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test


/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 7/10/17.
 */
class DisplayRewardUseCaseTest {

    @Before
    fun setUp() {

    }

    @Test
    fun listIsDisplayed() {

        val playerRepoMock = mock<PlayerRepository> {
            on { get() } doReturn Observable.just(Player())
        }

        val rewardRepoMock = mock<RewardRepository> {
            on { loadRewards() } doReturn Observable.just(listOf(Reward()))
        }

        val result = DisplayRewardsUseCase(rewardRepoMock, playerRepoMock, Schedulers.trampoline(), Schedulers.trampoline()).execute(Unit).blockingIterable()
        assertThat(result.count(), `is`(2))

        val loadedState = result.elementAt(1) as RewardsLoadedPartialChange
        assertThat(loadedState.data.size, `is`(1))
    }
}