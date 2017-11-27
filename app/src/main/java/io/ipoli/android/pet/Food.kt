package io.ipoli.android.pet

import android.support.annotation.DrawableRes
import io.ipoli.android.R

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 11/24/17.
 */
enum class Food(val category: Food.Category, @DrawableRes val image: Int, val price: Int = 10) {
    BANANA(Category.FRUIT, R.drawable.food_fruit_1, 20),
    CHERRIES(Category.FRUIT, R.drawable.food_fruit_2, 20),
    APPLE(Category.FRUIT, R.drawable.food_fruit_3, 20),
    PINEAPPLE(Category.FRUIT, R.drawable.food_fruit_4, 20),
    CARROT(Category.VEGETABLE, R.drawable.food_veggie_1, 20),
    PEPPERS(Category.VEGETABLE, R.drawable.food_veggie_2, 20),
    BROCCOLI(Category.VEGETABLE, R.drawable.food_veggie_3, 20),
    SALAD(Category.VEGETABLE, R.drawable.food_veggie_4, 20),
    CHICKEN(Category.MEAT, R.drawable.food_meat_1, 15),
    SAUSAGE(Category.MEAT, R.drawable.food_meat_2, 15),
    STEAK(Category.MEAT, R.drawable.food_meat_3, 15),
    EGG(Category.MEAT, R.drawable.food_meat_4, 10),
    SALMON(Category.MEAT, R.drawable.food_meat_5, 15),
    POOP(Category.POOP, R.drawable.food_poop_1, 5),
    CANDY(Category.CANDY, R.drawable.food_candy_1, 10),
    DOUGHNUT(Category.CANDY, R.drawable.food_candy_2, 10),
    ICE_CREAM(Category.CANDY, R.drawable.food_candy_3, 10),
    CUPCAKE(Category.CANDY, R.drawable.food_candy_4, 10),
    HAMBURGER(Category.JUNK, R.drawable.food_junk_1, 15),
    PIZZA(Category.JUNK, R.drawable.food_junk_2, 15),
    FRIES(Category.JUNK, R.drawable.food_junk_3, 15),
    HOT_DOG(Category.JUNK, R.drawable.food_junk_4, 15),
    BEER(Category.BEER, R.drawable.food_beer_1, 5);

    enum class Category {
        MEAT, JUNK, FRUIT, VEGETABLE, CANDY, BEER, POOP
    }
}
