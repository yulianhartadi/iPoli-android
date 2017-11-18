package io.ipoli.android.common.view

import android.support.annotation.ColorRes
import com.mikepenz.entypo_typeface_library.Entypo
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.ionicons_typeface_library.Ionicons
import io.ipoli.android.R

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/17/17.
 */
enum class AndroidIcon(val icon: IIcon, @ColorRes val color: Int) {
    HOME(Ionicons.Icon.ion_home, R.color.md_green_500),
    FRIENDS(GoogleMaterial.Icon.gmd_group, R.color.md_purple_300),
    RESTAURANT(GoogleMaterial.Icon.gmd_restaurant, R.color.md_red_500),
    PAW(Ionicons.Icon.ion_ios_paw, R.color.md_orange_500),
    BRIEFCASE(Ionicons.Icon.ion_briefcase, R.color.md_orange_500),
    BOOK(GoogleMaterial.Icon.gmd_import_contacts, R.color.md_blue_300),
    HEART(Ionicons.Icon.ion_heart, R.color.md_pink_500),
    RUN(GoogleMaterial.Icon.gmd_directions_run, R.color.md_green_500),
    MED_KIT(Ionicons.Icon.ion_medkit, R.color.md_red_500),
    TREE(Entypo.Icon.ent_tree, R.color.md_green_500),
    BEER(Ionicons.Icon.ion_beer, R.color.md_yellow_600),
    PLANE(Ionicons.Icon.ion_plane, R.color.md_purple_300),
    COMPASS(Ionicons.Icon.ion_android_compass, R.color.md_blue_300),
    LIGHT_BULB(Ionicons.Icon.ion_android_bulb, R.color.md_yellow_600),
    CAR(Ionicons.Icon.ion_android_car, R.color.md_purple_300),
    WRENCH(Ionicons.Icon.ion_wrench, R.color.md_orange_500),
    STAR(Ionicons.Icon.ion_ios_star, R.color.md_yellow_600),
    FITNESS(GoogleMaterial.Icon.gmd_fitness_center, R.color.md_green_500),
    COFFEE(Ionicons.Icon.ion_coffee, R.color.md_pink_500),
    BUS(Ionicons.Icon.ion_android_bus, R.color.md_blue_300),
    ACADEMIC(Entypo.Icon.ent_graduation_cap, R.color.md_blue_300),
    CAKE(Entypo.Icon.ent_cake, R.color.md_pink_500),
    GAME_CONTROLLER(Ionicons.Icon.ion_ios_game_controller_b, R.color.md_purple_300),
    FLASK(Ionicons.Icon.ion_erlenmeyer_flask, R.color.md_red_500),
    SHOPPING_CART(Entypo.Icon.ent_shopping_cart, R.color.md_pink_500),
    BIKE(GoogleMaterial.Icon.gmd_directions_bike, R.color.md_green_500),
    TRAIN(Ionicons.Icon.ion_android_train, R.color.md_blue_300),
    PIZZA(Ionicons.Icon.ion_pizza, R.color.md_orange_500),
    PHONE(Entypo.Icon.ent_old_phone, R.color.md_purple_300),
    CLOUD(Entypo.Icon.ent_cloud, R.color.md_blue_300),
    SUN(Ionicons.Icon.ion_ios_sunny, R.color.md_yellow_600),
    AMERICAN_FOOTBALL(Ionicons.Icon.ion_ios_americanfootball, R.color.md_red_500),
    TROPHY(Entypo.Icon.ent_trophy, R.color.md_yellow_600),
    FOOTBALL(Ionicons.Icon.ion_ios_football, R.color.md_orange_500),
    MONEY(Entypo.Icon.ent_credit, R.color.md_green_500),
    CAMERA(Ionicons.Icon.ion_ios_videocam, R.color.md_purple_500),
}