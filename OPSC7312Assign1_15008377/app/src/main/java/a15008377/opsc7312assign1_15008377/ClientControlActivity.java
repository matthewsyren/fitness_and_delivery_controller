/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class shows a report of the Clients that are stored in the database
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.view.View.OnKeyListener;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ClientControlActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_client_control);

            //Sets the NavigationDrawer for the Activity and sets the selected item in the NavigationDrawer to Home
            super.onCreateDrawer();
            super.setSelectedNavItem(R.id.nav_client_control);

            //Sets the TextChangedListener for the text_search_client, which will perform a search when a key is pressed
            final EditText txtSearchClient = (EditText) findViewById(R.id.text_search_client);
            txtSearchClient.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchClients(txtSearchClient);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            //Populates the views that need to be displayed on the Activity
            requestClients(null);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Repopulates the views when the Activity is resumed
    @Override
    public void onResume(){
        try{
            super.onResume();
            requestClients(null);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method fetches an ArrayList of the Clients that match the search term entered by the user
    public void searchClients(EditText txtSearchClient){
        try{
            //Fetches the search term and requests Clients that match the search term
            String searchTerm = txtSearchClient.getText().toString();
            requestClients(searchTerm);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method calls the FirebaseService class and requests the Clients from the Firebase Database
    public void requestClients(String searchTerm){
        try{
            //Displays ProgressBar
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
            progressBar.setVisibility(View.VISIBLE);

            //Requests location information from the LocationService class
            String firebaseKey = new User(this).getUserKey();
            Intent intent = new Intent(getApplicationContext(), FirebaseService.class);
            intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
            intent.setAction(FirebaseService.ACTION_FETCH_CLIENTS);
            intent.putExtra(FirebaseService.SEARCH_TERM, searchTerm);
            intent.putExtra(FirebaseService.RECEIVER, new DataReceiver(new Handler()));
            startService(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method populates the ListView with the client data stored in the database
    public void displayClients(final ArrayList<Client> lstClients){
        try{
            if(lstClients.size() > 0){
                //Sets the ListViewAdapter for list_view_clients
                ClientReportListViewAdapter adapter = new ClientReportListViewAdapter(this, lstClients);
                ListView listView = (ListView) findViewById(R.id.list_view_clients);
                listView.setAdapter(adapter);

                //Sets an OnItemClickListener on the ListView, which will take the user to the ClientActivity, where the user will be able to update information about the Clients
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                        Intent intent = new Intent(ClientControlActivity.this, ClientActivity.class);
                        intent.putExtra("action", "update");
                        intent.putExtra("clientObject", lstClients.get(pos));
                        startActivity(intent);
                    }
                });
            }
            else{
                Toast.makeText(getApplicationContext(), "There are currently no Clients added", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method takes the user to the ClientActivity
    public void addClientOnClick(View view){
        try{
            Intent intent = new Intent(ClientControlActivity.this, ClientActivity.class);
            intent.putExtra("action", "add");
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Creates a ResultReceiver to retrieve information from the FirebaseService
    private class DataReceiver extends ResultReceiver {
        private DataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            if(resultCode == FirebaseService.ACTION_FETCH_CLIENTS_RESULT_CODE){
                ArrayList<Client> lstClients = (ArrayList<Client>) resultData.getSerializable(FirebaseService.ACTION_FETCH_CLIENTS);

                //Displays error message if there are no Stock items to display
                if(lstClients.size() == 0){
                    Toast.makeText(getApplicationContext(), "There are currently no Clients added", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Clients fetched", Toast.LENGTH_LONG).show();
                }
                displayClients(lstClients);

                //Hides ProgressBar
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }
}