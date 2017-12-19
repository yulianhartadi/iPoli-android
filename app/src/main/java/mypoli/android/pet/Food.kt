package mypoli.android.pet

import android.support.annotation.DrawableRes
import mypoli.android.R

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 11/24/17.
 */
enum class Food(val category: Food.Category, @DrawableRes val image: Int, val price: Int = 10) {

    BANANA(Category.FRUIT, R.drawable.food_fruit_1, 15),
    CHERRIES(Category.FRUIT, R.drawable.food_fruit_2, 25),
    APPLE(Category.FRUIT, R.drawable.food_fruit_3, 20),
    PINEAPPLE(Category.FRUIT, R.drawable.food_fruit_4, 25),

    CARROT(Category.VEGETABLE, R.drawable.food_veggie_1, 15),
    PEPPERS(Category.VEGETABLE, R.drawable.food_veggie_2, 15),
    BROCCOLI(Category.VEGETABLE, R.drawable.food_veggie_3, 20),
    SALAD(Category.VEGETABLE, R.drawable.food_veggie_4, 25),

    CHICKEN(Category.MEAT, R.drawable.food_meat_1, 15),
    SAUSAGE(Category.MEAT, R.drawable.food_meat_2, 20),
    STEAK(Category.MEAT, R.drawable.food_meat_3, 25),
    EGG(Category.MEAT, R.drawable.food_meat_4, 20),
    SALMON(Category.MEAT, R.drawable.food_meat_5, 25),

    POOP(Category.POOP, R.drawable.food_poop_1, 5),

    CANDY(Category.CANDY, R.drawable.food_candy_1, 10),
    DOUGHNUT(Category.CANDY, R.drawable.food_candy_2, 15),
    ICE_CREAM(Category.CANDY, R.drawable.food_candy_3, 15),
    CUPCAKE(Category.CANDY, R.drawable.food_candy_4, 10),

    HAMBURGER(Category.JUNK, R.drawable.food_junk_1, 10),
    PIZZA(Category.JUNK, R.drawable.food_junk_2, 20),
    FRIES(Category.JUNK, R.drawable.food_junk_3, 10),
    HOT_DOG(Category.JUNK, R.drawable.food_junk_4, 15),

    BEER(Category.BEER, R.drawable.food_beer_1, 10);

    enum class Category {
        MEAT, JUNK, FRUIT, VEGETABLE, CANDY, BEER, POOP
    }
}
