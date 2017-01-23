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
import io.ipoli.android.challenge.events.NewChallengeResultsPickedEvent;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/17.
 */
public class AddChallengeResultsFragment extends BaseFragment {

    @BindView(R.id.new_challenge_result1)
    TextInputEditText result1;

    @BindView(R.id.new_challenge_result2)
    TextInputEditText result2;

    @BindView(R.id.new_challenge_result3)
    TextInputEditText result3;

    @BindView(R.id.new_challenge_results_image)
    ImageView image;


    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_challenge_results, container, false);
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
                String result1Text = result1.getText().toString();
                String result2Text = result2.getText().toString();
                String result3Text = result3.getText().toString();
                if(StringUtils.isEmpty(result1Text) && StringUtils.isEmpty(result2Text) && StringUtils.isEmpty(result3Text)) {
                    Toast.makeText(getContext(), R.string.add_challenge_expected_result, Toast.LENGTH_SHORT).show();
                } else {
                    postEvent(new NewChallengeResultsPickedEvent(result1Text, result2Text, result3Text));
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