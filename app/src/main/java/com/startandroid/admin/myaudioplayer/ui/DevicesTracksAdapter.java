package com.startandroid.admin.myaudioplayer.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.startandroid.admin.myaudioplayer.R;
import com.startandroid.admin.myaudioplayer.data.AudioModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DevicesTracksAdapter extends RecyclerView.Adapter<DevicesTracksAdapter.DevicesTracksViewHolder> {

    private List<AudioModel> mTrackList;
    private TrackItemClickListener mViewItemClickListener;


    public DevicesTracksAdapter (List<AudioModel> trackList, TrackItemClickListener listener) {
        mTrackList = trackList;
        mViewItemClickListener = listener;
    }


    @NonNull
    @Override
    public DevicesTracksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_track_item, parent, false);
        return new DevicesTracksViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesTracksViewHolder holder, int position){
        holder.bind(mTrackList.get(position));
    }

    @Override
    public int getItemCount() {
        return mTrackList.size();
    }

    interface TrackItemClickListener {
        void onItemClickListener(AudioModel itemData, int viewId);
    }

    class DevicesTracksViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.track_item)
        ConstraintLayout trackCardView;
        @BindView(R.id.track_name)
        TextView mTrackName;
        @BindView(R.id.artist)
        TextView mSinger;
        @BindView(R.id.btn_track_options)
        ImageButton mBtnTrackOptions;


        DevicesTracksViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
            increaseBtnHitArea(mBtnTrackOptions, 13, 5, 13, 10);
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
               parent.setTouchDelegate(new TouchDelegate(rect, btn));
            });
        }

        private float convertDpToPixel(float dp, Context context){
            return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        }

            void bind (final AudioModel itemData){
            mTrackName.setText(itemData.getName());
            mSinger.setText(itemData.getArtist());

            trackCardView.setOnClickListener((view) ->
                    mViewItemClickListener.onItemClickListener(itemData, trackCardView.getId()));

                mBtnTrackOptions.setOnClickListener(
                        v -> {
                            PopupMenu menu = new PopupMenu(trackCardView.getContext(), v);
                            menu.inflate(R.menu.audio_track_menu);
                            menu.setOnMenuItemClickListener(menuItem -> {
                                mViewItemClickListener.onItemClickListener(itemData, menuItem.getItemId());
                                return true;
                            });
                            menu.show();
                        });
        }
    }
}
