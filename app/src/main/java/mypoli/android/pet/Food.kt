package mypoli.android.pet

import android.support.annotation.DrawableRes
import mypoli.android.R

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 11/24/17.
 */
enum class Food(val category: Food.Category, @DrawableRes val image: Int, val price: Price) {

    BANANA(Category.FRUIT, R.drawable.food_fruit_1, Price(quantity = 6)),
    CHERRIES(Category.FRUIT, R.drawable.food_fruit_2, Price(quantity = 4)),
    APPLE(Category.FRUIT, R.drawable.food_fruit_3, Price(quantity = 5)),
    PINEAPPLE(Category.FRUIT, R.drawable.food_fruit_4, Price(quantity = 4)),

    CARROT(Category.VEGETABLE, R.drawable.food_veggie_1, Price(quantity = 6)),
    PEPPERS(Category.VEGETABLE, R.drawable.food_veggie_2, Price(quantity = 6)),
    BROCCOLI(Category.VEGETABLE, R.drawable.food_veggie_3, Price(quantity = 5)),
    SALAD(Category.VEGETABLE, R.drawable.food_veggie_4, Price(quantity = 4)),

    CHICKEN(Category.MEAT, R.drawable.food_meat_1, Price(quantity = 6)),
    SAUSAGE(Category.MEAT, R.drawable.food_meat_2, Price(quantity = 5)),
    STEAK(Category.MEAT, R.drawable.food_meat_3, Price(quantity = 4)),
    EGG(Category.MEAT, R.drawable.food_meat_4, Price(quantity = 5)),
    SALMON(Category.MEAT, R.drawable.food_meat_5, Price(quantity = 4)),

    POOP(Category.POOP, R.drawable.food_poop_1, Price(quantity = 10)),

    CANDY(Category.CANDY, R.drawable.food_candy_1, Price(quantity = 8)),
    DOUGHNUT(Category.CANDY, R.drawable.food_candy_2, Price(quantity = 6)),
    ICE_CREAM(Category.CANDY, R.drawable.food_candy_3, Price(quantity = 6)),
    CUPCAKE(Category.CANDY, R.drawable.food_candy_4, Price(quantity = 8)),

    HAMBURGER(Category.JUNK, R.drawable.food_junk_1, Price(quantity = 8)),
    PIZZA(Category.JUNK, R.drawable.food_junk_2, Price(quantity = 5)),
    FRIES(Category.JUNK, R.drawable.food_junk_3, Price(quantity = 8)),
    HOT_DOG(Category.JUNK, R.drawable.food_junk_4, Price(quantity = 6)),

    BEER(Category.BEER, R.drawable.food_beer_1, Price(quantity = 8));

    enum class Category {
        MEAT, JUNK, FRUIT, VEGETABLE, CANDY, BEER, POOP
    }

    data class Price(val gems: Int = 1, val quantity: Int)
}
