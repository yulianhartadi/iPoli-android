package io.ipoli.android.app.utils;

import java.util.UUID;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/3/16.
 */
public class IDGenerator {

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
