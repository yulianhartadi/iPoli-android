package io.ipoli.android.app.exceptions;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/5/17.
 */
public class PetNotFoundException extends RuntimeException {
    public PetNotFoundException(String playerId, String source) {
        super(String.format("Pet for player %s was not found in %s", playerId, source));
    }
}
