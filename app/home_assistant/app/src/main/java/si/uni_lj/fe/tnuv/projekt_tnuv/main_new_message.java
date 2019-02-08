package si.uni_lj.fe.tnuv.projekt_tnuv;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import si.uni_lj.fe.tnuv.projekt_tnuv.soap_webservices.SoapWebserviceManager;

public class main_new_message extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    FloatingActionButton fab;
    EditText userText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("JURE", "main_new_message activity started");
        setContentView(R.layout.activity_main_new_message);

        //actionbar
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Novo sporočilo");
        } else if(getActionBar() != null){
            getActionBar().setTitle("Novo sporočilo");
        }

        //get the edit text resource
        userText = (EditText) findViewById(R.id.new_message_text_edit);

        //allows the app to hide keyboard when clicking off the edit text
        userText.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) hideKeyboard(view);
            }
        });

        //allow the floating action button to be displayed above soft keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        //set the floating action button listener
        fab = findViewById(R.id.new_message_fab);
        fab.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Context context = getApplicationContext();
                   CharSequence text;
                   int duration = Toast.LENGTH_SHORT;
                   int result = 0;

                   //get text from edit text
                   String userMessage = userText.getText().toString();
                   Log.d("JURE", "Message entered: " + userMessage);

                   //check if the message contains text
                   if(userMessage.isEmpty()){
                       text = "Sporočilo nima vsebine";
                       Toast.makeText(context, text, duration).show();
                       return;
                   }

                   //send storage request to server
                   text = "Shranjujem sporočilo";
                   Toast.makeText(context, text, duration).show();

                   SoapWebserviceManager soapMng = new SoapWebserviceManager(getApplicationContext());
                   result = soapMng.PostMessage(userMessage);
                   if(result != 0) {
                       text = "Shranjevanje sporočila ni uspelo";
                       Toast.makeText(context, text, duration).show();
                       finish();
                   }

                   //update the local messages storage
                   soapMng.GetMessages();

                   //finsh the activity
                   finish();
               }
           }
        );
    }

    //spinner events
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Log.d("JURE", "onItemSelected: " + parent.getItemAtPosition(pos));
//        currentSpinnerSelection = parent.getItemAtPosition(pos).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void hideKeyboard(View view){
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
