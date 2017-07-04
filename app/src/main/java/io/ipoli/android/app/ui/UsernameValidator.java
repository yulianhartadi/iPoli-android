package io.ipoli.android.app.ui;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.regex.Pattern;

import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.feed.persistence.FeedPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/1/17.
 */
public class UsernameValidator {

    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 20;

    public static void validate(String username, FeedPersistenceService feedPersistenceService, ResultListener resultListener) {
        if (StringUtils.isEmpty(username)) {
            resultListener.onInvalid(UsernameValidationError.EMPTY);
            return;
        }

        if(username.length() < MIN_LENGTH || username.length() > MAX_LENGTH) {
            resultListener.onInvalid(UsernameValidationError.LENGTH);
            return;
        }

        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        if (!asciiEncoder.canEncode(username)) {
            resultListener.onInvalid(UsernameValidationError.FORMAT);
            return;
        }

        Pattern p = Pattern.compile("^\\w+$");
        if (!p.matcher(username).matches()) {
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
        EMPTY, NOT_UNIQUE, FORMAT, LENGTH;
    }
}
