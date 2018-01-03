package mypoli.android.challenge.data

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import mypoli.android.R
import mypoli.android.common.datetime.Time
import mypoli.android.quest.Color
import mypoli.android.quest.Icon
import org.threeten.bp.DayOfWeek

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
enum class PredefinedChallenge(
    val category: Category,
    val gemPrice: Int,
    val quests: List<PredefinedChallenge.Quest>,
    val durationDays: Int = 30
) {

    STRESS_FREE_MIND(
        Category.HEALTH_AND_FITNESS,
        5,
        listOf(
            PredefinedChallenge.Quest.Repeating(
                "Meditate every day for 20 min",
                "Meditate",
                duration = 20,
                weekDays = DayOfWeek.values().toList(),
                startTime = Time.at(19, 0),
                color = Color.GREEN,
                icon = Icon.SUN
            ),
            PredefinedChallenge.Quest.Repeating(
                "Read a book for 30 min 3 times a week",
                "Read a book",
                duration = 30,
                weekDays = listOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
                color = Color.BLUE,
                icon = Icon.BOOK
            ),
            PredefinedChallenge.Quest.OneTime(
                "Share your troubles with a friend",
                "Share your troubles with a friend",
                preferredDayOfWeek = DayOfWeek.SATURDAY,
                duration = 60,
                color = Color.PURPLE,
                icon = Icon.FRIENDS
            ),
            PredefinedChallenge.Quest.Repeating(
                "Take a walk for 30 min 5 times a week",
                "Take a walk",
                duration = 30,
                weekDays = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SUNDAY
                ),
                color = Color.GREEN,
                icon = Icon.TREE
            ),
            PredefinedChallenge.Quest.Repeating(
                "Say 3 things that I am grateful for every morning",
                "Say 3 things that I am grateful for",
                duration = 15,
                weekDays = DayOfWeek.values().toList(),
                startTime = Time.at(10, 0),
                color = Color.RED,
                icon = Icon.LIGHT_BULB
            )
        )),
    WEIGHT_CUTTER(
        Category.HEALTH_AND_FITNESS,
        5,
        listOf(
            PredefinedChallenge.Quest.OneTime(
                "Sign up for a gym club card",
                "Sign up for a gym club card",
                duration = 30,
                startAtDay = 1,
                color = Color.GREEN,
                icon = Icon.FITNESS
            ),
            PredefinedChallenge.Quest.Repeating(
                "Run 2 times a week for 30 min",
                "Go for a run",
                duration = 30,
                weekDays = listOf(
                    DayOfWeek.TUESDAY,
                    DayOfWeek.SATURDAY
                ),
                color = Color.GREEN,
                icon = Icon.RUN
            ),
            PredefinedChallenge.Quest.Repeating(
                "Workout at the gym 3 times a week for 1h",
                "Go for a run",
                duration = 60,
                startAtDay = 2,
                weekDays = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.FRIDAY
                ),
                color = Color.GREEN,
                icon = Icon.FITNESS
            ),
            PredefinedChallenge.Quest.Repeating(
                "Measure & record my weight every morning",
                "Measure & record my weight",
                duration = 15,
                weekDays = DayOfWeek.values().toList(),
                startTime = Time.at(10, 0),
                color = Color.GREEN,
                icon = Icon.STAR
            ),
            PredefinedChallenge.Quest.Repeating(
                "Prepare healthy dinner 6 times a week",
                "Prepare healthy dinner",
                duration = 45,
                weekDays = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY
                ),
                startTime = Time.at(19, 0),
                color = Color.ORANGE,
                icon = Icon.RESTAURANT
            )
        )),
    HEALTHY_FIT(
        Category.HEALTH_AND_FITNESS,
        5,
        listOf(
            Quest.Repeating(
                "Drink 1 big bottle of water every day",
                "Drink 1 big bottle of water",
                20,
                null,
                Color.GREEN,
                Icon.STAR,
                null,
                DayOfWeek.values().toList(),
                true
            ),
            Quest.Repeating(
                "Eat healthy breakfast every day",
                "Eat healthy breakfast",
                30,
                Time.atHours(9),
                Color.GREEN,
                Icon.RESTAURANT,
                null
            ),
            Quest.Repeating(
                "Workout 3 times a week",
                "Workout",
                60,
                Time.atHours(19),
                Color.GREEN,
                Icon.FITNESS,
                null,
                listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY)
            ),
            Quest.Repeating(
                "Eat a fruit every day",
                "Eat a fruit",
                20,
                null,
                Color.GREEN,
                Icon.TREE,
                null
            ),
            Quest.Repeating(
                "Meditate 3 times a week",
                "Meditate",
                20,
                null,
                Color.GREEN,
                Icon.STAR,
                null,
                listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
            ),
            Quest.Repeating(
                "Cook healthy dinner 5 times a week",
                "Cook healthy dinner",
                60,
                null,
                Color.GREEN,
                Icon.RESTAURANT,
                null,
                listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY
                )
            )
        )
    ),
    ENGLISH_JEDI(
        Category.BUILD_SKILL,
        5,
        listOf(
            Quest.OneTime(
                "Sign up at Duolingo",
                "Sign up at Duolingo",
                20,
                Time.atHours(11),
                Color.BLUE,
                Icon.BOOK,
                1
            ),
            Quest.OneTime(
                "Sign up at a local English course",
                "Sign up at a local English course",
                30,
                null,
                Color.BLUE,
                Icon.BOOK,
                1
            ),
            Quest.OneTime(
                "Subscribe to the Misterduncan YouTube channel",
                "Subscribe to the Misterduncan YouTube channel",
                30,
                Time.at(11, 30),
                Color.BLUE,
                Icon.BOOK,
                1
            ),
            Quest.Repeating(
                "Learn using Duolingo for 20 min every day",
                "Learn using Duolingo",
                20,
                null,
                Color.BLUE,
                Icon.ACADEMIC,
                2
            ),
            Quest.Repeating(
                "Watch e movie with English subtitles 5 times a week",
                "Watch e movie with English subtitles",
                60,
                Time.atHours(20),
                Color.PURPLE,
                Icon.CAMERA,
                null,
                listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
                )
            ),
            Quest.Repeating(
                "Read Alice in Wonderland 4 times a week",
                "Read Alice in Wonderland",
                20,
                Time.atHours(21),
                Color.BLUE,
                Icon.BOOK,
                null,
                listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY
                )
            )
        )),
    CODING_NINJA(
        Category.BUILD_SKILL,
        5,
        listOf(
            Quest.OneTime(
                "Sign up at freeCodeCamp",
                "Sign up at freeCodeCamp",
                20,
                Time.atHours(11),
                Color.BLUE,
                Icon.STAR,
                1
            ),
            Quest.Repeating(
                "Read JavaScript For Cats 3 times a week",
                "Read JavaScript For Cats",
                30,
                null,
                Color.BLUE,
                Icon.BOOK,
                null,
                listOf(
                    DayOfWeek.TUESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.SATURDAY
                )
            ),
            Quest.Repeating(
                "Conquer freeCodeCamp challenges 5 times a week",
                "Conquer freeCodeCamp challenges",
                45,
                null,
                Color.BLUE,
                Icon.LIGHT_BULB,
                null,
                listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY
                )
            ),
            Quest.Repeating(
                "Watch CS50x Programming Course 2 times a week",
                "Watch CS50x Programming Course",
                60,
                null,
                Color.BLUE,
                Icon.LIGHT_BULB,
                null,
                listOf(
                    DayOfWeek.THURSDAY,
                    DayOfWeek.SUNDAY
                )
            )
        )
    ),
    FAMOUS_WRITER(
        Category.BUILD_SKILL,
        5,
        listOf(
            Quest.OneTime(
                "Create blog at Medium",
                "Create blog at Medium",
                30,
                Time.atHours(11),
                Color.BLUE,
                Icon.LIGHT_BULB,
                1
            ),
            Quest.OneTime(
                "Choose what I am going to write about",
                "Choose what I am going to write about",
                30,
                Time.at(11, 30),
                Color.BLUE,
                Icon.LIGHT_BULB,
                1
            ),
            Quest.OneTime(
                "Pick 5 bloggers who inspire you and read most of their posts",
                "Choose what I am going to write about",
                120,
                null,
                Color.BLUE,
                Icon.ACADEMIC,
                2
            ),
            Quest.OneTime(
                "Research & write my first blog post",
                "Research & write my first blog post",
                120,
                null,
                Color.BLUE,
                Icon.LIGHT_BULB,
                3
            ),
            Quest.Repeating(
                "Write a blog post once every week",
                "Write a blog post once",
                90,
                null,
                Color.BLUE,
                Icon.LIGHT_BULB,
                4,
                listOf(
                    DayOfWeek.SATURDAY
                )
            )
        )
    ),
    MASTER_COMMUNICATOR(
        Category.DEEP_WORK,
        5,
        listOf(
            Quest.OneTime(
                "Research how to give great presentation",
                "Research how to give great presentation",
                60,
                null,
                Color.BLUE,
                Icon.LIGHT_BULB,
                1
            ),
            Quest.OneTime(
                "Sign up at Canva",
                "Sign up at Canva",
                20,
                null,
                Color.BLUE,
                Icon.STAR,
                1
            ),
            Quest.OneTime(
                "Create my presentation at Canva",
                "Create my presentation at Canva",
                120,
                null,
                Color.BLUE,
                Icon.LIGHT_BULB,
                2
            ),
            Quest.Repeating(
                "Practice presenting alone every day",
                "Practice presenting alone",
                30,
                null,
                Color.BLUE,
                Icon.STAR,
                3
            ),
            Quest.OneTime(
                "Practice presenting to a friend",
                "Practice presenting to a friend",
                60,
                null,
                Color.BLUE,
                Icon.FRIENDS,
                8
            ),
            Quest.OneTime(
                "Upload my presentation to SlideShare",
                "Upload my presentation to SlideShare",
                60,
                null,
                Color.BLUE,
                Icon.CLOUD,
                10
            )
        ),
        10
    ),
    FOCUSED_WORK(Category.DEEP_WORK, 5, listOf()),
    JOB_INTERVIEW(Category.DEEP_WORK, 5, listOf()),
    FRIENDS_TIME(
        Category.ME_TIME,
        5,
        listOf(
            Quest.Repeating(
                "Call a friend 2 times a week",
                "Call a friend",
                30,
                null,
                Color.PURPLE,
                Icon.PHONE,
                null,
                listOf(
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.SATURDAY
                )
            ),
            Quest.Repeating(
                "Go out with friends 2 times a week",
                "Go out with friends",
                90,
                null,
                Color.PURPLE,
                Icon.FRIENDS,
                null,
                listOf(
                    DayOfWeek.TUESDAY,
                    DayOfWeek.SATURDAY
                )
            ),
            Quest.OneTime(
                "Connect with a forgotten friend",
                "Connect with a forgotten friend",
                30,
                null,
                Color.PURPLE,
                Icon.PHONE,
                3
            ),
            Quest.OneTime(
                "Plan a vacation with friends",
                "Plan a vacation with friends",
                120,
                null,
                Color.PURPLE,
                Icon.FRIENDS,
                null,
                DayOfWeek.SUNDAY
            )
        )
    ),
    ENJOY_MYSELF(Category.ME_TIME, 5, listOf()),
    FAMILY_TIME(
        Category.ME_TIME,
        5,
        listOf(
            Quest.Repeating(
                "Call my parents every week",
                "Call my parents",
                20,
                Time.atHours(20),
                Color.PURPLE,
                Icon.PHONE,
                null,
                listOf(
                    DayOfWeek.SUNDAY
                )
            ),
            Quest.OneTime(
                "Visit my parents every month",
                "Visit my parents",
                120,
                Time.atHours(10),
                Color.PURPLE,
                Icon.FRIENDS,
                null,
                DayOfWeek.SUNDAY
            ),
            Quest.Repeating(
                "Have family dinner 3 times a week",
                "Have family dinner",
                45,
                Time.atHours(20),
                Color.PINK,
                Icon.PIZZA,
                null,
                listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.SUNDAY
                )
            ),
            Quest.Repeating(
                "Play with my children every weekend",
                "Play with my children",
                60,
                Time.atHours(11),
                Color.PINK,
                Icon.FOOTBALL,
                null,
                listOf(
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
                )
            ),
            Quest.OneTime(
                "Plan family vacation",
                "Plan family vacation",
                90,
                Time.atHours(10),
                Color.PURPLE,
                Icon.FRIENDS,
                null,
                DayOfWeek.SUNDAY
            )
        )
    ),
    KEEP_THINGS_TIDY(Category.ORGANIZE_MY_LIFE, 5, listOf()),
    ORGANIZE_MY_DAY(Category.ORGANIZE_MY_LIFE, 5, listOf()),
    STAY_ON_TOP_OF_THINGS(Category.ORGANIZE_MY_LIFE, 5, listOf());

    enum class Category {
        BUILD_SKILL,
        ME_TIME,
        DEEP_WORK,
        ORGANIZE_MY_LIFE,
        HEALTH_AND_FITNESS
    }

    sealed class Quest {
        data class OneTime(
            val text: String,
            val name: String,
            val duration: Int,
            val startTime: Time? = null,
            val color: Color = Color.GREEN,
            val icon: Icon = Icon.STAR,
            val startAtDay: Int? = null,
            val preferredDayOfWeek: DayOfWeek? = null,
            val selected: Boolean = true
        ) : Quest()

        data class Repeating(
            val text: String,
            val name: String,
            val duration: Int,
            val startTime: Time? = null,
            val color: Color = Color.GREEN,
            val icon: Icon = Icon.STAR,
            val startAtDay: Int? = null,
            val weekDays: List<DayOfWeek> = DayOfWeek.values().toList(),
            val selected: Boolean = true
        ) : Quest()
    }
}

