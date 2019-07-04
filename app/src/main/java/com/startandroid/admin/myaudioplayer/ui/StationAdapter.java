package com.startandroid.admin.myaudioplayer.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.RadioStationModel;
import com.startandroid.admin.myaudioplayer.util.EditorViewHitArea;
import com.startandroid.admin.myaudioplayer.util.TouchDelegateComposite;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import pl.droidsonroids.gif.GifImageView;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationViewHolder> {

    private List<RadioStationModel> mStationList = new ArrayList<>();
    private OnItemViewClickListener mRecyclerItemClickListener;

    public StationAdapter(OnItemViewClickListener listener) {
        mRecyclerItemClickListener = listener;
    }


    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.radio_station_item, parent, false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        holder.bind(mStationList.get(position));
    }

    @Override
    public int getItemCount() {
        return mStationList.size();
    }

    public void setStationList(List<RadioStationModel> stationList) {
        mStationList.clear();
        mStationList.addAll(stationList);
    }

    interface OnItemViewClickListener {
        void onItemClicked(RadioStationModel itemData, int viewId);
    }

    class StationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.station_item)
        ConstraintLayout mStationCardView;
        @BindView(R.id.station_icon)
        ImageView mStationIcon;
        @BindView(R.id.station_name)
        TextView mStationName;
        @BindView(R.id.btn_station_options)
        ImageButton mStationOptionsBtn;
        @BindView(R.id.btn_favorite)
        ToggleButton mBtnFavorite;

        private EditorViewHitArea editorViewHitArea;

        StationViewHolder (View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            editorViewHitArea = new EditorViewHitArea(new TouchDelegateComposite(itemView.getContext()));
            editorViewHitArea.increaseViewHitArea(mBtnFavorite, 13, 5, 13, 5);
            editorViewHitArea.increaseViewHitArea(mStationOptionsBtn, 13, 5, 13, 5);
        }

        void bind (final RadioStationModel itemData) {
            mStationName.setText(itemData.getStationName());

            mStationCardView.setOnClickListener(v ->
                    mRecyclerItemClickListener.onItemClicked(itemData, v.getId()));

            mBtnFavorite.setChecked(itemData.isFavorite());
            mBtnFavorite.setOnClickListener(v -> {
                itemData.setFavorite(!itemData.isFavorite());
                mRecyclerItemClickListener.onItemClicked(itemData, v.getId());
            });

            mStationOptionsBtn.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(mStationCardView.getContext(), v);
                menu.inflate(R.menu.station_item_menu);
                menu.setOnMenuItemClickListener(item -> {
                    mRecyclerItemClickListener.onItemClicked(itemData, item.getItemId());
                    return true;
                });
                menu.show();
            });
        }

    }
}



