package io.ipoli.android.player.exceptions;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/6/17.
 */

public class GrowthException extends Exception {
    public GrowthException(String message, Exception e) {
        super(message, e);
    }
}
