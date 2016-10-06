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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.R;

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

    private Animation fabOpen;
    private Animation fabClose;
    private Animation rotateForward;
    private Animation rotateBackward;
    private Animation fabClose1;
    private boolean isOpen = false;

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


        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fabClose1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
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
        isOpen = true;
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
        fab1.startAnimation(fabOpen);
        fab2.startAnimation(fabOpen);
        fab1.setClickable(true);
        fab2.setClickable(true);
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
        fabClose1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                container.animate().alpha(0).setDuration(100).start();
//                container.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fab.startAnimation(rotateBackward);
        fab2.startAnimation(fabClose);
        fab1.startAnimation(fabClose1);
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
