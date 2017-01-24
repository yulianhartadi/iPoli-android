package io.ipoli.android.quest.persistence;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/29/16.
 */
public interface OnChangeListener {

    void onNew();

    void onChanged();

    void onDeleted();
}
