package com.startandroid.admin.myaudioplayer.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.RadioStationModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationViewHolder> {

    private List<RadioStationModel> mStationList;
    private OnItemViewClickListener mRecyclerItemClickListener;

    public StationAdapter(List<RadioStationModel> stationList, OnItemViewClickListener listener) {
        mRecyclerItemClickListener = listener;
        mStationList = stationList;
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

    interface OnItemViewClickListener {
        void onItemClickListener(RadioStationModel itemData, int viewId);
    }

    class StationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.station_cardview)
        CardView mStationCardView;
        @BindView(R.id.station_icon)
        ImageView mStationIcon;
        @BindView(R.id.station_name)
        TextView mStationName;
        @BindView(R.id.btn_station_options)
        ImageButton mStationOptionsBtn;
        @BindView(R.id.btn_favorite)
        ToggleButton mBtnFavorite;

        private TouchDelegateComposite touchDelegate;

        StationViewHolder (View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            touchDelegate = new TouchDelegateComposite(itemView);
            increaseBtnHitArea(mBtnFavorite, 13, 5, 13, 5);
            increaseBtnHitArea(mStationOptionsBtn, 13, 5, 13, 5);
        }

        private void increaseBtnHitArea(@NonNull final View btn, int top, int left, int bottom, int right) {
            View parent = (View) btn.getParent();

            parent.post(() -> {
               final Rect rect = new Rect();
               btn.getHitRect(rect);
               rect.top -= convertDpToPixel(top, btn.getContext());
               rect.left -= convertDpToPixel(left, btn.getContext());
               rect.bottom += convertDpToPixel(bottom, btn.getContext());
               rect.right += convertDpToPixel(right, btn.getContext());
               touchDelegate.addDelegate(new TouchDelegate(rect, btn));
               parent.setTouchDelegate(touchDelegate);
            });
        }

        private float convertDpToPixel(float dp, Context context){
            return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        }

        void bind (final RadioStationModel itemData) {
            mStationName.setText(itemData.getStationName());

            mStationCardView.setOnClickListener(v ->
                    mRecyclerItemClickListener.onItemClickListener(itemData, v.getId()));

            mBtnFavorite.setChecked(itemData.isFavorite());
            mBtnFavorite.setOnClickListener(v -> {
                itemData.setFavorite(!itemData.isFavorite());
                mRecyclerItemClickListener.onItemClickListener(itemData, v.getId());
            });

            mStationOptionsBtn.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(mStationCardView.getContext(), v);
                menu.inflate(R.menu.station_item_menu);
                menu.setOnMenuItemClickListener(item -> {
                    mRecyclerItemClickListener.onItemClickListener(itemData, item.getItemId());
                    return true;
                });
                menu.show();
            });
        }

    }
}