enum class AndroidPredefinedChallenge(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val smallImage: Int,
    @DrawableRes val backgroundImage: Int,
    val category: Category
) {
    STRESS_FREE_MIND(
        R.string.challenge_stress_free_mind,
        R.string.challenge_stress_free_mind_description,
        R.drawable.challenge_stress_free_mind,
        R.drawable.challenge_stress_free_mind_background,
        Category.HEALTH_AND_FITNESS
    ),
    WEIGHT_CUTTER(
        R.string.challenge_weight_cutter,
        R.string.challenge_weight_cutter_description,
        R.drawable.challenge_weight_cutter,
        R.drawable.challenge_weight_cutter_background,
        Category.HEALTH_AND_FITNESS
    ),
    HEALTHY_FIT(
        R.string.challenge_healthy_and_fit,
        R.string.challenge_healthy_and_fit_description,
        R.drawable.challenge_healthy_and_fit,
        R.drawable.challenge_healthy_and_fit_background,
        Category.HEALTH_AND_FITNESS
    ),
    ENGLISH_JEDI(
        R.string.challenge_english_jedi,
        R.string.challenge_english_jedi_description,
        R.drawable.challenge_english_jedi,
        R.drawable.challenge_english_jedi_background,
        Category.BUILD_SKILL
    ),
    CODING_NINJA(
        R.string.challenge_coding_ninja,
        R.string.challenge_coding_ninja_description,
        R.drawable.challenge_coding_ninja,
        R.drawable.challenge_coding_ninja_background,
        Category.BUILD_SKILL
    ),
    FAMOUS_WRITER(
        R.string.challenge_famous_writer,
        R.string.challenge_famous_writer_description,
        R.drawable.challenge_famous_writer,
        R.drawable.challenge_famous_writer_background,
        Category.BUILD_SKILL
    ),
    MASTER_COMMUNICATOR(
        R.string.challenge_master_communicator,
        R.string.challenge_master_communicator_description,
        R.drawable.challenge_communication_master,
        R.drawable.challenge_communication_master_background,
        Category.DEEP_WORK
    ),
    FOCUSED_WORK(
        R.string.challenge_focused_work,
        R.string.challenge_focused_work_description,
        R.drawable.challenge_focus_on_work,
        R.drawable.challenge_focus_on_work_background,
        Category.DEEP_WORK
    ),
    JOB_INTERVIEW(
        R.string.challenge_job_interview,
        R.string.challenge_job_interview_description,
        R.drawable.challenge_job_interview,
        R.drawable.challenge_job_interview_background,
        Category.DEEP_WORK
    ),
    FRIENDS_TIME(
        R.string.challenge_friends_time,
        R.string.challenge_friends_time_description,
        R.drawable.challenge_friends_time,
        R.drawable.challenge_friends_time_background,
        Category.ME_TIME
    ),
    ENJOY_MYSELF(
        R.string.challenge_enjoy_myself,
        R.string.challenge_enjoy_myself_description,
        R.drawable.challenge_enjoy_myself,
        R.drawable.challenge_enjoy_myself_background,
        Category.ME_TIME
    ),
    FAMILY_TIME(
        R.string.challenge_family_time,
        R.string.challenge_family_time_description,
        R.drawable.challenge_family_time,
        R.drawable.challenge_family_time_background,
        Category.ME_TIME
    ),
    KEEP_THINGS_TIDY(
        R.string.challenge_keep_things_tidy,
        R.string.challenge_keep_things_tidy_description,
        R.drawable.challenge_keep_things_tidy,
        R.drawable.challenge_keep_things_tidy_background,
        Category.ORGANIZE_MY_LIFE
    ),
    ORGANIZE_MY_DAY(
        R.string.challenge_organize_my_day,
        R.string.challenge_organize_my_day_description,
        R.drawable.challenge_organize_my_day,
        R.drawable.challenge_organize_my_day_background,
        Category.ORGANIZE_MY_LIFE
    ),
    STAY_ON_TOP_OF_THINGS(
        R.string.challenge_stay_on_top_of_things,
        R.string.challenge_stay_on_top_of_things_description,
        R.drawable.challenge_chores,
        R.drawable.challenge_chores_background,
        Category.ORGANIZE_MY_LIFE
    );

    enum class Category(@StringRes val title: Int, @ColorRes val color: Int) {
        BUILD_SKILL(R.string.challenge_category_build_skill_name, R.color.md_blue_500),
        ME_TIME(R.string.challenge_category_me_time_name, R.color.md_purple_500),
        DEEP_WORK(R.string.challenge_category_deep_work_name, R.color.md_red_500),
        ORGANIZE_MY_LIFE(R.string.challenge_category_organize_my_life_name, R.color.md_teal_500),
        HEALTH_AND_FITNESS(R.string.challenge_category_health_fitness_name, R.color.md_green_500)
    }
}