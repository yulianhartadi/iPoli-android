package io.ipoli.android.challenge.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.challenge.events.NewChallengeReasonsPickedEvent;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/17.
 */
public class AddChallengeReasonsFragment extends BaseFragment {

    @BindView(R.id.new_challenge_reason1)
    TextInputEditText reason1;

    @BindView(R.id.new_challenge_reason2)
    TextInputEditText reason2;

    @BindView(R.id.new_challenge_reason3)
    TextInputEditText reason3;

    @BindView(R.id.new_challenge_reasons_image)
    ImageView image;


    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_challenge_reasons, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_challenge_wizard_next_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next:
                String reason1Text = reason1.getText().toString();
                String reason2Text = reason2.getText().toString();
                String reason3Text = reason3.getText().toString();
                if(StringUtils.isEmpty(reason1Text) && StringUtils.isEmpty(reason2Text) && StringUtils.isEmpty(reason3Text)) {
                    Toast.makeText(getContext(), R.string.add_challenge_reason, Toast.LENGTH_SHORT).show();
                } else {
                    postEvent(new NewChallengeReasonsPickedEvent(reason1Text, reason2Text, reason3Text));
                    KeyboardUtils.hideKeyboard(getActivity());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    public void setCategory(Category category) {
        switch (category) {
            case LEARNING:
                image.setImageResource(R.drawable.new_learning_quest);
                break;
            case WELLNESS:
                image.setImageResource(R.drawable.new_wellness_quest);
                break;
            case WORK:
                image.setImageResource(R.drawable.new_work_quest);
                break;
            case PERSONAL:
                image.setImageResource(R.drawable.new_personal_quest);
                break;
            case FUN:
                image.setImageResource(R.drawable.new_fun_quest);
                break;
            case CHORES:
                image.setImageResource(R.drawable.new_chores_quest);
                break;
        }
    }
}