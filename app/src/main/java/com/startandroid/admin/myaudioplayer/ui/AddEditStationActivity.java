package com.startandroid.admin.myaudioplayer.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.adapter.ViewDataAdapter;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Url;
import com.mobsandgeeks.saripaar.exception.ConversionException;
import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.MyDbHelper;
import com.startandroid.admin.myaudioplayer.data.RadioStationModel;

import java.util.List;
import java.util.Objects;

public class AddEditStationActivity extends AppCompatActivity implements Validator.ValidationListener {

    public static final int EDIT_MODE = 1;
    public static final int ADD_MODE = 2;

    public static final String ACTIVITY_MODE_KEY = "mode";
    public static final String STATION_KEY = "station";


    @BindView(R.id.station_name_et)
    EditText mStationNameEt;
    @NotEmpty(emptyText = "пусто", messageResId = R.string.empty_err_msg)
    @BindView(R.id.station_name_txtIL)
    TextInputLayout mStationNameTxtIL;

    @BindView(R.id.station_link_et)
    EditText mStationLinkEt;
    @Url(messageResId = R.string.invalid_url_err_msg)
    @BindView(R.id.station_link_txtIL)
    TextInputLayout mStationLinkTxtIL;

    @BindView(R.id.save_btn)
    Button mSaveBtn;
    @BindView(R.id.cancel_btn)
    Button mCancelBtn;
    @BindView(R.id.add_edit_activity_toolbar)
    Toolbar mToolbar;

    private RadioStationModel mStation;
    private int mActivityMode;
    private MyDbHelper myDb;
    private Validator validator;
    private boolean isDataValid;


    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_station2);
        ButterKnife.bind(this);
        myDb = new MyDbHelper(this.getApplicationContext());

        validator = new Validator(this);
        validator.setValidationListener(this);
        validator.registerAdapter(TextInputLayout.class, new TextInputlayoutAdapter());

        String title;
        mActivityMode = getIntent().getIntExtra(ACTIVITY_MODE_KEY, ADD_MODE);
        if (mActivityMode == EDIT_MODE) {
            isDataValid = true;
            title = getResources().getString(R.string.add_edit_activity_title_edit);
            mStation = (RadioStationModel)getIntent().getSerializableExtra(STATION_KEY);
            mStationNameEt.setText(mStation.getStationName());
            mStationLinkEt.setText(mStation.getPath());
        } else {
            isDataValid = false;
            title = getResources().getString(R.string.add_edit_activity_title_add);
        }
        mToolbar.setTitle(title);

        mStationNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validator.validate();
            }
        });

        mStationLinkEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validator.validate();
            }
        });


    }

    @OnClick({R.id.cancel_btn, R.id.save_btn})
    void onClick(View v) {
        if (v.getId() == mSaveBtn.getId()){
            if (isDataValid){
                if (mActivityMode == EDIT_MODE && mStation != null) {
                    updateStation();
                } else if (mActivityMode == ADD_MODE) {
                    addNewStation();
                }
            } else {

            }
        } else {
            finish();
        }
    }

    @Override
    public void onValidationSucceeded() {
        isDataValid = true;
        mStationNameTxtIL.setErrorEnabled(false);
        mStationLinkTxtIL.setErrorEnabled(false);
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        isDataValid = false;
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof TextInputLayout) {
                ((TextInputLayout) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("CheckResult")
    private void updateStation(){
        String name = mStationNameEt.getText().toString();
        String url = mStationLinkEt.getText().toString();
        mStation.setStationName(name);
        mStation.setPath(url);
        myDb.update(mStation).subscribe(
                () -> {
                    Toast.makeText(this, R.string.success_edit_toast, Toast.LENGTH_SHORT).show();
                    finish();
                },
                throwable -> {
                    Toast.makeText(this, R.string.failure_edit_toast, Toast.LENGTH_SHORT).show();
                    throwable.printStackTrace();
                    finish();
                });
    }

    @SuppressLint("CheckResult")
    private void addNewStation(){
        RadioStationModel station = new RadioStationModel();
        station.setStationName( mStationNameEt.getText().toString());
        station.setPath(mStationLinkEt.getText().toString());
        station.setFavorite(false);

        myDb.insert(station).subscribe(
                () -> {
                    Toast.makeText(this, R.string.success_add_toast, Toast.LENGTH_SHORT).show();
                    finish();
                },
                throwable -> {
                    Toast.makeText(this, R.string.failure_add_toast, Toast.LENGTH_SHORT).show();
                    throwable.printStackTrace();
                    finish();
                });
    }

    class TextInputlayoutAdapter implements ViewDataAdapter<TextInputLayout, String> {

        /**
         * Extract and return the appropriate data from a given {@link View}.
         *
         * @param view The {@link View} from which contains the data that we are
         *             interested in.
         * @return The interested data.
         * @throws ConversionException If the adapter is unable to convert the data to the expected
         *                             data type.
         */
        @Override
        public String getData(TextInputLayout view) throws ConversionException {
            return getText(view);
        }

        private String getText(TextInputLayout til) {
            return Objects.requireNonNull(til.getEditText()).getText().toString();
        }

    }
}


