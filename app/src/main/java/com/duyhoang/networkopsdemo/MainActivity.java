package com.duyhoang.networkopsdemo;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DownloadCallback, View.OnClickListener {


    /**
     * The sequence of events in the code so far is as follows:

     The Activity starts a NetworkFragment and passes in a specified URL.
     When a user action triggers the Activity's downloadData() method, the NetworkFragment executes the DownloadTask.
     The AsyncTask method onPreExecute() runs first (on the UI thread) and cancels the task if the device is not connected to the Internet.
     The AsyncTask method doInBackground() then runs on the background thread and calls the downloadUrl() method.
     The downloadUrl() method takes a URL string as a parameter and uses an HttpsURLConnection object to fetch the web content as an InputStream.
     The InputStream is passed to the readStream() method, which converts the stream to a string.
     Finally, once the background work is complete, the AsyncTask's onPostExecute() method runs on the UI thread and uses the DownloadCallback to send the result back to the UI as a String.
     */
    private static final String TAG = "MainActivity";
    private static final String Url = "https://www.google.com";


    TextView txtInfo;
    private NetworkFragment mNetworkFrag;
    private boolean mIsDownloading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIsDownloading = false;
        txtInfo = findViewById(R.id.text_content);
        findViewById(R.id.button_start).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);
        mNetworkFrag = NetworkFragment.getInstance(getSupportFragmentManager(), Url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_action_fetch:
                startDownload();
                return true;
            case R.id.item_action_clear:
                finishDownloading();
                txtInfo.setText("");
                return true;
        }
        return false;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_start: startDownload();
                break;
            case R.id.button_cancel: finishDownloading(); txtInfo.setText("");
                Toast.makeText(this, "Downloading has stopped!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void startDownload(){
        if(!mIsDownloading && mNetworkFrag != null){
            mNetworkFrag.startDownload();
            mIsDownloading = true;
        }
    }





    @Override
    public void updateFromDownload(String result) {
        if(result != null)
            txtInfo.append("\n" + result);
        else
            txtInfo.append("\n" + R.string.error_notification);
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch (progressCode){
            case Progress.CONNECT_SUCCESS:
//                Toast.makeText(this, "connecting success", Toast.LENGTH_SHORT).show();
                txtInfo.append("\nconnecting success");
                break;
            case Progress.ERROR:
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
//                Toast.makeText(this, "Get input stream successfully", Toast.LENGTH_SHORT).show();
                txtInfo.append("\nGet input stream successfully");
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                txtInfo.append("\nComplete " + percentComplete + "%.....");
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
        }

    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void finishDownloading() {
        mIsDownloading = false;
        if(mNetworkFrag != null)
            mNetworkFrag.cancelDownload();
    }


}
