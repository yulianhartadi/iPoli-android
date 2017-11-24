package io.ipoli.android.pet

import android.support.annotation.DrawableRes
import io.ipoli.android.R

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 11/24/17.
 */
enum class Food(val category: Food.Category, @DrawableRes val image: Int, val price: Int = 10) {
    BANANA(Category.FRUIT, R.drawable.food_fruit_1),
    CHERRIES(Category.FRUIT, R.drawable.food_fruit_2),
    APPLE(Category.FRUIT, R.drawable.food_fruit_3),
    PINEAPPLE(Category.FRUIT, R.drawable.food_fruit_4),
    CARROT(Category.VEGETABLE, R.drawable.food_veggie_1),
    PEPPERS(Category.VEGETABLE, R.drawable.food_veggie_2),
    BROCCOLI(Category.VEGETABLE, R.drawable.food_veggie_3),
    SALAD(Category.VEGETABLE, R.drawable.food_veggie_4),
    CHICKEN(Category.MEAT, R.drawable.food_meat_1),
    SAUSAGE(Category.MEAT, R.drawable.food_meat_2),
    STEAK(Category.MEAT, R.drawable.food_meat_3),
    EGG(Category.MEAT, R.drawable.food_meat_4),
    SALMON(Category.MEAT, R.drawable.food_meat_5),
    POOP(Category.POOP, R.drawable.food_poop_1),
    CANDY(Category.CANDY, R.drawable.food_candy_1),
    DOUGHNUT(Category.CANDY, R.drawable.food_candy_2),
    ICE_CREAM(Category.CANDY, R.drawable.food_candy_3),
    CUPCAKE(Category.CANDY, R.drawable.food_candy_4),
    HAMBURGER(Category.JUNK, R.drawable.food_junk_1),
    PIZZA(Category.JUNK, R.drawable.food_junk_2),
    FRIES(Category.JUNK, R.drawable.food_junk_3),
    HOT_DOG(Category.JUNK, R.drawable.food_junk_4),
    BEER(Category.BEER, R.drawable.food_beer_1);

    enum class Category {
        MEAT, JUNK, FRUIT, VEGETABLE, CANDY, BEER, POOP
    }
}
