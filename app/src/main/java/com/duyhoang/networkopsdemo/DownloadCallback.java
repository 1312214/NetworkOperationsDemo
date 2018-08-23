package com.duyhoang.networkopsdemo;

import android.net.NetworkInfo;

/**
 * Created by rogerh on 6/21/2018.
 */

public interface DownloadCallback {
    public interface Progress{
        int ERROR = -1;
        int CONNECT_SUCCESS = 0;
        int GET_INPUT_STREAM_SUCCESS = 1;
        int PROCESS_INPUT_STREAM_IN_PROGRESS = 2;
        int PROCESS_INPUT_STREAM_SUCCESS = 3;
    }


    public void updateFromDownload(String result);

    public void onProgressUpdate(int progressCode, int percentComplete);

    public NetworkInfo getActiveNetworkInfo();

    public void finishDownloading();

}
