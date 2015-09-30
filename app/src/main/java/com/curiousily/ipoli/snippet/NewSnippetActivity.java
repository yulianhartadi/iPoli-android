package com.curiousily.ipoli.snippet;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.app.BaseActivity;
import com.curiousily.ipoli.app.services.events.CompleteInputEvent;
import com.curiousily.ipoli.app.services.events.PartialInputEvent;
import com.curiousily.ipoli.app.services.events.ReadyForSpeechInputEvent;
import com.curiousily.ipoli.app.services.events.RequestSpeechInputEvent;
import com.curiousily.ipoli.snippet.events.CreateSnippetEvent;
import com.curiousily.ipoli.snippet.services.events.SnippetCreatedEvent;
import com.curiousily.ipoli.user.User;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/8/15.
 */
public class NewSnippetActivity extends BaseActivity {

    @Bind(R.id.snippet_input_text)
    TextView textInput;

    @Bind(R.id.snippet_input_voice)
    TextView voiceInput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_snippet);
        setTitleColor(getResources().getColor(R.color.md_dark_text_87));
        ButterKnife.bind(this);
    }

    @OnClick(R.id.snippet_mic_button)
    public void onMicButton(View view) {
        EventBus.post(new RequestSpeechInputEvent());
    }

    @Subscribe
    public void onReadyForSpeechInput(ReadyForSpeechInputEvent e) {
        textInput.setVisibility(View.GONE);
        voiceInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, textInput.getTextSize());
        voiceInput.setVisibility(View.VISIBLE);
        voiceInput.setHint(getString(R.string.speak_hint));
    }

    @Subscribe
    public void onPartialInput(PartialInputEvent e) {
        voiceInput.setText(e.input);
    }

    @Subscribe
    public void onCompleteInput(CompleteInputEvent e) {
        voiceInput.setVisibility(View.GONE);
        textInput.setText(e.input);
        textInput.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.snippet_save)
    public void onSave(View view) {
        Snippet snippet = new Snippet();
        snippet.text = textInput.getText().toString();
        snippet.createdBy = User.getCurrent(this);
        EventBus.post(new CreateSnippetEvent(snippet));
    }

    @OnClick(R.id.snippet_cancel)
    public void onCancel(View view) {
        finish();
    }

    @Subscribe
    public void onSnippetCreated(SnippetCreatedEvent e) {
        Toast.makeText(this, R.string.toast_snippet_created, Toast.LENGTH_LONG).show();
        finish();
    }
}
