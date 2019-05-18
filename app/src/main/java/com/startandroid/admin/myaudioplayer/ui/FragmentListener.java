package com.startandroid.admin.myaudioplayer.ui;

import android.view.View;

import com.startandroid.admin.myaudioplayer.data.AudioModel;
import com.startandroid.admin.myaudioplayer.data.RadioStationModel;

import java.util.List;

public interface FragmentListener {
    void onAddQueueItems(List<AudioModel> audioList, boolean isNew);
    void onAddQueueItems(RadioStationModel station);

}
