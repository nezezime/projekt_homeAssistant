package si.uni_lj.fe.tnuv.projekt_tnuv;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.Arrays;

import si.uni_lj.fe.tnuv.projekt_tnuv.WebApiCall.WebApiCallback;
import si.uni_lj.fe.tnuv.projekt_tnuv.WebApiCall.WebApiTask;

public class aktivnost_trola extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, java.io.Serializable {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    public trolaManager trolaMng;
    SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("JURE", "Activity trola started");

        trolaMng = new trolaManager();

        setContentView(R.layout.activity_aktivnost_trola);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //actionbar
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Prihodi avtobusov");
        } else if(getActionBar() != null){
            getActionBar().setTitle("Prihodi avtobusov");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        trolaMng.makeTrolaApiRequest(trolaMng.makeTrolaUrl());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_aktivnost_trola, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.trola_refresh) {
            //refresh the data
            Log.d("JURE", "Refresh by button");

            //toast
            CharSequence text = "Osvežujem";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(getApplicationContext(), text, duration).show();

            //make a new request
            trolaMng.makeTrolaApiRequest(trolaMng.makeTrolaUrl());

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {

    }


    private class trolaManager implements WebApiCallback, java.io.Serializable{
        String[][] trznicaMosteCenterArrivals; //prihodi v smeri centra
        String[][] trznicaMosteArrivals; //prihodi izven smeri centra
        int stationNumberCenter = 303011;
        int stationNumber = 303012;
        boolean DIRECTION_CENTRAL = true;

        @Override
        public void updateFromDownload(Object result) {
            Log.d("JURE", "odziv streznika: " + (String) result);

            parseTrolaApiResponse((String)result);

            // Create the adapter that will return a fragment for each of the two
            // primary sections of the activity.
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            //put the parsed data into Intent when starting a new fragment

            // Set up the ViewPager with the sections adapter.
            /*
      The {@link ViewPager} that will host the section contents.
     */
            ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);

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

        String makeTrolaUrl() {
            return "https://www.trola.si/trznica moste";
        }

        void makeTrolaApiRequest(String url){
            Log.d("JURE", "makeTrolaApiRequest");

            NetworkInfo netInfo = getActiveNetworkInfo();

            //check network connection and make http request
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

        //returns a 2D array of strings
        //string element: String[x][0] = bus line number
        // String[x][1] = bus line name
        // String[x][2] = arrivals
        String[][] parseTrolaApiResponse(String response){
            Log.d("JURE", "parseTrolaApiResponse");

            try{
                JSONObject jsonObj = new JSONObject(response);
                JSONObject station = jsonObj.getJSONArray("stations").getJSONObject(0);

                //get the station direction according to station number
                String JSONstationNumber = station.getString("number");
                //Log.d("JURE", "station number: " + stationNumber);

                if(JSONstationNumber.equals(Integer.toString(stationNumberCenter))){
                    //put the values into storage
                    fillBusArrivalsStorage(station, DIRECTION_CENTRAL);
                } else {
                    fillBusArrivalsStorage(station, !DIRECTION_CENTRAL);
                }

                station = jsonObj.getJSONArray("stations").getJSONObject(1);
                JSONstationNumber = station.getString("number");
                Log.d("JURE", "station number: " + JSONstationNumber);

                if(JSONstationNumber.equals(Integer.toString(stationNumber))){
                    fillBusArrivalsStorage(station, !DIRECTION_CENTRAL);
                } else {
                    fillBusArrivalsStorage(station, DIRECTION_CENTRAL);
                }


            } catch (JSONException e){
                Log.d("JURE", "parseTrolaApiResponse: error trying to parse JSON");
            }


            return null;
        }

        //fills the strings containing bus arrivals with values
        //direction:
        //  true - arrivals in central direction
        //  false - arrivals in the opposite direction
        void fillBusArrivalsStorage(JSONObject json, boolean direction) {
            Log.d("JURE", "fillBusArrivalStorage");

            try{
                JSONArray array = json.getJSONArray("buses");
                int numElements = array.length();
                //Log.d("JURE", "JSON array length: " + numElements);

                if(direction){
                    trznicaMosteCenterArrivals = new String[numElements][3];
                } else {
                    trznicaMosteArrivals = new String[numElements][3];
                }

                String lineNum, lineName;
                String lineArrivals;

                for(int i=0; i<numElements; i++){

                    //read line number from JSON
                    lineNum = array.getJSONObject(i).getString("number");
                    lineNum = lineNum + "  ";
                    //Log.d("JURE", "line number: " + lineNum);

                    //get the bus line name
                    lineName = array.getJSONObject(i).getString("direction");
                    //Log.d("JURE", "line name: " + lineName);

                    //get arrival times from JSON and convert to pretty string
                    JSONArray arrivalsList = array.getJSONObject(i).getJSONArray("arrivals");
                    int arrivalNum = arrivalsList.length();

                    //Log.d("JURE", "number of busses arriving: " + Integer.toString(arrivalNum));
                    lineArrivals = "";

                    if(arrivalNum > 0) {
                        for(int j=0; j<arrivalNum; j++){
                            //show a maximum of 3 arrivals
                            if(j>2) break;
                            lineArrivals = lineArrivals.concat(Integer.toString(arrivalsList.getInt(j)));
                            lineArrivals = lineArrivals.concat(", ");
                        }

                        //slice the last ", "
                        lineArrivals = lineArrivals.substring(0, lineArrivals.length() - 2);

                    } else {
                        lineArrivals = "ni podatka";
                    }
                    //Log.d("JURE", "casi prihodov: " + lineArrivals);

                    //store the row that was just read to corresponding string
                    if(direction){
                        trznicaMosteCenterArrivals[i][0] = lineNum; //line number
                        trznicaMosteCenterArrivals[i][1] = lineName; //line name
                        trznicaMosteCenterArrivals[i][2] = lineArrivals; //arrivals
                    } else {
                        trznicaMosteArrivals[i][0] = lineNum; //line number
                        trznicaMosteArrivals[i][1] = lineName; //line name
                        trznicaMosteArrivals[i][2] = lineArrivals; //arrivals
                    }
                }

                Log.d("JURE", "direction center: " + Arrays.deepToString(trznicaMosteCenterArrivals));
                Log.d("JURE", "direction center opposite: " + Arrays.deepToString(trznicaMosteArrivals));

            } catch (JSONException e) {
                Log.d("JURE", "fillBusArrivalStorage: error trying to parse JSON");
            }
        }

        int getTrznicaMosteCenterRowNumber(){
            return trznicaMosteCenterArrivals.length;
        }

        int getTrznicaMosteRowNumber(){
            return trznicaMosteArrivals.length;
        }
    }


    /**
     * A placeholder fragment containing a view.
     */
    public static class PlaceholderFragment extends Fragment implements java.io.Serializable{
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_TROLA_MANAGER = "trola_manager";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         * @note: razredi in spremenljivke, za katere hocemo, da so jih fragment vidi,
         * moramo podati v tej metodi (+ razredi morajo implementirati java.io.Serializable)
         */
        public static PlaceholderFragment newInstance(int sectionNumber, trolaManager trolaMng) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putSerializable(ARG_TROLA_MANAGER, trolaMng);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d("JURE", "creating new fragment in trola view");

            //ustvari nov fragment
            View rootView = inflater.inflate(R.layout.fragment_aktivnost_trola, container, false);

            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            //tukaj vstavim vrednosti v tabelo
            TableLayout tabelaTrola = (TableLayout) rootView.findViewById(R.id.trola_fragment_table);

            int fragmentNum = getArguments().getInt(ARG_SECTION_NUMBER);
            Log.d("JURE", "fragment number: " + Integer.toString(fragmentNum));

            //deserialize the trolaManager class
            trolaManager trolaFragmentMng = (trolaManager) getArguments().getSerializable(ARG_TROLA_MANAGER);
            //Log.d("JURE", "rezultat: " + Arrays.deepToString(trolaFragmentMng.trznicaMosteCenterArrivals));

            //fill the central direction data
            if(fragmentNum == 1){
                for(int i=0; i<trolaFragmentMng.getTrznicaMosteCenterRowNumber(); i++) {
                    TableRow row = new TableRow(this.getContext());
                    TextView lineNumber = new TextView(this.getContext());
                    TextView lineDescription = new TextView(this.getContext());
                    TextView lineArrivals = new TextView(this.getContext());

                    //set values according to values set in storage
                    lineNumber.setText(trolaFragmentMng.trznicaMosteCenterArrivals[i][0]);
                    lineDescription.setText(trolaFragmentMng.trznicaMosteCenterArrivals[i][1]);
                    lineArrivals.setText(trolaFragmentMng.trznicaMosteCenterArrivals[i][2]);

                    //set text size
                    lineNumber.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
                    lineNumber.setPadding(20, 20, 20, 5);

                    lineDescription.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

                    lineArrivals.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);

                    //apply row
                    row.addView(lineNumber);
                    row.addView(lineDescription);
                    row.addView(lineArrivals);

                    tabelaTrola.addView(row);

                    //dont draw spacer for the last row
                    if(i==trolaFragmentMng.getTrznicaMosteCenterRowNumber() - 1) break;

                    TableRow spaceRow = new TableRow(this.getContext());
                    View spacer1 = new View(this.getContext());
                    View spacer2 = new View(this.getContext());
                    View spacer3 = new View(this.getContext());

                    spacer1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
                    spacer1.setBackgroundColor(Color.rgb(0,0,0));

                    spacer2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
                    spacer2.setBackgroundColor(Color.rgb(0,0,0));

                    spacer3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
                    spacer3.setBackgroundColor(Color.rgb(0,0,0));

                    spaceRow.addView(spacer1);
                    spaceRow.addView(spacer2);
                    spaceRow.addView(spacer3);

                    tabelaTrola.addView(spaceRow);
                }
            }
            //fill the opposite direction data
            else if(fragmentNum == 2){
                for(int i=0; i<trolaFragmentMng.getTrznicaMosteRowNumber(); i++) {
                    TableRow row = new TableRow(this.getContext());
                    TextView lineNumber = new TextView(this.getContext());
                    TextView lineDescription = new TextView(this.getContext());
                    TextView lineArrivals = new TextView(this.getContext());

                    //set values according to values set in storage
                    lineNumber.setText(trolaFragmentMng.trznicaMosteArrivals[i][0]);
                    lineDescription.setText(trolaFragmentMng.trznicaMosteArrivals[i][1]);
                    lineArrivals.setText(trolaFragmentMng.trznicaMosteArrivals[i][2]);

                    //set text size
                    lineNumber.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
                    lineNumber.setPadding(20, 20, 20, 5);

                    lineDescription.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

                    lineArrivals.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);

                    //apply row
                    row.addView(lineNumber);
                    row.addView(lineDescription);
                    row.addView(lineArrivals);

                    tabelaTrola.addView(row);

                    //dont draw spacer for the last row
                    if(i==trolaFragmentMng.getTrznicaMosteCenterRowNumber() - 1) break;

                    TableRow spaceRow = new TableRow(this.getContext());
                    View spacer1 = new View(this.getContext());
                    View spacer2 = new View(this.getContext());
                    View spacer3 = new View(this.getContext());

                    spacer1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
                    spacer1.setBackgroundColor(Color.rgb(0,0,0));

                    spacer2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
                    spacer2.setBackgroundColor(Color.rgb(0,0,0));

                    spacer3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
                    spacer3.setBackgroundColor(Color.rgb(0,0,0));

                    spaceRow.addView(spacer1);
                    spaceRow.addView(spacer2);
                    spaceRow.addView(spacer3);

                    tabelaTrola.addView(spaceRow);

                    //uredi se prikaz smeri postajalisca
                    TextView smerPostaje = (TextView) rootView.findViewById(R.id.trola_smer);
                    smerPostaje.setText("");
                }
            }

            //fill the table rows with entries for each bus
            /*for(int i = 0; i<5; i++){
                TableRow row = new TableRow(this.getContext());
                View rowSpacer = new View(this.getContext());
                TextView lineNumber = new TextView(this.getContext());
                TextView lineDescription = new TextView(this.getContext());
                TextView arrivals = new TextView(this.getContext());

                //set values according to values set in storage

                lineNumber.setText("test");
                row.addView(lineNumber);
                tabelaTrola.addView(row);

                //add spacer between rows
                //rowSpacer.setLayoutParams(new ViewGroup.LayoutParams(match_parent, 10));
                //tabelaTrola.addView(rowSpacer);
            }*/


            return rootView;
        }
    }

    /**
     * returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter implements java.io.Serializable{

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, trolaMng);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }
}
