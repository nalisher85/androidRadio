package com.startandroid.admin.myaudioplayer.ui;

import com.startandroid.admin.myaudioplayer.data.AudioModel;
import com.startandroid.admin.myaudioplayer.data.RadioStationModel;

import java.util.List;

public interface FragmentListener {
    void onAddQueueItems(List<AudioModel> audioList, boolean cleanOldList);
    void onAddQueueItem(AudioModel audioTrack, boolean cleanOldList);
    void onAddQueueItem(RadioStationModel station);

}
