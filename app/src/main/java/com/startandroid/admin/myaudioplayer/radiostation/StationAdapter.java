package com.startandroid.admin.myaudioplayer.radiostation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;
import com.startandroid.admin.myaudioplayer.util.EditorViewHitArea;
import com.startandroid.admin.myaudioplayer.util.TouchDelegateComposite;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationViewHolder> {

    private List<RadioStation> mStationList = new ArrayList<>();
    private OnItemViewClickListener mItemClickListener;

    public StationAdapter(OnItemViewClickListener listener) {
        mItemClickListener = listener;
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

    public void setStationList(List<RadioStation> stationList) {
        mStationList.clear();
        mStationList.addAll(stationList);
    }

    interface OnItemViewClickListener {
        void onItemClicked(RadioStation itemData, int viewId);
    }

    class StationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.station_item)
        LinearLayout mStationItem;
        @BindView(R.id.station_icon)
        ImageView mStationIcon;
        @BindView(R.id.station_name)
        TextView mStationName;
        @BindView(R.id.btn_station_options)
        ImageButton mStationOptionsBtn;
        @BindView(R.id.btn_favorite)
        ToggleButton mBtnFavorite;

        StationViewHolder (View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            TouchDelegateComposite stationItemTouchDelegate = new TouchDelegateComposite(itemView.getContext());
            EditorViewHitArea.increaseViewHitAreaPost(mBtnFavorite, stationItemTouchDelegate,
                    13, 5, 13, 5);
            EditorViewHitArea.increaseViewHitAreaPost(mStationOptionsBtn, stationItemTouchDelegate,
                    13, 5, 13, 5);
        }

        void bind (final RadioStation itemData) {
            mStationName.setText(itemData.getStationName());

            mStationItem.setOnClickListener(v ->
                    mItemClickListener.onItemClicked(itemData, v.getId()));

            mBtnFavorite.setChecked(itemData.isFavorite());
            mBtnFavorite.setOnClickListener(v -> {
                itemData.setFavorite(!itemData.isFavorite());
                mItemClickListener.onItemClicked(itemData, v.getId());
            });

            mStationOptionsBtn.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(mStationItem.getContext(), v);
                menu.inflate(R.menu.station_item_menu);
                menu.setOnMenuItemClickListener(item -> {
                    mItemClickListener.onItemClicked(itemData, item.getItemId());
                    return true;
                });
                menu.show();
            });
        }

    }
}



