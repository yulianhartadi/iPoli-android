package io.ipoli.android.app.ui;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.utils.ViewUtils;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/27/16.
 */
public class FabMenuView extends RelativeLayout {
    private Unbinder unbinder;

    @BindView(R.id.fab_menu_container)
    ViewGroup container;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.fab1)
    FloatingActionButton fab1;

    @BindView(R.id.fab2)
    FloatingActionButton fab2;

    @BindView(R.id.fab3)
    FloatingActionButton fab3;

    @BindView(R.id.fab4)
    FloatingActionButton fab4;

    @BindView(R.id.fab_label)
    TextView fabLabel;

    @BindView(R.id.fab1_label)
    TextView fab1Label;

    @BindView(R.id.fab2_label)
    TextView fab2Label;

    @BindView(R.id.fab3_label)
    TextView fab3Label;

    @BindView(R.id.fab4_label)
    TextView fab4Label;

    private Animation fabOpen;
    private Animation fabClose;
    private Animation rotateForward;
    private Animation rotateBackward;

    public FabMenuView(Context context) {
        super(context);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    public FabMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    private void initUI(Context context) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.layout_fab_menu, this);
        unbinder = ButterKnife.bind(this, view);

        setElevation(ViewUtils.dpToPx(5, getResources()));

        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

    }

    @OnClick(R.id.fab)
    public void onFabCLick(View view) {
        open();
    }

    public void open() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        container.setClickable(true);
        container.setVisibility(VISIBLE);
        container.setAlpha(1);
        openAnimation();
    }

    private void openAnimation() {
        rotateForward.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fab.setImageResource(R.drawable.ic_done_white_24dp);
                fab.setRotation(-45);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fab.startAnimation(rotateForward);
        fabLabel.startAnimation(fabOpen);
        fab1.startAnimation(fabOpen);
        fab1Label.startAnimation(fabOpen);
        fab2.startAnimation(fabOpen);
        fab2Label.startAnimation(fabOpen);
        fab3.startAnimation(fabOpen);
        fab3Label.startAnimation(fabOpen);
        fab4.startAnimation(fabOpen);
        fab4Label.startAnimation(fabOpen);
    }

    private void close() {
        container.setClickable(false);
        rotateBackward.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                fab.setImageResource(R.drawable.ic_add_white_24dp);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fabClose.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                container.animate().alpha(0).setDuration(getResources().getInteger(
                        android.R.integer.config_shortAnimTime)).start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fab.startAnimation(rotateBackward);
        fab4.startAnimation(fabClose);
        fab4Label.startAnimation(fabClose);
        fab3.startAnimation(fabClose);
        fab3Label.startAnimation(fabClose);
        fab2Label.startAnimation(fabClose);
        fab2.startAnimation(fabClose);
        fab1Label.startAnimation(fabClose);
        fab1.startAnimation(fabClose);
        fabLabel.startAnimation(fabClose);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_BACK) {
            close();
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();
        super.onDetachedFromWindow();
    }
}
