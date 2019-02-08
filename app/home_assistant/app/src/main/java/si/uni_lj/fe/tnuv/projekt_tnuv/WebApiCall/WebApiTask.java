package si.uni_lj.fe.tnuv.projekt_tnuv.WebApiCall;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebApiTask extends AsyncTask<String, Void, String> {

    private static final String DEBUG_TAG = "DownloadWebpageTask";
    private WebApiCallback caller;

    //constructor - allows async task to send progress updates to caller
    public WebApiTask(WebApiCallback caller) {
        this.caller = caller;
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            URL request_url = new URL(urls[0]);

            HttpURLConnection urlConnection = (HttpURLConnection) request_url.openConnection();

            //request json response from the web api
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoInput(true);
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            Log.d("JURE", "Server response code: " + Integer.toString(responseCode));

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            }

            finally {
                urlConnection.disconnect();
            }
        } catch(IOException e) {
            Log.e("JURE", "WebApiTask error: malformed URL.");
            return "error malformed url";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        //return the result to the caller
        caller.updateFromDownload(result);
    }



}
