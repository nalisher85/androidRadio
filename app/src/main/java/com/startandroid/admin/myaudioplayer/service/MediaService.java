package com.startandroid.admin.myaudioplayer.service;

import android.app.Notification;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.support.v4.media.MediaBrowserCompat;
import androidx.media.MediaBrowserServiceCompat;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.startandroid.admin.myaudioplayer.data.MyDbHelper;
import com.startandroid.admin.myaudioplayer.data.StorageAudioFiles;
import com.startandroid.admin.myaudioplayer.service.notifications.MediaNotificationManager;
import com.startandroid.admin.myaudioplayer.service.players.MediaPlayerAdapter;


import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class MediaService extends MediaBrowserServiceCompat {

    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final String LOG_TAG = MediaService.class.getSimpleName();

    public static final String USER_ACTION_CLEAR_PLAY_LIST = "clear_play_list";
    public static final String KEY_QUEUE_INDEX = "queue_index";


    private final List<MediaSessionCompat.QueueItem> mPlayList = new ArrayList<>();
    private final List<MediaSessionCompat.QueueItem> mShufflePlayList = new ArrayList<>();
    private int mQueueIndex = -1;


    private MediaSessionCompat mMediaSession;
    private PlayerAdapter mMediaPayer;
    private MediaNotificationManager mMediaNotificationManager;
    private boolean mServiceInStartedState;

    private StorageAudioFiles storageAudioFiles;
    private Disposable audioMetadataFromStorageSubscribtion;

    private int mShuffleMode = PlaybackStateCompat.SHUFFLE_MODE_NONE;
    private int mRepeatMode = PlaybackStateCompat.REPEAT_MODE_NONE;



    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("myLog1", "MediaService->onCreate");
        storageAudioFiles = new StorageAudioFiles(this);
        mMediaSession = new MediaSessionCompat(this, LOG_TAG);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionToken(mMediaSession.getSessionToken());
        mMediaSession.setCallback(new MediaSessionCallback());

        mMediaNotificationManager = new MediaNotificationManager(this);

        mMediaPayer = new MediaPlayerAdapter(this, new MediaPlayerListener());

    }

    @Override
    public void onDestroy() {
        Log.d("myLog1", "MediaService->onDestroy");
        mMediaNotificationManager.onDestroy();
        mMediaPayer.stop();
        mMediaSession.release();
        Log.d(LOG_TAG, "onDestroy: MediaPlayerAdapter stopped, and MediaSession released");
    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String s, int i, @Nullable Bundle bundle) {
        Log.d("myLog1", "MediaService->onGetRoot");
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(@NonNull String s, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d("myLog1", "MediaService->onLoadChildren");

        if (!mPlayList.isEmpty()) {
            List<MediaBrowserCompat.MediaItem> mItems = new ArrayList<>();

            for (MediaSessionCompat.QueueItem qItem : mPlayList){
                MediaBrowserCompat.MediaItem mItem =
                        new MediaBrowserCompat.MediaItem(qItem.getDescription(),
                                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
                mItems.add(mItem);
            }
            result.sendResult(mItems);

        } else {
            result.sendResult(null);
        }
    }

    private List<MediaSessionCompat.QueueItem> getPlayList(){
        return mShuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL
                ? mShufflePlayList
                : mPlayList;
    }

    private void addPlayList (MediaSessionCompat.QueueItem item) {
        mPlayList.add(item);

        if(mShuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL)
            mShufflePlayList.add(item);
    }

    private void removePlayListItem (MediaDescriptionCompat item)  {

        if (mShuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {

           for (int i = 0; i < mPlayList.size(); i++) {
               int removeSuccess = 0;

               String id = mPlayList.get(i).getDescription().getMediaId();
               if (Objects.equals(item.getMediaId(), id)){
                   mPlayList.remove(mPlayList.get(i));
                   removeSuccess += 1;
               }

               id = mShufflePlayList.get(i).getDescription().getMediaId();
               if (Objects.equals(item.getMediaId(), id)) {
                   mShufflePlayList.remove(mShufflePlayList.get(i));
                   removeSuccess += 1;
               }
                if (removeSuccess == 2) break;
           }

        } else {

            for (MediaSessionCompat.QueueItem i : mPlayList) {
                if (Objects.equals(item.getMediaId(), i.getDescription().getMediaId())){
                    mPlayList.remove(i);
                    break;
                }
            }
        }
    }

    private void setExtras(@NonNull String key){
        Bundle bundle = new Bundle();

        switch (key){
            case KEY_QUEUE_INDEX:
                bundle.putInt(key, mQueueIndex);
                break;
        }

        mMediaSession.setExtras(bundle);
    }




    private int playListIndexOf(MediaDescriptionCompat media) {
        List<MediaSessionCompat.QueueItem> playList = getPlayList();
        int index = -1;

        for (MediaSessionCompat.QueueItem item : playList){

            if (item.getDescription().getMediaId().equals(media.getMediaId())) {
                index = playList.indexOf(item);
                break;
            }
        }
        return index;
    }

    private void clearPlayList(){
        mPlayList.clear();
        mShufflePlayList.clear();
        mQueueIndex = -1;
    }


    //-----------------------

    public class MediaSessionCallback extends MediaSessionCompat.Callback {
        private MediaMetadataCompat mPreparedMedia;
        private Disposable mediaMetadataSubscription;

        boolean playOnPreparedMedia;

        public MediaSessionCallback() {
            super();
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            mRepeatMode = repeatMode;
            mMediaSession.setRepeatMode(repeatMode);
        }

        /**
         * Override to handle the setting of the shuffle mode.
         * <p>
         * You should call {link setShuffleMode} before the end of this method in order to
         * notify the change to the {link MediaControllerCompat}, or
         * {link MediaControllerCompat#getShuffleMode} could return an invalid value.
         *
         * @param shuffleMode The shuffle mode which is one of followings:
         *                    {@link PlaybackStateCompat#SHUFFLE_MODE_NONE},
         *                    {@link PlaybackStateCompat#SHUFFLE_MODE_ALL},
         *                    {@link PlaybackStateCompat#SHUFFLE_MODE_GROUP}
         */
        @Override
        public void onSetShuffleMode(int shuffleMode) {

            if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {

                mShufflePlayList.addAll(mPlayList);
                Collections.shuffle(mShufflePlayList);
                mMediaSession.setQueue(mShufflePlayList);

            } else {
                mShufflePlayList.clear();
                mMediaSession.setQueue(mPlayList);
            }

            mShuffleMode = shuffleMode;
            mMediaSession.setShuffleMode(shuffleMode);
        }

        @Override
        public void onSkipToNext() {
           mQueueIndex = mQueueIndex >= (getPlayList().size() - 1) ? 0 : ++mQueueIndex;
           setExtras(KEY_QUEUE_INDEX);
            mPreparedMedia = null;
            onPrepare();
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
            mQueueIndex = mQueueIndex <= 0 ? getPlayList().size() - 1 : --mQueueIndex;
            setExtras(KEY_QUEUE_INDEX);
            mPreparedMedia = null;
            onPrepare();
            onPlay();
        }

        @Override
        public void onSeekTo (long pos) {
            mMediaPayer.seekTo(pos);
        }


        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            addPlayList(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mQueueIndex == -1) ? 0 : mQueueIndex;
            setExtras(KEY_QUEUE_INDEX);
            mMediaSession.setQueue(getPlayList());
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {

            if (mQueueIndex == playListIndexOf(description)) {

                if (getPlayList().size() <= 1) {
                    onStop();
                    mQueueIndex = -1;
                    setExtras(KEY_QUEUE_INDEX);
                    clearPlayList();
                }
                else {
                    removePlayListItem(description);
                    mMediaSession.setQueue(getPlayList());
                    mQueueIndex = mQueueIndex >= getPlayList().size() ? 0 : mQueueIndex;
                    setExtras(KEY_QUEUE_INDEX);
                    onPrepare();

                    if (mMediaSession.getController().getPlaybackState().getState()
                            == PlaybackStateCompat.STATE_PLAYING)
                        onPlay();
                }

            } else {
                mQueueIndex = mQueueIndex > playListIndexOf(description) ? --mQueueIndex : mQueueIndex;
                setExtras(KEY_QUEUE_INDEX);
                removePlayListItem(description);
                mMediaSession.setQueue(getPlayList());
            }
        }

        @Override
        public void onPrepare() {
            if (mQueueIndex < 0 && mPlayList.isEmpty()){
                return;
            }

            MediaDescriptionCompat mediaDescription = getPlayList().get(mQueueIndex).getDescription();
            final String mediaId = mediaDescription.getMediaId();
            Uri uri = mediaDescription.getMediaUri();

            if(uri != null && !Objects.equals(uri.getScheme(), "http")) {
                audioMetadataFromStorageSubscribtion = storageAudioFiles.getAudioMetadataByIdAsync(mediaId)
                        .subscribe(metadata -> {
                            mPreparedMedia = metadata;
                            mMediaPayer.setCurrentMedia(mPreparedMedia);
                            mMediaSession.setMetadata(mPreparedMedia);
                            if(playOnPreparedMedia) onPlay();
                            playOnPreparedMedia = false;
                        }, err -> err.printStackTrace());

            } else {
                mediaMetadataSubscription = new MyDbHelper(MediaService.this.getApplicationContext())
                        .getRadioStationById(Integer.parseInt(mediaId))
                        .subscribe(
                                radioStationModel -> {
                                    mPreparedMedia = radioStationModel.convertToMetadata();
                                    mMediaPayer.setCurrentMedia(mPreparedMedia);
                                    mMediaSession.setMetadata(mPreparedMedia);
                                    mRepeatMode = PlaybackStateCompat.REPEAT_MODE_ONE;
                                    if(playOnPreparedMedia) onPlay();
                                    playOnPreparedMedia = false;
                                }
                        );
            }
            if(!mMediaSession.isActive()) mMediaSession.setActive(true);
        }

        @Override
        public void onPlay() {

            if (!isReadyToPlay()) {
                playOnPreparedMedia = true;
                return;
            }

            Uri currentMediaUri = getPlayList().get(mQueueIndex).getDescription().getMediaUri();
            mMediaPayer.playFromUri(currentMediaUri);
        }

        @Override
        public void onPause() {
            mMediaPayer.pause();
        }

        @Override
        public void onStop() {
            mMediaPayer.stop();
            mMediaSession.setActive(false);
            audioMetadataFromStorageSubscribtion.dispose();
        }

        @Override
        public void onSkipToQueueItem(long id) {

            for (MediaSessionCompat.QueueItem item : getPlayList()){

                if (item.getQueueId() == id) {
                    mQueueIndex = getPlayList().indexOf(item);
                    setExtras(KEY_QUEUE_INDEX);
                    onPrepare();
                    onPlay();
                }
            }
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            if (action.equals(USER_ACTION_CLEAR_PLAY_LIST)) clearPlayList();
        }



        private boolean isReadyToPlay() {
            return !mPlayList.isEmpty() && mPreparedMedia != null;
        }
    }


    // MediaPlayerAdapter Callback: MediaPlayerAdapter state -> MusicService.
    public class MediaPlayerListener extends PlaybackInfoListener {

        private final ServiceManager mServiceManager;

        MediaPlayerListener() {
            mServiceManager = new ServiceManager();
        }

        @Override
        public void onPlaybackCompleted() {

            switch (mRepeatMode) {
                case PlaybackStateCompat.REPEAT_MODE_ALL:
                    mMediaSession.getController().getTransportControls().skipToNext();
                    break;
                case PlaybackStateCompat.REPEAT_MODE_ONE:
                    mMediaSession.getController().getTransportControls().play();
                    break;
                case PlaybackStateCompat.REPEAT_MODE_NONE:
                    if (mQueueIndex < (mPlayList.size()-1))
                        mMediaSession.getController().getTransportControls().skipToNext();
                    break;
            }

        }

        @Override
        synchronized public void onPlaybackStateChange(PlaybackStateCompat state) {

            mMediaSession.setPlaybackState(state);

            // Manage the started state of this service.
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mServiceManager.moveServiceToStartedState(state);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    mServiceManager.updateNotificationForPause(state);
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    mServiceManager.moveServiceOutOfStartedState(state);
                    break;
            }
        }

        class ServiceManager {

            private void moveServiceToStartedState(PlaybackStateCompat state) {
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mMediaPayer.getCurrentMedia(), state, getSessionToken());

                if (!mServiceInStartedState) {
                    ContextCompat.startForegroundService(
                            MediaService.this,
                            new Intent(MediaService.this, MediaService.class));
                    mServiceInStartedState = true;
                }
                startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
            }

            private void updateNotificationForPause(PlaybackStateCompat state) {
                stopForeground(false);
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mMediaPayer.getCurrentMedia(), state, getSessionToken());
                mMediaNotificationManager.getNotificationManager()
                        .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
            }

            private void moveServiceOutOfStartedState(PlaybackStateCompat state) {
                stopForeground(true);
                stopSelf();
                mServiceInStartedState = false;
            }
        }
    }
}
