package com.startandroid.admin.myaudioplayer.main;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.startandroid.admin.myaudioplayer.BasePresenter;

import java.util.List;

public interface MainActivityContract {

    interface View {

        void showBottomSheet();

        void destroyBottomSheet();

        void showAudioBottomSheet();

        void showRadioBottomSheet();

        void clearBottomSheet();

        void showAudioBShQueueList();

        void showBShMiddleContainerWithImg();

        void showAudioBShPeekTitle(String title);

        void showAudioBShPeekSubtitle(String subtitle);

        void showRadioBShPeekTitle(String title);

        void showAudioBShProgressDurationTime(String duration);

        void setAudioPlayBtnStatus(boolean isPlaying);

        void setRadioPlayBtnMode(PlayMode mode);

        void setShuffleBtnMode(int mode);

        void setRepeatBtnMode(int mode);

        boolean isBottomSheetInitialized();

        void expandBottomSheet();

        void collapseBottomSheet();

        void setMediaSeekBarNewPlaybackState(PlaybackStateCompat state);

        void setMediaSeekBarNewMetadata(MediaMetadataCompat metadata);

        void clearABShMiddleContainer();

        void showQueueListButton();

        void changeQueueAdapterData(List<MediaSessionCompat.QueueItem> list);

        void changeQueueAdapterData(int position);

        void changeQueueAdapterData(boolean isPlaying);

        void setAudioTrackAsRingtone(MediaDescriptionCompat audioTrack);
    }

    interface Presenter extends BasePresenter {

        void stop();

        void connectMediaBrowser();

        void disconnectMediaBrowser();

        boolean isMediaTypeChanged();

        void playPause();

        void skipToPrevious();

        void skipToNext();

        void skipToQueueItem(int position);

        void setRepeatMode(int repeatMode);

        void setShuffleMode(int shuffleMode);

        void removeQueueItem(MediaDescriptionCompat item);

        void seekMusicTo(int position);

        void deleteRadioStation(String id);

        void deleteCurrantRadioStation();

        void deleteMusic(String id);

        void deleteCurrantMusic();

        void deleteCurrantMediaFromQueue();

        void setCurrantMusicAsRingTone();

        void subscribeMediaSeekBar();

        void subscribeAudioBottomSheet();

        void subscribeRadioBottomSheet();

        void switchAudioBottomSheetMiddle();

        void switchOffAudioBottomSheetPlayList();
    }
}
