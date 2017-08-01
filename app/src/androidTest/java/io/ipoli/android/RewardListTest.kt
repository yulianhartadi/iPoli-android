package io.ipoli.android

import android.support.test.espresso.Espresso
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import io.ipoli.android.rewards.RewardListController
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Created by vini on 7/10/17.
 * This test is not executing due to this: https://github.com/realm/realm-java/issues/4662 & https://github.com/realm/realm-java/pull/4883
 */
@RunWith(AndroidJUnit4::class)
class RewardListTest {

    @Rule @JvmField
    val testRule = ActivityTestRule<MainActivity>(MainActivity::class.java)

    private lateinit var router: Router


    @Before
    fun setUp() {
        val activity = testRule.getActivity()
        activity.runOnUiThread({
            router = testRule.getActivity().router
            router.setRoot(RouterTransaction.with(RewardListController()))
        })
    }

    @Test
    fun listIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.rewardList)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}