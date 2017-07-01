package io.ipoli.android.app.ui;

import java.util.regex.Pattern;

import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.feed.persistence.FeedPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/1/17.
 */
public class UsernameValidator {

    public static void validate(String username, FeedPersistenceService feedPersistenceService, ResultListener resultListener) {
        if (StringUtils.isEmpty(username)) {
            resultListener.onInvalid(UsernameValidationError.EMPTY);
            return;
        }

        Pattern p = Pattern.compile("^\\w+$", Pattern.UNICODE_CHARACTER_CLASS);
        if(!p.matcher(username).matches()) {
            resultListener.onInvalid(UsernameValidationError.FORMAT);
            return;
        }

        feedPersistenceService.isUsernameAvailable(username, isAvailable -> {
            if (!isAvailable) {
                resultListener.onInvalid(UsernameValidationError.NOT_UNIQUE);
                return;
            }
            resultListener.onValid();
        });
    }

    public interface ResultListener {
        void onValid();
        void onInvalid(UsernameValidationError error);
    }

    public enum UsernameValidationError {
        EMPTY, NOT_UNIQUE, FORMAT;
    }
}
