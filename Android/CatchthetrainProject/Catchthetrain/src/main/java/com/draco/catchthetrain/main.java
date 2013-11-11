package com.draco.catchthetrain;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Minutes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by suresh on 04/11/13.
 */
public class main extends Activity {


    private static final String TAG = main.class.getSimpleName();
    private EditText from;
    private EditText to;
    private Button button;
    private SimpleDateFormat format;
    private ScrollTextView scrolltext;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        scrolltext = (ScrollTextView) findViewById(R.id.MarqueeText);

        scrolltext.setTextColor(Color.BLACK);

        from = (EditText) findViewById(R.id.from);
        to = (EditText) findViewById(R.id.to);
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                savePreferences("origin", from.getText().toString());
                savePreferences("destination", to.getText().toString());
                initiateRequest();
            }
        });
        format = new SimpleDateFormat("HH:mm");

        loadSavedPreferences();
        initiateRequest();
    }

    private void initiateRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream source = retrieveStream(getUrl());
                Log.d("URL", getUrl());
                final Gson gson = new Gson();

                Reader reader = new InputStreamReader(source);

                final Response response = gson.fromJson(reader, Response.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            scrolltext.setText(getBanner(response.getTrainTimes()));
                            scrolltext.startScroll();
                            String trainTime = response.getTime();
                            String currentTime = getCurrentTime();
                            Log.v(TAG, trainTime);
                            ((TextView) findViewById(R.id.textView)).setText(getClosestTrainTime(response.getTrainTimes()));
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
        }).start();
    }

    private String getBanner(List<TrainTime> trainTimeList) {
        StringBuilder sb = new StringBuilder();
        for (TrainTime trainTime : trainTimeList) {
            sb.append(trainTime.trainDestination);
            sb.append(" : ");
            sb.append(trainTime.time);
            sb.append(" ---- ");
        }
        return sb.toString();
    }

    private String getUrl() {

        String url = "https://api.trafiklab.se/sl/reseplanerare.json";
        //?S=hornstull&Z=Norsborg&Time=13%3A35&Timesel=depart&Lang=en&key=40c778bc76e71f0720b147f851326440";

        StringBuilder sb = new StringBuilder();
        sb.append(url);
        sb.append("?S=");

        try {
            ;
            sb.append(URLEncoder.encode(from.getText().toString(), "UTF-8"));
            sb.append("&Z=");
            sb.append(URLEncoder.encode(to.getText().toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sb.append("&Time=");
        String getTime = getCurrentTime();
        sb.append(getTime);
        sb.append("&Timesel=depart&Lang=en&key=40c778bc76e71f0720b147f851326440");
        return sb.toString();
    }

    private String getCurrentTime() {
        String str = format.format(new Date());
        return str;
    }

    private String getTimeDifference(Date dateStart, Date dateStop) {
        try {
            DateTime dateStartTime = new DateTime(dateStart);
            DateTime dateStopTime = new DateTime(dateStop);
            int minutes = Minutes.minutesBetween(dateStartTime, dateStopTime).getMinutes() % 60;
            String difference = "";
            if (minutes > 0) {
                difference = String.valueOf(minutes);
            } else {
                return "";
            }
            return difference + " Minutes";
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return "";
    }

    private InputStream retrieveStream(String url) {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet getRequest = new HttpGet(url);
        try {
            HttpResponse getResponse = client.execute(getRequest);
            final int statusCode = getResponse.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.w(getClass().getSimpleName(),
                        "Error " + statusCode + " for URL " + url);
                return null;
            }

            HttpEntity getResponseEntity = getResponse.getEntity();
            return getResponseEntity.getContent();

        } catch (IOException e) {
            getRequest.abort();
            Log.e(TAG, "Error for URL " + url, e);
        }

        return null;

    }

    private void loadSavedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        String origin = sharedPreferences.getString("origin", "norsborg");
        String destination = sharedPreferences.getString("destination", "slussen");
        from.setText(origin);
        to.setText(destination);
    }

    private void savePreferences(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private String getClosestTrainTime(List<TrainTime> trainTimeList) {
        Date serverTime, currentTime;
        for (TrainTime trainTime : trainTimeList) {

            try {
                serverTime = format.parse(trainTime.time);
                currentTime = format.parse(getCurrentTime());
                String difference = getTimeDifference(currentTime, serverTime);
                if (TextUtils.isEmpty(difference))
                    continue;
                return difference;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}