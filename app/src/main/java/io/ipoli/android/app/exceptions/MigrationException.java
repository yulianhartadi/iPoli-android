package io.ipoli.android.app.exceptions;

import com.google.firebase.database.DatabaseException;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/3/17.
 */
public class MigrationException extends RuntimeException {
    public MigrationException(String message, DatabaseException cause) {
        super(message, cause);
    }
}
