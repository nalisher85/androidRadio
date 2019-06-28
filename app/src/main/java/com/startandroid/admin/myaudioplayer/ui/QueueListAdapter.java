package com.startandroid.admin.myaudioplayer.ui;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.startandroid.admin.myaudioplayer.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class QueueListAdapter extends RecyclerView.Adapter<QueueListAdapter.QueueItemViewHolder> {

    private List<QueueItem> mQueueList;
    private QueueItemClickListener mQueueItemClickListener;
    private int mPlayingPosition = -1;
    private boolean mIsPlaying = false;

    public QueueListAdapter(QueueItemClickListener listener) {
        mQueueItemClickListener = listener;
    }


    @NonNull
    @Override
    public QueueItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_track_item, parent, false);

        return new QueueItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueItemViewHolder holder, int position) {
        holder.bind(mQueueList.get(position));
        holder.isPlaying(position == mPlayingPosition && mIsPlaying);
    }

    @Override
    public int getItemCount() {
        return mQueueList.size();
    }

    public void setPlayingPosition(int position){
        mPlayingPosition = position;
    }

    public void setQueueList(List<QueueItem> list){
        mQueueList = list;
    }

    public void setPlaying(boolean isPlaying){
        mIsPlaying = isPlaying;
    }


    //--------------------------------------------------------------------

    public interface QueueItemClickListener {
        void onItemClickListener(QueueItem itemData, int viewId);
    }

    class QueueItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.track_item)
        ConstraintLayout trackItem;
        @BindView(R.id.track_item_icon)
        ImageView mTrackIcon;
        @BindView(R.id.track_name)
        TextView mTrackName;
        @BindView(R.id.artist)
        TextView mSinger;
        @BindView(R.id.btn_track_options)
        ImageButton mBtnTrackOptions;

        QueueItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(QueueItem item) {
            MediaDescriptionCompat description = item.getDescription();
            mTrackName.setText(description.getTitle());
            mSinger.setText(description.getSubtitle());

            trackItem.setOnClickListener((view) ->
                    mQueueItemClickListener.onItemClickListener(item, trackItem.getId()));

            mBtnTrackOptions.setOnClickListener(
                    v -> showQueueItemMenu(v, item));

        }

        void isPlaying(boolean isPlaying) {
            if (isPlaying) mTrackIcon.setImageResource(R.drawable.ic_playing);
            else mTrackIcon.setImageResource(R.drawable.ic_audiotrack);
        }

        void showQueueItemMenu(View v, QueueItem item){

            PopupMenu menu = new PopupMenu(trackItem.getContext(), v);
            menu.inflate(R.menu.queue_item_menu);

            menu.setOnMenuItemClickListener(menuItem -> {
                mQueueItemClickListener.onItemClickListener(item, menuItem.getItemId());
                return true;
            });
            menu.show();
        }
    }
}
