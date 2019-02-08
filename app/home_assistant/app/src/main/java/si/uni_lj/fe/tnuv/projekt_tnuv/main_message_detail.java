package si.uni_lj.fe.tnuv.projekt_tnuv;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import si.uni_lj.fe.tnuv.projekt_tnuv.MessageManagement.messageStorageManager;

public class main_message_detail extends AppCompatActivity {

    //keys required to retrieve the extras
    private static final String EXTRA_DATE_AND_TIME = "EXTRA_DATE_AND_TIME";
    private static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    private static final String EXTRA_USER_COLOR = "EXTRA_USER_COLOR";
    private static final String EXTRA_USER_NAME = "EXTRA_USER_NAME";

    //views
    private TextView dateAndTime;
    private TextView message;
    private TextView userName;
    private View coloredBackground;

    //needs to be accessed for deletion
    long dateAndTimeExtra;

    messageStorageManager storageManager;

    ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_message_detail);

        Log.d("JURE", "message detail activity started");

        storageManager= new messageStorageManager(this);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        /*
        * PRAVILEN POSTOPEK ZA POSREDOVANJE PODATKOV
        * Kot extra posljes samo unique ID, in potem iz baze povleces potrebne podatke
        * */
        Intent i = getIntent();

        //get the extras
        dateAndTimeExtra = i.getLongExtra(MainActivity.EXTRA_DATE_AND_TIME, 0);
        String messageExtra = i.getStringExtra(MainActivity.EXTRA_MESSAGE);
        String userNameExtra = i.getStringExtra(MainActivity.EXTRA_USER_NAME);
        int userColorExtra = i.getIntExtra(MainActivity.EXTRA_USER_COLOR, 0);

        //apply the values
        dateAndTime = (TextView) findViewById(R.id.main_message_datetime);
        dateAndTime.setText(storageManager.unixToStandardTime(dateAndTimeExtra));

        userName = (TextView) findViewById(R.id.main_message_user_name);
        userName.setText(userNameExtra);

        message = (TextView) findViewById(R.id.main_message_body);
        Log.d("JURE", "sporocilo: " + messageExtra);
        message.setText(messageExtra);

        coloredBackground = findViewById(R.id.main_message_detail_view);
        //coloredBackground.setBackgroundColor(ContextCompat.getColor(this, userColorExtra));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_aktivnost_main_message_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.delete_message){
            deleteMessage();
        } else if (id == R.id.home){
            CharSequence text = "izhod";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(getApplicationContext(), text, duration).show();
            finish();
        }
        return true;
    }

    private void deleteMessage(){
        Log.d("JURE", "Message detail activity -> message deletion: " + Long.toString(dateAndTimeExtra));

        //delete the message from database
        storageManager.deleteFromMessageDb(dateAndTimeExtra);

        //toast
        CharSequence text = "Sporočilo izbrisano";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(getApplicationContext(), text, duration).show();

        //finsih the activity;
        finish();
    }
}
