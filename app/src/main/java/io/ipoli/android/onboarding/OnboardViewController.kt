package io.ipoli.android.onboarding

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler
import io.ipoli.android.MyPoliApp
import io.ipoli.android.R
import io.ipoli.android.common.di.UIModule
import io.ipoli.android.common.navigation.Navigator
import io.ipoli.android.common.view.Debounce
import io.ipoli.android.common.view.inflate
import io.ipoli.android.common.view.intRes
import io.ipoli.android.common.view.pager.BasePagerAdapter
import io.ipoli.android.common.view.rootRouter
import kotlinx.android.synthetic.main.controller_onboard.view.*
import kotlinx.android.synthetic.main.item_onboard_page.view.*
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

class OnboardViewController(args: Bundle? = null) : RestoreViewOnCreateController(
    args
), Injects<UIModule> {

    private val eventLogger by required { eventLogger }

    companion object {
        const val PAGE_COUNT = 5
    }

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {}

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            eventLogger.logEvent("onboard_change_page", mapOf("position" to position))
            view!!.onboardProgress.animateProgress((position + 1) * 20)
            view!!.onboardNext.setText(
                if (position + 1 == PAGE_COUNT) R.string.done
                else R.string.next
            )
        }
    }

    override fun onContextAvailable(context: Context) {
        inject(MyPoliApp.uiModule(context))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = container.inflate(R.layout.controller_onboard)
        val adapter = OnboardPageAdapter()
        view.onboardPager.adapter = adapter
        adapter.updateAll(
            listOf(
                OnboardPageViewModel(
                    title = "Why myPoli?",
                    message = "Focus on your Goals by combining your ToDos, Events & Habits in one place! Organize your life to find time for friends, family, working out and taking care of yourself.",
                    image = R.drawable.onboard_screen_1
                ),
                OnboardPageViewModel(
                    title = "Turn your life into a game",
                    message = "myPoli motivates you to complete your Tasks and fight procrastination by turning your life into an RPG-style game! Track your progress on Habits & Goals to achieve anything!",
                    image = R.drawable.onboard_screen_2
                ),
                OnboardPageViewModel(
                    title = "Level Up",
                    message = "Complete your Quests (Tasks) to unlock features & get rewarded! Stay productive to Level up your avatar, boost your intelligence, strength, focus, well-being & willpower.",
                    image = R.drawable.onboard_screen_3
                ),
                OnboardPageViewModel(
                    title = "Faithful Companion",
                    message = "Always get reminded on time by your new pet. Take good care of it to get additional rewards & bonuses! Customize your pet with awesome items & make it unique!",
                    image = R.drawable.onboard_screen_4
                ),
                OnboardPageViewModel(
                    title = "Challenge Yourself",
                    message = "Complete step-by-step challenges for workout routines, nutrition plans, destressing your life, learning new skills or just having fun!",
                    image = R.drawable.onboard_screen_5
                )
            )
        )

        view.onboardPager.addOnPageChangeListener(onPageChangeListener)

        view.onboardProgress.max = PAGE_COUNT * 20
        view.onboardProgress.progress = 20

        view.onboardPager.setPageTransformer(
            true
        ) { page, position ->
            val pageWidth = page.width

            if (0 <= position && position < 1) {
                page.translationX = pageWidth * -position
            }
            if (-1 < position && position < 0) {
                page.translationX = pageWidth * -position
            }

            if (position > -1.0f && position < 1.0f && position != 0.0f) {
                val translateX = pageWidth / 2 * position
                page.onboardImage.translationX = translateX
                page.onboardTitle.translationX = translateX
                page.onboardMessage.translationX = translateX
                val alpha = 1.0f - Math.abs(position)
                page.onboardImage.alpha = alpha
                page.onboardImage.scaleX = Math.max(alpha, 0.85f)
                page.onboardImage.scaleY = Math.max(alpha, 0.85f)
                page.onboardTitle.alpha = alpha
                page.onboardMessage.alpha = alpha
            }
        }

        view.onboardExistingPlayer.setOnClickListener(Debounce.clickListener {
            eventLogger.logEvent("onboard_existing_player")
            Navigator(rootRouter).toAuth(
                onboardData = null,
                changeHandler = VerticalChangeHandler()
            )
        })

        view.onboardSkip.setOnClickListener(Debounce.clickListener {
            eventLogger.logEvent("onboard_skip")
            Navigator(rootRouter).toPickOnboardItems()
        })

        view.onboardNext.setOnClickListener(Debounce.clickListener {
            val currentItem = view.onboardPager.currentItem
            if (currentItem + 1 == PAGE_COUNT) {
                eventLogger.logEvent("onboard_done")
                Navigator(rootRouter).toPickOnboardItems()
            } else {
                view.onboardPager.setCurrentItem(currentItem + 1, true)
            }
        })

        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        activity?.let {
            eventLogger.logCurrentScreen(it, "Onboard")
        }
    }

    override fun onDestroyView(view: View) {
        view.onboardPager.removeOnPageChangeListener(onPageChangeListener)
        super.onDestroyView(view)
    }

    data class OnboardPageViewModel(
        val title: String,
        val message: String,
        @DrawableRes val image: Int
    )

    inner class OnboardPageAdapter : BasePagerAdapter<OnboardPageViewModel>() {

        override fun layoutResourceFor(item: OnboardPageViewModel) = R.layout.item_onboard_page

        override fun bindItem(item: OnboardPageViewModel, view: View) {
            view.onboardTitle.text = item.title
            view.onboardMessage.text = item.message
            view.onboardImage.setImageResource(item.image)
        }

    }

    private fun ProgressBar.animateProgress(to: Int) {
        val animator = ObjectAnimator.ofInt(this, "progress", progress, to)
        animator.duration = intRes(android.R.integer.config_shortAnimTime).toLong()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }
}