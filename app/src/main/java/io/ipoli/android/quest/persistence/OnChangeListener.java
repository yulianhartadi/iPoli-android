package io.ipoli.android.quest.persistence;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/29/16.
 */
public interface OnChangeListener<T> {

    void onNew(T data);

    void onChanged(T data);

    void onDeleted();
}
