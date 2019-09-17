package com.startandroid.admin.myaudioplayer.data.remotesource;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.startandroid.admin.myaudioplayer.data.model.RadioStation;

import java.util.List;

public class Response {

    @SerializedName("result")
    @Expose
    private List<RadioStation> result;

    public Response(List<RadioStation> result) {
        this.result = result;
    }

    public List<RadioStation> getResult() {
        return result;
    }
}
