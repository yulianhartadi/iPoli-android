package io.ipoli.android.quest.ui.menus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.ipoli.android.R;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/14/16.
 */
public class DuplicateQuestItemsHelper {

    @NonNull
    public static Map<Integer, DuplicateDateItem> createDuplicateDateMap(Context context, MenuItem duplicateItem) {
        List<DuplicateDateItem> duplicateDateItems = getDuplicateDates(context);

        Map<Integer, DuplicateDateItem> itemIdToDate = new HashMap<>();
        for (DuplicateDateItem item : duplicateDateItems) {
            int id = new Random().nextInt();
            itemIdToDate.put(id, item);
            duplicateItem.getSubMenu().add(Menu.NONE, id, Menu.NONE, item.title);
        }
        return itemIdToDate;
    }

    @NonNull
    private static List<DuplicateDateItem> getDuplicateDates(Context context) {
        List<DuplicateDateItem> duplicateDateItems = new ArrayList<>();
        duplicateDateItems.add(new DuplicateDateItem(context.getString(R.string.today), new Date()));
        duplicateDateItems.add(new DuplicateDateItem(context.getString(R.string.tomorrow), DateUtils.getTomorrow()));
        duplicateDateItems.add(new DuplicateDateItem(context.getString(R.string.pick_date), null));
        return duplicateDateItems;
    }
}
