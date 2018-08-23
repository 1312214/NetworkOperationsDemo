package com.duyhoang.networkopsdemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by rogerh on 6/21/2018.
 */

public class NetworkFragment extends Fragment {
    private static final String TAG = "NetworkFragment";
    private static final String URL_KEY = "UrlString";

    private DownloadCallback mCallBack;
    private String mUrl;
    private DownloadTask mDownloadTask;

    public static NetworkFragment getInstance(FragmentManager fm, String url){
        NetworkFragment networkFragment = (NetworkFragment) fm.findFragmentByTag(NetworkFragment.TAG);
        if(networkFragment == null){
            networkFragment = new NetworkFragment();
            Bundle bundle = new Bundle();
            bundle.putString(NetworkFragment.URL_KEY, url);
            networkFragment.setArguments(bundle);
            fm.beginTransaction().add(networkFragment, NetworkFragment.TAG).commit();
        }
        return networkFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this Fragment across configuration changes in the host Activity.
        setRetainInstance(true);
        mUrl = getArguments().getString(URL_KEY);
        Log.e(TAG, "Inside -- onCreate of NetworkFragment: url = " + mUrl);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Reference to host Activity that will handle
        Log.e(TAG, "Inside onAttach of NetworkFragment");
        mCallBack = (DownloadCallback)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBack = null;
    }


    @Override
    public void onDestroy() {
        cancelDownload();

        super.onDestroy();
    }

    ///XXXXX
    public void startDownload() {
        cancelDownload();
        mDownloadTask = new DownloadTask();
        mDownloadTask.execute(mUrl);

    }

    public void cancelDownload(){
        if(mDownloadTask != null){
            mDownloadTask.cancel(true);
            mDownloadTask = null;
        }
    }


    private class DownloadTask extends AsyncTask<String, Integer, DownloadTask.Result>{

        static final String TAG = "DownloadTask";


        class Result{
            public String strValue;
            public Exception exception;

            public Result(String value){
                strValue = value;
            }
            public Result(Exception exception){
                this.exception = exception;
            }
        }


        //xxxxxxxxx
        // If there is no connectivity to the Internet, then canceling the DownloadTask that is doing.
        @Override
        protected void onPreExecute() {
            if(mCallBack != null) {
                NetworkInfo networkInfo = mCallBack.getActiveNetworkInfo();
                if(networkInfo == null || !networkInfo.isConnected() ||
                        (networkInfo.getType() != ConnectivityManager.TYPE_WIFI && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE) ){
                    mCallBack.updateFromDownload(null);
                    cancel(true);
                    Log.e(TAG, "INSIDE -- Cancel background task by no having internet connection");
                }
            }


        }


        @Override
        protected Result doInBackground(String... strings) {
            Result rs = null;
            String strUrl = strings[0];
            if(strUrl != null && strUrl.length() > 0 && !isCancelled()){
                try {
                    Log.e(TAG,"Inside DoInBackground method");
                    URL Url = new URL(strUrl);
                    String resultString = downloadDataFromUrl(Url);
                    if(resultString != null){
                        rs = new Result(resultString);
                    } else{
                        throw new IOException("Result string is null");
                    }
                } catch (Exception e) {
                    rs = new Result(e);
                }
            }

            return rs;
        }




        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(values.length == 2){
                mCallBack.onProgressUpdate(values[0], values[1]);
            }
        }

        @Override
        protected void onPostExecute(Result result) {
            if(result != null && mCallBack != null){
                if(result.strValue != null){
                    mCallBack.updateFromDownload(result.strValue);
                }
                else if(result.exception != null){
                    mCallBack.updateFromDownload("Exception: " + result.exception.getMessage());
                }
                mCallBack.finishDownloading();
            }
        }


        private String downloadDataFromUrl(URL url) throws Exception {
            HttpsURLConnection connection = null;
            InputStream inputStream = null;
            String responseString = null;

            try {
                connection = (HttpsURLConnection)url.openConnection();
                connection.setReadTimeout(3000);
                connection.setConnectTimeout(3000);
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.connect();
                Log.e(TAG, "Inside -- downloadDataFromUrl method");
                publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS, 0);
                int responseCode = connection.getResponseCode();
                if(responseCode != HttpsURLConnection.HTTP_OK){
                    throw new IOException("HTTP error code " + responseCode);
                }

                inputStream = connection.getInputStream();
                publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
                if(inputStream != null){
                    responseString = readStream(inputStream, 10000);
                    publishProgress(DownloadCallback.Progress.PROCESS_INPUT_STREAM_SUCCESS, 0);
                }
            }
            finally {
                if(inputStream != null){
                    inputStream.close();
                }
                if(connection != null){
                    connection.disconnect();
                }
            }

            return responseString;
        }

        private String readStream(InputStream inputStream, int maxLength) throws IOException {
            Log.e(TAG, "Inside - readStream method");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
//            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
            char[] charBuf = new char[maxLength];
            int offset = 0;
            int readSize = 0;
            int percent;
            String result = null;
            //XXX readsize > maxLength
            while(offset < maxLength && offset != -1){
                offset += readSize;
                Log.e(TAG, "Inside -- the while loop of readStream: " + offset);
                percent = (offset * 100)/ maxLength;
                publishProgress(DownloadCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS, percent);
                readSize = reader.read(charBuf, offset, maxLength - offset);
                Log.e(TAG, "readSize = " + readSize);
            }

            if(offset != -1){
                int realSize = Math.min(maxLength, offset);
                result = new String(charBuf, 0, realSize);
            }

            return result;
        }

    }


}
