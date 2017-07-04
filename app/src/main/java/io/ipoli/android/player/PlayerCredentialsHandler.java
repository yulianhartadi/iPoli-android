package io.ipoli.android.player;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.ipoli.android.R;
import io.ipoli.android.app.activities.SignInActivity;
import io.ipoli.android.app.ui.ThemedSnackbar;
import io.ipoli.android.feed.data.Profile;
import io.ipoli.android.feed.persistence.FeedPersistenceService;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.player.ui.dialogs.UsernamePickerFragment;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/2/17.
 */
public class PlayerCredentialsHandler {

    private final PlayerPersistenceService playerPersistenceService;

    private final FeedPersistenceService feedPersistenceService;

    public enum Action {
        SHARE_QUEST, GIVE_KUDOS, ADD_QUEST, FOLLOW_PLAYER
    }

    public PlayerCredentialsHandler(PlayerPersistenceService playerPersistenceService, FeedPersistenceService feedPersistenceService) {
        this.playerPersistenceService = playerPersistenceService;
        this.feedPersistenceService = feedPersistenceService;
    }

    public void authorizeAccess(Player player, CredentialStatus credentialStatus, Action action, AppCompatActivity context,
                                View signInRootView) {

        if (credentialStatus == CredentialStatus.AUTHORIZED) {
            return;
        }

        if (credentialStatus == CredentialStatus.GUEST) {
            int messageRes;
            switch (action) {
                case SHARE_QUEST:
                    messageRes = R.string.sign_in_to_post_message;
                    break;
                case GIVE_KUDOS:
                    messageRes = R.string.sign_in_to_give_kudos_message;
                    break;
                case ADD_QUEST:
                    messageRes = R.string.sign_in_to_add_post_as_quest_message;
                    break;
                default:
                    messageRes = R.string.sign_in_to_follow_message;
                    break;
            }

            Snackbar snackbar = ThemedSnackbar.make(signInRootView, messageRes, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.sign_in_button, view -> context.startActivity(new Intent(context, SignInActivity.class)));
            snackbar.show();
            return;
        }

        if (credentialStatus == CredentialStatus.NO_USERNAME) {
            UsernamePickerFragment.newInstance(username -> {
                player.setUsername(username);
                playerPersistenceService.save(player);
                feedPersistenceService.createProfile(new Profile(player));
            }).show(context.getSupportFragmentManager());
        }
    }
}