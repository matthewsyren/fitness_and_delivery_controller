/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class is used to add and update Client information
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;

public class ClientActivity extends AppCompatActivity implements IAPIConnectionResponse{
    //Declarations
    Client client;
    String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_client);

            //Displays Activity in appropriate form
            displayViews();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Takes the user back to the ClientControlActivity when the back button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try{
            int id = item.getItemId();

            //Takes the user back to the ClientControlActivity if the button that was pressed was the back button
            if (id == android.R.id.home) {
                Intent intent = new Intent(ClientActivity.this, ClientControlActivity.class);
                startActivity(intent);
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    //Method alters Activity based on the action the user is performing
    public void displayViews(){
        try{
            //Fetches the user's action from the Bundle
            Bundle bundle = getIntent().getExtras();
            action = bundle.getString("action");
            Button button = (Button) findViewById(R.id.button_add_client);

            //Changes Activity based on the user's action
            if(action.equals("update")){
                EditText txtClientID = (EditText) findViewById(R.id.text_client_id);
                txtClientID.setEnabled(false);
                button.setText(R.string.button_update_client);
                Client client = (Client) bundle.getSerializable("clientObject");
                displayData(client);
            }
            else if(action.equals("add")){
                button.setText(R.string.button_add_client);
            }

            //Displays Back button in ActionBar
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null){
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method pre-populates the TextViews on this Activity with the data from the Client item that was clicked on in the previous Activity and sent through the bundle
    public void displayData(Client client){
        try{
            //View assignments
            EditText txtClientID = (EditText) findViewById(R.id.text_client_id);
            EditText txtClientName = (EditText) findViewById(R.id.text_client_name);
            EditText txtClientEmail = (EditText) findViewById(R.id.text_client_email);
            EditText txtClientAddress = (EditText) findViewById(R.id.text_client_address);

            //Displays appropriate data in Views
            txtClientID.setText(client.getClientID());
            txtClientName.setText(client.getClientName());
            txtClientEmail.setText(client.getClientEmail());
            txtClientAddress.setText(client.getClientAddress());
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method adds/updates the Client details to the database
    public void addClientOnClick(View view) {
        try{
            //View assignments
            EditText txtClientID = (EditText) findViewById(R.id.text_client_id);
            EditText txtClientName = (EditText) findViewById(R.id.text_client_name);
            EditText txtClientEmail = (EditText) findViewById(R.id.text_client_email);
            EditText txtClientAddress = (EditText) findViewById(R.id.text_client_address);

            //Fetches data from Views
            String clientID = txtClientID.getText().toString();
            String clientName = txtClientName.getText().toString();
            String clientEmail = txtClientEmail.getText().toString();
            String clientAddress = txtClientAddress.getText().toString();
            client = new Client(clientID, clientName, clientEmail, clientAddress);

            //Calls the Google Maps API to determine whether the user has entered a valid address
            if(client.validateClient(this) && checkInternetConnection()){
                if(action.equals("update") || (action.equals("add") && !client.checkClientID(this))){
                    //Displays ProgressBar
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
                    progressBar.setVisibility(View.VISIBLE);

                    //Fetches coordinates of address entered by the user from the Google Maps API
                    APIConnection api = new APIConnection();
                    api.delegate = this;
                    api.execute("http://maps.google.com/maps/api/geocode/json?address=" + client.getClientAddress());
                }
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method reads the data returned from the Google Maps API (the coordinates if the address entered by the user) and determines whether the user has entered a valid address
    @Override
    public void getJsonResponse(String response) {
        try{
            //Turns response into JSONObject
            JSONObject location = Client.getAddressCoordinates(response, this);

            //Saves address coordinates if they were found by Google Maps
            if(location != null){
                client.setClientLatitude(location.getDouble("lat"));
                client.setClientLongitude(location.getDouble("lng"));

                //Gets Firebase Database reference
                final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = firebaseDatabase.getReference().child(new User(this).getUserKey()).child("clients");

                //Adds Listeners for when the data is changed
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean valid = true;

                        //Writes the Client details to the Firebase Database
                        if(action.equals("add")) {
                            if (dataSnapshot.child(client.getClientID()).exists()) {
                                Toast.makeText(getApplicationContext(), "The Client ID you have entered has already been used, please choose another one", Toast.LENGTH_LONG).show();
                                valid = false;
                                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }

                        databaseReference.removeEventListener(this);
                        if(valid) {
                            requestUpdateOfClient(client);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.i("Data", "Failed to read data, please check your internet connection");
                    }
                });
            }
        }
        catch(Exception exc){
            exc.printStackTrace();
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method calls the FirebaseService class and passes in a Client object that must be written to the Firebase database
    public void requestUpdateOfClient(Client client){
        try{
            //Displays ProgressBar
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
            progressBar.setVisibility(View.VISIBLE);

            //Requests location information from the LocationService class
            String firebaseKey = new User(this).getUserKey();
            Intent intent = new Intent(getApplicationContext(), FirebaseService.class);
            intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
            intent.setAction(FirebaseService.ACTION_UPDATE_CLIENT);
            intent.putExtra(FirebaseService.ACTION_UPDATE_CLIENT, client);
            intent.putExtra(FirebaseService.RECEIVER, new DataReceiver(new Handler()));
            startService(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method checks the Internet connection, and returns true if there is an internet connection, and false if there is no internet connection
    public boolean checkInternetConnection(){
        boolean connected = true;
        try{
            ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            //Displays a message if there is no internet connection
            if (!(networkInfo != null && networkInfo.isConnected())) {
                Toast.makeText(getApplicationContext(), "Please check your internet connection...", Toast.LENGTH_LONG).show();
                connected = false;
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
        return connected;
    }

    //Creates a ResultReceiver to retrieve information from the FirebaseService
    private class DataReceiver extends ResultReceiver {
        private DataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){

            if(resultCode == FirebaseService.ACTION_UPDATE_CLIENT_RESULT_CODE){
                Intent intent = null;

                if(action.equals("add")){
                    Toast.makeText(getApplicationContext(), "Client successfully added", Toast.LENGTH_LONG).show();
                    intent = getIntent();
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Client successfully updated", Toast.LENGTH_LONG).show();

                    //Takes the user back to the ClientControlActivity once the update is complete
                    intent = new Intent(ClientActivity.this, ClientControlActivity.class);
                }

                //Hides ProgressBar
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
                progressBar.setVisibility(View.INVISIBLE);

                startActivity(intent);
            }
        }
    }
}