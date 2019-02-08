package si.uni_lj.fe.tnuv.projekt_tnuv;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import si.uni_lj.fe.tnuv.projekt_tnuv.WebApiCall.WebApiCallback;
import si.uni_lj.fe.tnuv.projekt_tnuv.list_item.MessageDataSource;
import si.uni_lj.fe.tnuv.projekt_tnuv.list_item.listItem;
import si.uni_lj.fe.tnuv.projekt_tnuv.list_item_logic.listItemController;
import si.uni_lj.fe.tnuv.projekt_tnuv.soap_webservices.SoapWebserviceManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, listItemInterface, WebApiCallback {

    SoapWebserviceManager soapClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //actionbar
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sporočila");
        } else if(getActionBar() != null){
            getActionBar().setTitle("Sporočila");
        }

        soapClient = new SoapWebserviceManager(getApplicationContext());
        //user login and server connectivity test
        if(soapClient.userLoggedIn == false) {
            Intent i = new Intent(getApplicationContext(), login.class);
            startActivity(i);
        }

        //refresh list of users
        String dateTime = soapClient.GetDateTime();
        Log.d("SOAP", "RESULT: " + dateTime);
        soapClient.GetUsers();


        //za prikazovanje sporočil
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_main);
        layoutInflater = getLayoutInflater();

        //dependency injection -> razred mainActivity za kontroler ustvari dependency namesto da bi kontroler to storil sam
        controller = new listItemController(this, new MessageDataSource(this));

        //gumb za novo sporocilo
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.main_new_message);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                CharSequence text = "Novo sporočilo";
                int duration = Toast.LENGTH_SHORT;

                Toast.makeText(context, text, duration).show();

                Intent i = new Intent(context, main_new_message.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //refresh the data in recycler view
        updateRecyclerView();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.pokazi_vreme) {
            startActivity(new Intent(this, aktivnost_vreme.class));
        } else if (id == R.id.pokazi_avtobus) {
            startActivity(new Intent(this, aktivnost_trola.class));
        } else if (id == R.id.novo_sporocilo) {
            startActivity(new Intent(this, main_new_message.class));
        } else if (id == R.id.odjava) {
            Log.d("MAIN", "Logging user out");
            soapClient.UserLogout();

            //go to login activity
            String text = "uporabnik odjavljen";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
            startActivity(new Intent(getApplicationContext(), login.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //trola API call example
    /*public void testKlicTrolaApi(View V){

        Log.d("JURE", "Calling trola api");

        NetworkInfo netInfo = getActiveNetworkInfo();

        if(netInfo !=null && netInfo.isConnected()) {
            new WebApiTask(this).execute("https://www.trola.si/trznica moste/20");
        }
        else {
            Context context = getApplicationContext();
            CharSequence text = "No network connection";
            int duration = Toast.LENGTH_SHORT;

            Toast.makeText(context, text, duration).show();
        }
    }*/

    @Override
    public void updateFromDownload(Object result) {
        Log.d("MAIN", "Server response: "+ (String) result);
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


    /************************ PRIKAZ SPOROCIL *****************************************************/

    public static final String EXTRA_DATE_AND_TIME = "EXTRA DATE AND TIME";
    public static final String EXTRA_MESSAGE = "EXTRA MESSAGE";
    public static final String EXTRA_USER_COLOR = "EXTRA USER COLOR";
    public static final String EXTRA_USER_NAME = "EXTRA USER NAME";

    private List<listItem> listOfData;

    private LayoutInflater layoutInflater;
    private RecyclerView recyclerView;
    private CustomAdapter adapter;

    private listItemController controller;

    @Override
    //forwards the data received from controller to the detail activity
    public void startMessageDetailActivity(long dateAndTime, String message, String userName, int userColor) {
        Intent i = new Intent(this, main_message_detail.class);
        i.putExtra(EXTRA_DATE_AND_TIME, dateAndTime);
        i.putExtra(EXTRA_MESSAGE, message);
        i.putExtra(EXTRA_USER_COLOR, userColor);
        i.putExtra(EXTRA_USER_NAME, userName);
        startActivity(i);
    }

    private class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder>{

        @Override
        //inflates a single message_item
        public CustomAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = layoutInflater.inflate(R.layout.activity_main_message_item, parent, false);

            return new CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(CustomViewHolder holder, int position) {
            //get the contents of the message being inflated
            listItem currentItem = listOfData.get(position);

            //get the first letter of the username
            String userLetter = currentItem.getUserName().substring(0,1);

            //set the message parameters
            holder.coloredCircle.setColorFilter(currentItem.getUserColor());
            holder.userName.setText(userLetter);
            holder.dateAndTime.setText(unixToStandardDatetime(currentItem.getDateAndTime()));
            holder.message.setText(currentItem.getMessage());
        }

        public String unixToStandardDatetime(long timestamp){
            String datetime;

            Date date = new java.util.Date(timestamp);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            //sdf.setTimeZone();
            datetime = sdf.format(date);
            return datetime;
        }

        @Override
        //helps the adapter to determine how many messages are going to be displayed
        //should there be a lot of messages to display, we can show only a few of them and inflate
        //additional messages as the user scrolls down
        public int getItemCount() {
            return listOfData.size();
        }

        //connects message contents with the message display
        class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private ImageView coloredCircle;
            private TextView dateAndTime;
            private TextView userName;
            private TextView message;
            private ViewGroup container;

            public CustomViewHolder(View itemView) {
                super(itemView);

                this.coloredCircle = (ImageView) itemView.findViewById(R.id.main_message_item_circle);
                this.dateAndTime = (TextView) itemView.findViewById(R.id.main_message_item_datetime_text);
                this.userName = (TextView) itemView.findViewById(R.id.main_message_item_user_name);
                this.message = (TextView) itemView.findViewById(R.id.main_message_item_text);
                this.container = (ViewGroup) itemView.findViewById(R.id.main_message_item);

                this.container.setOnClickListener(this);

            }

            @Override
            public void onClick(View view) {

                //return the contents of the selected list element
                listItem listItem = listOfData.get(this.getAdapterPosition());
                controller.onListItemClick(listItem);
            }
        }
    }

    @Override
    public void setupAdatapterAndView(List<listItem> listOfData) {
        this.listOfData = listOfData;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomAdapter();
        recyclerView.setAdapter(adapter);
    }

    public void updateRecyclerView(){
        Log.d("MAIN", "updateRecyclerView");
        soapClient.GetMessages();
        controller.getListFromDataSource();
    }
}
