package io.ipoli.assistant.services;

import com.squareup.otto.Bus;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class LocalCommandParserService implements CommandParserService {

    private final Bus bus;

    public LocalCommandParserService(Bus bus) {
        this.bus = bus;
    }

    @Override
    public void parse(String command) {

    }
}
