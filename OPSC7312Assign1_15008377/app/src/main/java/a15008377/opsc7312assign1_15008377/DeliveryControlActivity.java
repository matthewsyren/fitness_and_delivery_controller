/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class displays a report of all incomplete Deliveries
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DeliveryControlActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_delivery_control);

            //Sets the NavigationDrawer for the Activity and sets the selected item in the NavigationDrawer to Home
            super.onCreateDrawer();
            super.setSelectedNavItem(R.id.nav_delivery_control);

            //Sets the TextChangedListener for the text_search_delivery, which will perform a search when a key is pressed
            final EditText txtSearchDelivery = (EditText) findViewById(R.id.text_search_delivery);
            txtSearchDelivery.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchDeliveries(txtSearchDelivery);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            requestDeliveries(null);
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
            requestDeliveries(null);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method fetches the Deliveries that match the search result and send them to the displayDeliveries method
    public void searchDeliveries(EditText txtSearchDelivery){
        try{
            //Fetches the search term and requests Deliveries that match the search term
            String searchTerm = txtSearchDelivery.getText().toString();
            requestDeliveries(searchTerm);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    //Method calls the FirebaseService class and requests the Clients from the Firebase Database
    public void requestDeliveries(String searchTerm){
        try{
            //Displays ProgressBar
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
            progressBar.setVisibility(View.VISIBLE);

            //Requests location information from the LocationService class
            String firebaseKey = new User(this).getUserKey();
            Intent intent = new Intent(getApplicationContext(), FirebaseService.class);
            intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
            intent.setAction(FirebaseService.ACTION_FETCH_DELIVERIES);
            intent.putExtra(FirebaseService.DELIVERY_COMPLETE, 0);
            intent.putExtra(FirebaseService.SEARCH_TERM, searchTerm);
            intent.putExtra(FirebaseService.RECEIVER, new DataReceiver(new Handler()));
            startService(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method populates the ListView on this Activity
    public void displayDeliveries(final ArrayList<Delivery> lstDeliveries){
        try{
            //Sets the Adapter for the list_view_deliveries ListView
            DeliveryReportListViewAdapter adapter = new DeliveryReportListViewAdapter(this, lstDeliveries);
            final ListView listView = (ListView) findViewById(R.id.list_view_deliveries);
            listView.setAdapter(adapter);

            //Sets OnItemClickListener, which will pass the information of the Delivery clicked to the DeliveryActivity
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(DeliveryControlActivity.this, DeliveryActivity.class);
                    intent.putExtra("action", "update");
                    intent.putExtra("deliveryObject", lstDeliveries.get(position));
                    startActivity(intent);}
            });
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method takes the user to the DeliveryActivity
    public void addDeliveryOnClick(View view){
        try{
            Intent intent = new Intent(DeliveryControlActivity.this, DeliveryActivity.class);
            intent.putExtra("action", "add");
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    //Creates a ResultReceiver to retrieve information from the FirebaseService
    private class DataReceiver extends ResultReceiver {
        private DataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            if(resultCode == FirebaseService.ACTION_FETCH_DELIVERIES_RESULT_CODE){
                ArrayList<Delivery> lstDeliveries = (ArrayList<Delivery>) resultData.getSerializable(FirebaseService.ACTION_FETCH_DELIVERIES);

                //Displays error message if there are no Delivery items to display
                if(lstDeliveries.size() == 0){
                    Toast.makeText(getApplicationContext(), "There are currently no Deliveries added", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Deliveries fetched", Toast.LENGTH_LONG).show();
                }
                displayDeliveries(lstDeliveries);

                //Hides ProgressBar
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }
}