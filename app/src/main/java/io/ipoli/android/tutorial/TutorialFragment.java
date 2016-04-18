package io.ipoli.android.tutorial;


import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.ipoli.android.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialFragment extends Fragment {
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String IMAGE = "image";
    private static final String BACKGROUND = "background_color";
    private String title;
    private int image;
    private String description;
    private int backgroundColor;

    public TutorialFragment() {
    }

    public static TutorialFragment newInstance(String title, String description, @DrawableRes int image, @ColorInt int backgroundColor) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(DESCRIPTION, description);
        args.putInt(IMAGE, image);
        args.putInt(BACKGROUND, backgroundColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(TITLE);
            description = getArguments().getString(DESCRIPTION);
            image = getArguments().getInt(IMAGE);
            backgroundColor = getArguments().getInt(BACKGROUND);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tutorial, container, false);
        View containerView = v.findViewById(R.id.main);
        TextView titleView = (TextView) v.findViewById(R.id.title);
        TextView descView = (TextView) v.findViewById(R.id.description);
        ImageView imageView = (ImageView) v.findViewById(R.id.image);

        titleView.setText(title);
        descView.setText(description);
        imageView.setImageResource(image);
        containerView.setBackgroundColor(backgroundColor);

        return v;
    }
}
