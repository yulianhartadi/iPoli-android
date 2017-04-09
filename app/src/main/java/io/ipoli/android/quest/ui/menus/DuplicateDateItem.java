package io.ipoli.android.quest.ui.menus;

import org.threeten.bp.LocalDate;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/14/16.
 */
public class DuplicateDateItem {
    public String title;
    public LocalDate date;

    public DuplicateDateItem(String title, LocalDate date) {
        this.title = title;
        this.date = date;
    }
}
