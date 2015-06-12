package com.curiousily.ipoli;

import com.squareup.otto.Bus;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class PoliBus {
    private static Bus instance = null;

    private PoliBus() {}

    public static Bus get() {
        if (instance == null) {
            instance = new Bus();
        }
        return instance;
    }
}