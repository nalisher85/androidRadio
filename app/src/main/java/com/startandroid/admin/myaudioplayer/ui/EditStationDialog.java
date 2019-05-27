package com.startandroid.admin.myaudioplayer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.startandroid.admin.myaudioplayer.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditStationDialog extends DialogFragment implements View.OnClickListener {

    @BindView(R.id.station_name_et)
    EditText mStationName;
    @BindView(R.id.station_link_et)
    EditText mStationlink;
    @BindView(R.id.cancel_btn)
    Button mCancelBtn;
    @BindView(R.id.save_btn)
    Button mSaveBtn;

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     *
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle(R.string.edit_station_dialog_title);
        View v = getLayoutInflater().inflate(R.layout.edit_station_dialog, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @OnClick({R.id.cancel_btn, R.id.save_btn})
    @Override
    public void onClick(View v) {

    }
}
