package si.uni_lj.fe.tnuv.projekt_tnuv;

import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import si.uni_lj.fe.tnuv.projekt_tnuv.WebApiCall.WebApiCallback;
import si.uni_lj.fe.tnuv.projekt_tnuv.WebApiCall.WebApiTask;
import si.uni_lj.fe.tnuv.projekt_tnuv.data.projektTnuvDatabase;
import si.uni_lj.fe.tnuv.projekt_tnuv.data.weatherTable;
import si.uni_lj.fe.tnuv.projekt_tnuv.data.weatherTableDao;
import si.uni_lj.fe.tnuv.projekt_tnuv.soap_webservices.SoapWebserviceManager;

public class aktivnost_vreme extends AppCompatActivity {

    TextView vremeTrenutnoTemperatura;
    TextView vremeTrenutnoOpis;
    TextView vremeNapovedTemperatura;
    TextView vremeNapovedOpis;
    TextView vremeKraj;
    ImageView vremeTrenutno;
    ImageView vremeNapoved;
    Switch vremeLokacija;

    weatherStorageManager storageManager;
    final int currentWeatherUid = 10;
    final int forecastWeatherUid = 1;

    weatherStateManager weatherManager;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private long UPDATE_INTERVAL = 10800 * 1000; //10sekund
    private long FASTEST_UPDATE_INTERVAL = 10 * 1000;

    private boolean LOCATION_UPDATES_ENABLED = false;

    private SharedPreferences storageShPref;

    // SHARED PREFERENCES
    /*
    * timestamp of the last weather api call
    * timestamp of the last location update
    * latitude
    * longitude
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aktivnost_vreme);

        storageManager = new weatherStorageManager();
        storageManager.connectToDatabase();

        weatherManager = new weatherStateManager();

        //db testing
        /*
        storageManager.readDatabase(0);
        storageManager.readDatabase(1);
        storageManager.weatherDatabaseDao.nukeWeatherTable();
        */

        //button for soap webservices testing
        final Button soapButton = findViewById(R.id.soap_test_button);
        soapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call test soap webservice call
                SoapWebserviceManager testSoapMng = new SoapWebserviceManager(getApplicationContext());
                //String dateTime = testSoapMng.GetDateTime();
                //Log.d("SOAP", "RESULT: " + dateTime);
                //testSoapMng.UserLogin("Jure", "password");

                //Intent i = new Intent(getApplicationContext(), login.class);
                //startActivity(i);
                //testSoapMng.UserLogout();

