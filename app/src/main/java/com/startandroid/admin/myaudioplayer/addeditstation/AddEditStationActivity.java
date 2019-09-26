package com.startandroid.admin.myaudioplayer.addeditstation;

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
import com.startandroid.admin.myaudioplayer.data.RadioStationSource;
import com.startandroid.admin.myaudioplayer.data.localsource.RadioStationLocalDataSource;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import java.util.List;
import java.util.Objects;

public class AddEditStationActivity extends AppCompatActivity implements Validator.ValidationListener,
        AddEditStationContract.View {

    public static final String STATION_ID_KEY = "station_id";


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

    private RadioStation mStation;
    private Validator mValidator;
    private AddEditStationContract.Presenter mPresenter;


    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_station);
        ButterKnife.bind(this);

        mValidator = new Validator(this);
        mValidator.setValidationListener(this);
        mValidator.registerAdapter(TextInputLayout.class, new TextInputLayoutAdapter());

        String stationId = getIntent().getStringExtra(STATION_ID_KEY);
        RadioStationSource repository = RadioStationLocalDataSource.getInstance();
        mPresenter = new AddEditStationPresenter(stationId, repository, this);

        mStationNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mValidator.validate();
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
                mValidator.validate();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @OnClick({R.id.cancel_btn, R.id.save_btn})
    void onClick(View v) {
        if (v.getId() == mSaveBtn.getId()){

            String name = mStationNameEt.getText().toString();
            String link = mStationLinkEt.getText().toString();
            mPresenter.saveStation(name, link);

        } else {
            finishView();
        }
    }

    @Override
    public void showStationName(String name) {
        mStationNameEt.setText(name);
    }

    @Override
    public void showStationLink(String link) {
        mStationLinkEt.setText(link);
    }

    @Override
    public void showAddToolbarTitle() {
        mToolbar.setTitle(R.string.add_edit_activity_title_add);
    }

    @Override
    public void showEditToolbarTitle() {
        mToolbar.setTitle(R.string.add_edit_activity_title_edit);
    }

    @Override
    public void setStationNameErr(boolean enabled) {
        mStationNameTxtIL.setErrorEnabled(enabled);
    }

    @Override
    public void setStationLinkErr(boolean enabled) {
        mStationLinkTxtIL.setErrorEnabled(enabled);
    }

    @Override
    public void finishView() {
        mPresenter.onDestroy();
        finish();
    }

    @Override
    public void onValidationSucceeded() {
        mPresenter.validationSucceeded();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        mPresenter.validationFailed();

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

    class TextInputLayoutAdapter implements ViewDataAdapter<TextInputLayout, String> {

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


