package io.ipoli.android.app.share;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/29/16.
 */
public class ShareDialogAdapter extends ArrayAdapter<ShareApp> {


    public ShareDialogAdapter(Context context, List<ShareApp> apps) {
        super(context, R.layout.share_dialog_item, apps);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ShareApp app = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.share_dialog_item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.app_name);
        name.setText(app.name);

        ImageView icon = (ImageView) convertView.findViewById(R.id.app_icon);
        icon.setImageDrawable(app.icon);

        return convertView;
    }
}