                testSoapMng.GetMessages();
                //testSoapMng.PostMessage("Message from a mobile app");
            }
        });

        storageShPref = getPreferences(Context.MODE_PRIVATE);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult == null){
                    Log.d("JURE", "Location result is null");
                    return;
                }
                for(Location location : locationResult.getLocations()){
                    //update UI with location data
                    float latitude = (float) location.getLatitude();
                    float longitude = (float) location.getLongitude();
                    Log.d("JURE", "Latitude and longitude: " + Float.toString(latitude) + "; "+ Float.toString(longitude));

                    //store the result with timestamp in shared preferences
                    String key  = getResources().getString(R.string.weather_location_timestamp);
                    long locationTimestamp = System.currentTimeMillis();

                    SharedPreferences.Editor editor = storageShPref.edit();
                    editor.putLong(key, locationTimestamp);
                    editor.apply();

                    Log.d("JURE", "values stored: " + Long.toString(storageShPref.getLong(key, 1243)));

                    key = getResources().getString(R.string.weather_location_latitude);
                    editor.putFloat(key, latitude);
                    editor.apply();

                    Log.d("JURE", Float.toString(storageShPref.getFloat(key, 1234)));

                    key = getResources().getString(R.string.weather_location_longitude);
                    editor.putFloat(key, longitude);
                    editor.apply();

                    Log.d("JURE", Float.toString(storageShPref.getFloat(key, 1234)));

                    //NEW_LOCATION_DATA_AVAILABLE = true;

                    Log.d("JURE", "Location data acquired. Calling weather api and setting up view");

                    //construct appropriate url and call weather api
                    weatherManager.getWeatherData(weatherManager.makeWeatherApiUrl(false), true);

                    //we only need one result
                    stopLocationUpdates();
                }
            }
        };

        //get elements
        vremeTrenutno = (ImageView) findViewById(R.id.vreme_trenutno);
        vremeNapoved = (ImageView) findViewById(R.id.vreme_napoved);
        vremeKraj = (TextView) findViewById(R.id.vreme_kraj);
        vremeTrenutnoOpis = (TextView) findViewById(R.id.vreme_trenutno_opis);
        vremeNapovedOpis = (TextView) findViewById(R.id.vreme_napoved_opis);
        vremeTrenutnoTemperatura = (TextView) findViewById(R.id.vreme_trenutno_temperatura);
        vremeNapovedTemperatura = (TextView) findViewById(R.id.vreme_napoved_temperatura);
        vremeLokacija = (Switch) findViewById(R.id.vreme_gps_switch);

        //listen for switch button events
        vremeLokacija.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                if(on){
                    Log.d("JURE", "using location with weather api");
                    LOCATION_UPDATES_ENABLED = true;
                    startLocationUpdates();
                    requestLocationUpdates();

                }
                else {
                    Log.d("JURE", "using default location for weather api");
                    LOCATION_UPDATES_ENABLED = false;
                    stopLocationUpdates();

                    weatherManager.getWeatherData(weatherManager.makeWeatherApiUrl(true), true);
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        weatherManager.getWeatherData(weatherManager.makeWeatherApiUrl(true), false);
        Log.d("JURE", "Weather data updated.");

        //apply to weather view
        weatherManager.applyWeatherView();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    //do not receive location updates when the activity is inactive
    @Override
    protected void onStop() {
        super.onStop();
        if(LOCATION_UPDATES_ENABLED) {
            stopLocationUpdates();
        }
    }

    protected void startLocationUpdates(){

        //make a location request
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);

        //create a location settings request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        //check if location settings are ok
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(aktivnost_vreme.this,
                                1);//REQUEST CHECK SETTINGS JE UNDEFINED
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    protected void requestLocationUpdates(){
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        } catch (SecurityException e){
            Log.e("JURE", "Security exception: cannot get coarse location.");
        }
    }

    protected void stopLocationUpdates(){
        Log.d("JURE", "location updates stopped");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private class weatherStateManager implements WebApiCallback{

        @Override
        public void updateFromDownload(Object result) {
            Log.d("JURE", "Odziv strežnika: "+ (String) result);

            parseWeatherApiResponse((String)result);
            weatherManager.applyWeatherView();
        }

        @Override
        public NetworkInfo getActiveNetworkInfo() {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo;
        }

        @Override
        public void onProgressUpdate(int progressCode, int percentComplete) {

        }

        @Override
        public void finishDownloading() {

        }

        //calls OpenweatherMap api if needed
        private void getWeatherData(String url, boolean FORCE_REQUEST){

            Log.d("JURE", "getWeatherData");

            if(FORCE_REQUEST){
                Log.d("JURE", "getWeatherData -> forced request");
                weatherManager.callApiAndParse(url);
            }
            else {
                //retrieve last read timestamp
                String key = getResources().getString(R.string.weather_api_call_timestamp);
                long defaultTimestamp = (int) getResources().getInteger(R.integer.weather_api_default_timestamp);
                long lastReadTimestamp = storageShPref.getLong(key, defaultTimestamp);

                final long apiReadPeriod = 10800000;

                //read every 3 hours
                //Log.d("JURE", Long.toString(System.currentTimeMillis()));
                if (System.currentTimeMillis() - lastReadTimestamp > apiReadPeriod) {

                    weatherManager.callApiAndParse(url);

                    //store the last read timestamp
                    long timestamp = System.currentTimeMillis();

                    SharedPreferences.Editor editor = storageShPref.edit();
                    editor.putLong(key, timestamp);
                    editor.apply(); //android studio tecnari ce uporabim commit

                }
            }
        }

        private String makeWeatherApiUrl(boolean mode){

            Log.d("JURE", "weatheManager -> makeWeatherApiUrl");

            String url;

            //default url(without location)
            if(mode){
                url = "https://api.openweathermap.org/data/2.5/forecast?id=3239318&lang=sl&units=metric&APPID=207611351bf12705fde4f7cce590693f"; //london: 2643743, ljubljana id: 3239318, somewhere in Russia: 519188
            }
            else {

                //get coordinates from storage
                float lat = storageShPref.getFloat(getResources().getString(R.string.weather_location_latitude), 0);
                float lon = storageShPref.getFloat(getResources().getString(R.string.weather_location_longitude), 0);

                url = "https://api.openweathermap.org/data/2.5/forecast?lat=" +Float.toString(lat)+"&lon="+Float.toString(lon)+"&lang=sl&units=metric&APPID=207611351bf12705fde4f7cce590693f";
                Log.d("JURE", "makeWeatherApiUrl: " + url);

                //NEW_LOCATION_DATA_AVAILABLE = false;
            }

            return url;
        }

        private void parseWeatherApiResponse(String response){

            Log.d("JURE", "parseWeatherApiResponse");

            try {
                JSONObject jsonObj = new JSONObject(response);

            /*
            Vremenske kode za openweathermap:
            - 2xx nevihta
            - 3xx rahel dez
            - 5xx dez
            - 6xx sneg
            - 7xx megla
            - 800 jasno
            - 80x oblaki
            - 906 toca

             */

                //CURRENT WEATHER
                //za OpenWeatherMap - current weather
                JSONObject jsonCurrentWeather = jsonObj.getJSONArray("list").getJSONObject(0);

                int currentWeatherId = jsonCurrentWeather.getJSONArray("weather").getJSONObject(0).getInt("id");
                double currentWeatherTemperature = jsonCurrentWeather.getJSONObject("main").getDouble("temp");
                String currentWeatherDescription = jsonCurrentWeather.getJSONArray("weather").getJSONObject(0).getString("description");

                int forecastIndex = getForecastIndex();
                JSONObject jsonForecastWeather = jsonObj.getJSONArray("list").getJSONObject(forecastIndex);

                int forecastWeatherID = jsonForecastWeather.getJSONArray("weather").getJSONObject(0).getInt("id");
                double forecastWeatherTemperature = jsonForecastWeather.getJSONObject("main").getDouble("temp");
                String forecastWeatherDescription = jsonForecastWeather.getJSONArray("weather").getJSONObject(0).getString("description");

                //put variables into storageShPref
                weatherTable currentWeather = storageManager.setRowValues(false, currentWeatherId, currentWeatherTemperature, currentWeatherDescription);
                weatherTable forecastWeather = storageManager.setRowValues(true, forecastWeatherID, forecastWeatherTemperature, forecastWeatherDescription);

                //storageManager.writeDatabase(currentWeather);
                //storageManager.writeDatabase(forecastWeather);

                storageManager.writeDatabase(forecastWeather, currentWeather);

            } catch (JSONException e){
                Log.e("JURE", "parseWeatherApiResponse: error trying to parse JSON");
            }
        }

        //returns the ID for JSON array for an appropriate weather forecast time
        private int getForecastIndex(){

            Calendar dt = Calendar.getInstance();
            int hourNow = dt.get(Calendar.HOUR_OF_DAY);
            int forecastIndex;

            //get forecast for today
            if (hourNow < 11) {
                forecastIndex = ((int)(12 - hourNow)/3) + 1;
            }

            //get forecast for the next day around noon
            else {
                forecastIndex = ((int)(36 - hourNow)/3);
            }

            return forecastIndex;
        }

        private void callApiAndParse(String url){
            String weatherApiResponse = "";

            Log.d("JURE", "weatherManager -> callApiAndParse");
            Log.d("JURE", "url: " + url);

            NetworkInfo netInfo = getActiveNetworkInfo();

            if(netInfo !=null && netInfo.isConnected()) {
                new WebApiTask(this).execute(url);
            }
            else {
                Context context = getApplicationContext();
                CharSequence text = "No network connection";
                int duration = Toast.LENGTH_SHORT;

                Toast.makeText(context, text, duration).show();
            }
        }

        private void applyWeatherView(){

            Log.d("JURE", "applyWeatherView");

            //get weather data from the database
            weatherTable currentWeather = storageManager.readDatabase(0);
            weatherTable forecastWeather = storageManager.readDatabase(1);

            //set location info
            if(LOCATION_UPDATES_ENABLED){
                String kraj = "Koordinate: ";

                float koordinata = storageShPref.getFloat(getResources().getString(R.string.weather_location_latitude), 1234);
                kraj = kraj + Float.toString(koordinata) + ", ";

                koordinata = storageShPref.getFloat(getResources().getString(R.string.weather_location_longitude), 1234);
                kraj = kraj + Float.toString(koordinata);

                vremeKraj.setText(kraj);
            }
            else {
                vremeKraj.setText("Ljubljana");
            }

            //current weather
            if(currentWeather.getWeatherId() != 800){
                switch ((int)(currentWeather.getWeatherId()/100)){
                    case 8:
                        vremeTrenutno.setImageResource(R.drawable.vreme_80_oblacno);
                        break;
                    case 7:
                        vremeTrenutno.setImageResource(R.drawable.vreme_3_rahel_dez); //manjka boljsa ikona
                        break;
                    case 6:
                        vremeTrenutno.setImageResource(R.drawable.vreme_6_sneg);
                        break;
                    case 5:
                        vremeTrenutno.setImageResource(R.drawable.vreme_5_dez);
                        break;
                    case 3:
                        vremeTrenutno.setImageResource(R.drawable.vreme_3_rahel_dez);
                        break;
                    case 2:
                        vremeTrenutno.setImageResource(R.drawable.vreme_2_nevihta);
                        break;
                }
            }
            else {
                //clear sky
                vremeTrenutno.setImageResource(R.drawable.vreme_800_jasno);
            }

            String temperatureStr = Double.toString(currentWeather.getWeatherTemperature()) + getResources().getString(R.string.degreesCelsiusStr);

            vremeTrenutnoTemperatura.setText(temperatureStr);
            vremeTrenutnoOpis.setText(currentWeather.getWeatherDescription());

            //forecast
            if(forecastWeather.getWeatherId() != 800){
                switch ((int)(forecastWeather.getWeatherId()/100)){
                    case 8:
                        vremeNapoved.setImageResource(R.drawable.vreme_80_oblacno);
                        break;
                    case 7:
                        vremeNapoved.setImageResource(R.drawable.vreme_3_rahel_dez); //manjka boljsa ikona
                        break;
                    case 6:
                        vremeNapoved.setImageResource(R.drawable.vreme_6_sneg);
                        break;
                    case 5:
                        vremeNapoved.setImageResource(R.drawable.vreme_5_dez);
                        break;
                    case 3:
                        vremeNapoved.setImageResource(R.drawable.vreme_3_rahel_dez);
                        break;
                    case 2:
                        vremeNapoved.setImageResource(R.drawable.vreme_2_nevihta);
                        break;
                }
            }
            else {
                vremeNapoved.setImageResource(R.drawable.vreme_800_jasno);
            }

            temperatureStr = Double.toString(forecastWeather.getWeatherTemperature()) + getResources().getString(R.string.degreesCelsiusStr); //zaokrozi na samo eno decimalko

            vremeNapovedTemperatura.setText(temperatureStr);
            vremeNapovedOpis.setText(forecastWeather.getWeatherDescription());
        }

    }

    /**********************************************************************************************
     * *********************************************************************************************
     * Weather Storage Management
     *
     *
     * */

    //allows database connection, writing and reading weather data from the database
    public class weatherStorageManager{
        projektTnuvDatabase weatherDatabase;
        weatherTableDao weatherDatabaseDao;


        //connect to db
        void connectToDatabase() {
            weatherDatabase = projektTnuvDatabase.getDatabaseInstance(getApplicationContext());
            weatherDatabaseDao = weatherDatabase.weatherTableDao();

            //currentWeather = new weatherTable();
            //forecastWeather = new weatherTable();
            Log.d("JURE", "successfully connected to weather database");
        }

        //write to db
        void writeDatabase(weatherTable... writeData){

            /*
            //set the correct uid
            weatherDatabaseDao.insertWeatherData(writeData);
            Log.d("JURE", "weather api data inserted into database");
            */

            new writeDbAsync().execute(writeData);
            Log.d("JURE", "weather api data inserted into database");

        }

        //get from db
        weatherTable readDatabase(int isWeatherForecast){

            Log.d("JURE", "weatherStorageManager -> readDatabase");
            //List<weatherTable> readResult = weatherDatabaseDao.getByForecast(isWeatherForecast);

            List<weatherTable> readResult = null;

            try {
                readResult = new readDbAsync().execute(isWeatherForecast).get(100, TimeUnit.MILLISECONDS);
            } catch(ExecutionException e){
                Log.e("JURE", "Execution exception readDatabase");
            } catch(InterruptedException e){
                Log.e("JURE", "Interrupted exception readDatabase");
            } catch(TimeoutException e){
                Log.e("JURE", "Timeout exception readDatabase");
            }

            //check if the any data has been read
            if(readResult == null || readResult.isEmpty()) {
                Log.d("JURE", "Data not found. Calling weather api to get fresh data.");

                //weatherManager.callApiAndParse("https://api.openweathermap.org/data/2.5/forecast?id=3239318&lang=sl&units=metric&APPID=207611351bf12705fde4f7cce590693f");
                if(!LOCATION_UPDATES_ENABLED) {
                    weatherManager.getWeatherData(weatherManager.makeWeatherApiUrl(true), true);
                }

                //readResult = weatherDatabaseDao.getByForecast(isWeatherForecast);
                try {
                    readResult = new readDbAsync().execute(isWeatherForecast).get();
                }
                catch(ExecutionException e){
                    Log.e("JURE", "Execution exception readDatabase");
                }
                catch(InterruptedException e){
                    Log.e("JURE", "Interrupted exception readDatabase");
                }

                if(readResult == null || readResult.isEmpty()){
                    Log.d("JURE", "Data still not available. Returning dummy data.");

                    weatherTable dummyRet = new weatherTable();

                    dummyRet.setWeatherId(800);
                    dummyRet.setWeatherTemperature(123.4);
                    dummyRet.setWeatherDescription("dummy weather description");
                    return dummyRet;
                }
            }

            weatherTable readFromDb = readResult.get(0);

            if(readResult.size() > 1){
               Log.d("JURE", "Too many matching elements found");
            }

            //log the values read

            Log.d("JURE", "data read:");
            Log.d("JURE", "uid: " + Integer.toString(readFromDb.getUid()));
            Log.d("JURE", "description: " + readFromDb.getWeatherDescription());
            Log.d("JURE", "temperature: " + readFromDb.getWeatherTemperature());


            return readResult.get(0);
        }

        //set values for one table row
        weatherTable setRowValues(boolean isForecast, int weatherId, double temperature, String description){

            weatherTable data = new weatherTable();

            if(isForecast == true) {
                data.setUid(forecastWeatherUid);
            }
            else data.setUid(currentWeatherUid);

            data.setIsWeatherForecast(isForecast);
            data.setWeatherId(weatherId);
            data.setWeatherTemperature(temperature);
            data.setWeatherDescription(description);

            return data;
        }

        //naj bo static!!
        // ce dam to ven in nastavim kot static ne vem, kako dobiti instanco StorageManagerja
        private class writeDbAsync extends AsyncTask<weatherTable, Void, Void> {

            @Override
            protected Void doInBackground(weatherTable... data) {

                int parNum = data.length;
                //Log.d("JURE", "number of elements to write: " + Integer.toString(parNum));

                for(int i = 0; i<parNum; i++){
                    weatherDatabaseDao.insertWeatherData(data[i]);
                }

                return null;
            }
        }

        private class readDbAsync extends AsyncTask<Integer, Void, List<weatherTable>>{


            @Override
            protected List<weatherTable> doInBackground(Integer... isWeatherForecast) {
                List<weatherTable> dataList;

                if(isWeatherForecast.length > 1){
                    Log.d("JURE", "readDbAsync: too many input arguments given. Using the first one");
                }

                dataList = weatherDatabaseDao.getByForecast(isWeatherForecast[0]);

                return dataList;
            }
        }
    }
}
