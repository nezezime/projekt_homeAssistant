package si.uni_lj.fe.tnuv.projekt_tnuv;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import si.uni_lj.fe.tnuv.projekt_tnuv.soap_webservices.SoapWebserviceManager;

public class login extends AppCompatActivity {

    EditText userName;
    EditText userPassword;
    Button loginButton;
    Context context;
    SoapWebserviceManager soapClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();

        userName = findViewById(R.id.input_username);
        userPassword = findViewById(R.id.input_password);
        //newMessageFab = findViewById(R.id.main_new_message);
        //newMessageFab.setVisibility(View.GONE);
        loginButton = findViewById(R.id.btn_login);

        //test the connection to server if()
        soapClient = new SoapWebserviceManager(getApplicationContext());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }
        });

    }

    private void userLogin() {
        Log.d("LOGIN", "userLogin");

        //validate
        String user_name = userName.getText().toString();
        String password = userPassword.getText().toString();

        if(user_name.isEmpty()) {
            String text = "uporabniško ime ne sme biti prazno";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        if(password.isEmpty()) {
            String text = "geslo ne sme biti prazno";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(login.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Prijava poteka...");
        progressDialog.show();

        //test server connection
        int result = soapClient.TestServerConnection();
        if(result != 0) {
            progressDialog.dismiss();
            Log.d("LOGIN", "No connection to server " + Integer.toString(result));
            loginButton.setEnabled(true);
            String text = "trenutno ni mogoče vzpostaviti povezave s strežnikom";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        //login using SOAP
        result = soapClient.UserLogin(user_name, password);
        Log.d("LOGIN", "Login result " + Integer.toString(result));

        if(result != 0) {
            progressDialog.dismiss();
            loginButton.setEnabled(true);
            String text = "napačno uporabniško ime ali geslo";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        finish();
                    }
                }, 2000);
    }

    @Override
    public void onBackPressed() {
        //disable the back button to MainActivity
        moveTaskToBack(true);
    }

}
