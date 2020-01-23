package dam.androidjavierjuanuceda.u5t9httpclient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private final static String URL_GEONAMES = "http://api.geonames.org/wikipediaSearchJSON";
    private final static String USER_NAME = "javier22012020";
    private final static int ROWS = 10;

    private final static String URL_WEATHER = "http://api.openweathermap.org/data/2.5/weather";
    private final static String APP_ID = "180c7086dd244c23a1e89f2a69c4c77f";
    private static MainActivity mainActivity;
    private EditText etPlaceName;
    private Button btSearch;
    private ListView lvSearchResult;
    private ArrayList<GeonamesPlace> listSearchResult;

    private GetHttpDataTask getHttpDataTask;
    private GetHttpWeatherTask getHttpWeatherTask;

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUI();
        mainActivity = this;
    }

    private void setUI() {
        etPlaceName = findViewById(R.id.etPlaceName);
        btSearch = findViewById(R.id.btSearch);
        btSearch.setOnClickListener(this);

        listSearchResult = new ArrayList<>();

        lvSearchResult = findViewById(R.id.lvSearchResult);
        lvSearchResult.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listSearchResult));
        lvSearchResult.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (isNetworkAvailable()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            String place = etPlaceName.getText().toString();
            if (!place.isEmpty()) {
                URL url;
                try {
                    url = new URL(URL_GEONAMES + "?q=" + place + "&maxRows=" + ROWS + "&lang=es&username=" + USER_NAME);
                    getHttpDataTask = new GetHttpDataTask(lvSearchResult);
                    getHttpDataTask.execute(url);
                } catch (MalformedURLException e) {
                    Log.i("URL", e.getMessage());
                }
            } else Toast.makeText(this, "Write a place to search", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, "Sorry, network is not available", Toast.LENGTH_SHORT).show();

    }

    public boolean isNetworkAvailable() {
        boolean networkAvailable = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                networkAvailable = true;
            }
        }

        return networkAvailable;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (getHttpDataTask != null) {
            getHttpDataTask.cancel(true);
            Log.e("onDestroy()", "ASYNCTASK was cancelled");
        } else {
            Log.e("onDestroy()", "ASYNCTASK = NULL, was not cancelled");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GeonamesPlace geonamesPlace = listSearchResult.get(position);

        URL url;
        try {
            url = new URL(URL_WEATHER + "?lat=" + geonamesPlace.getLatitute() + "&lon=" + geonamesPlace.getLatitute() + "&APPID=" + APP_ID);
            getHttpWeatherTask = new GetHttpWeatherTask(geonamesPlace);
            getHttpWeatherTask.execute(url);

        } catch (MalformedURLException e) {
            Log.i("URL", e.getMessage());
        }

    }

    private static class GetHttpWeatherTask extends AsyncTask<URL, Void, String> {

        private final int CONNECTION_TIMEOUT = 1000;
        private final int READ_TIMEOUT = 1000;
        private GeonamesPlace geonamesPlace;
        private String search_Result_geonames;

        public GetHttpWeatherTask(GeonamesPlace geonamesPlace) {
            this.geonamesPlace = geonamesPlace;
            search_Result_geonames = "";
        }


        @SuppressLint("WrongThread")
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(URL... urls) {
            getMainActivity().etPlaceName.setInputType(InputType.TYPE_NULL);
            HttpURLConnection urlConnection = null;
            String searchResult = "";

            try {
                urlConnection = (HttpURLConnection) urls[0].openConnection();
                urlConnection.setRequestProperty("Connection", "close");
                urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
                urlConnection.setReadTimeout(READ_TIMEOUT);

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String resultStream = readStream(urlConnection.getInputStream());
                    JSONObject json = new JSONObject(resultStream);
                    searchResult += "Weather conditions for {" + String.format("%.2f", geonamesPlace.getLatitute()) + ", " + String.format("%.2f", geonamesPlace.getLongitude()) + "}" + System.lineSeparator();
                    JSONObject item = new JSONObject(json.getString("main"));
                    searchResult += "TEMP : " + String.format("%.0f", (item.getDouble("temp") - 273.15)) + " C" + System.lineSeparator() + "HUMIDITY : " + item.getString("humidity") + "%" + System.lineSeparator();
                    JSONArray jArray = json.getJSONArray("weather");
                    item = jArray.getJSONObject(0);
                    searchResult += item.get("description");
                    search_Result_geonames = searchResult;
                } else Log.i("URL", "ErrorCode: " + urlConnection.getResponseCode());


            } catch (IOException e) {

                Log.i("IOException", e.getMessage());
            } catch (JSONException e) {
                Log.i("JSONException", e.getMessage());
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }

            return searchResult;
        }

        public String getSearch_Result_geonames() {
            return search_Result_geonames;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private String readStream(InputStream inputStream) {
            StringBuilder sb = new StringBuilder();

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String nextLine;
                while ((nextLine = reader.readLine()) != null) {
                    sb.append(nextLine);
                }

            } catch (IOException e) {
                Log.i("IO-Exception-ReadStream", e.getMessage());
            }

            return sb.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(MainActivity.getMainActivity().getApplicationContext(), getSearch_Result_geonames(), Toast.LENGTH_SHORT).show();
            getMainActivity().etPlaceName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.e("onCancelled()", "ASYNCTASK: I've been cancelled and ready to GC clean");
        }
    }

    private static class GetHttpDataTask extends AsyncTask<URL, Void, ArrayList<GeonamesPlace>> {

        private final int CONNECTION_TIMEOUT = 1000;
        private final int READ_TIMEOUT = 1000;
        private final WeakReference<ListView> listViewWeakReference;

        public GetHttpDataTask(ListView listView) {
            this.listViewWeakReference = new WeakReference<>(listView);
        }


        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected ArrayList<GeonamesPlace> doInBackground(URL... urls) {

            HttpURLConnection urlConnection = null;
            ArrayList<GeonamesPlace> searchResult = new ArrayList<>();

            try {
                urlConnection = (HttpURLConnection) urls[0].openConnection();
                urlConnection.setRequestProperty("Connection", "close");
                urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
                urlConnection.setReadTimeout(READ_TIMEOUT);

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String resultStream = readStream(urlConnection.getInputStream());
                    JSONObject json = new JSONObject(resultStream);
                    JSONArray jArray = json.getJSONArray("geonames");

                    if (jArray.length() > 0) {

                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject item = jArray.getJSONObject(i);
                            searchResult.add(new GeonamesPlace(item.getString("summary"), item.getDouble("lat"), item.getDouble("lng")));
                            if (isCancelled()) break;
                        }

                    } else
                        searchResult.add(new GeonamesPlace());

                } else Log.i("URL", "ErrorCode: " + urlConnection.getResponseCode());


            } catch (IOException e) {

                Log.i("IOException", e.getMessage());

            } catch (JSONException e) {
                Log.i("JSONException", e.getMessage());
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }

            return searchResult;
        }

        @Override
        protected void onPostExecute(ArrayList<GeonamesPlace> searchResult) {
            ListView listView = listViewWeakReference.get();
            if (listView != null) {
                if (searchResult != null && searchResult.size() > 0) {
                    ArrayAdapter<GeonamesPlace> adapter = (ArrayAdapter<GeonamesPlace>) listView.getAdapter();
                    adapter.clear();
                    adapter.addAll(searchResult);
                    adapter.notifyDataSetChanged();

                }
            } else
                Toast.makeText(listView.getContext().getApplicationContext(), "Not possible to contact " + URL_GEONAMES, Toast.LENGTH_SHORT).show();


            super.onPostExecute(searchResult);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private String readStream(InputStream inputStream) {
            StringBuilder sb = new StringBuilder();

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null) {
                    sb.append(nextLine);
                }

            } catch (IOException e) {
                Log.i("IO-Exception-ReadStream", e.getMessage());
            }

            return sb.toString();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.e("onCancelled()", "ASYNCTASK: I've been cancelled and ready to GC clean");
        }
    }
}


