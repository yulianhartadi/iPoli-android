package io.ipoli.android.app.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface CommandParserService {
    void parse(String command);

}
