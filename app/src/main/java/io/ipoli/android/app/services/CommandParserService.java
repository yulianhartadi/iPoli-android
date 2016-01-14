package io.ipoli.android.app.services;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface CommandParserService {
    boolean parse(String command);
    boolean parse(String command, Command validCommand);
    boolean parse(String command, Command[] validCommands);

}
