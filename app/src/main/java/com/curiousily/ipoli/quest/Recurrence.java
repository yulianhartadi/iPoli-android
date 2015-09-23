package com.curiousily.ipoli.quest;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/23/15.
 */
public class Recurrence {
    public int interval = 1;
    public Frequency frequency;
    public int frequencyMultiplier = 1;
    public int timesPerDay = 1;
    public Set<String> includedDays = new HashSet<>();
    public Set<String> excludedDays = new HashSet<>();

    public enum Frequency {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

}
