package io.ipoli.android.quest.ui.menus;

import java.util.Date;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/14/16.
 */
public class SnoozeTimeItem {
    public String title;
    public int minutes;
    public Date date;
    public boolean pickTime;
    public boolean pickDate;

    public SnoozeTimeItem(String title, int minutes) {
        this.title = title;
        this.minutes = minutes;
        this.date = null;
    }

    public SnoozeTimeItem(String title, Date date) {
        this.title = title;
        this.date = date;
        this.minutes = -1;
    }

    public SnoozeTimeItem(String title) {
        this.title = title;
    }
}
