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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
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

            //Sets the onKeyListener for the text_search_client, which will perform a search when the enter key is pressed
            final EditText txtSearchDelivery = (EditText) findViewById(R.id.text_search_delivery);
            txtSearchDelivery.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_ENTER){
                        String searchTerm = txtSearchDelivery.getText().toString();
                        searchDeliveries(searchTerm);

                        //Hides keyboard once search is completed
                        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        //Displays message to user
                        Toast.makeText(getApplicationContext(), "Search complete!", Toast.LENGTH_LONG).show();
                        return true;
                    }
                    return false;
                }
            });
            populateViews();
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
            populateViews();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method populates the views that are displayed on this Activity
    public void populateViews(){
        try{
            getIncompleteDeliveries();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method fetches the Deliveries that match the search result and send them to the displayDeliveries method
    public void searchDeliveries(String searchTerm){
        try{
            ArrayList<Delivery> lstSearchResults = Delivery.searchDeliveries(searchTerm, this, 0);
            displayDeliveries(lstSearchResults);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    //Method fetches all Deliveries and sends them to the displayDeliveries method
    public void getIncompleteDeliveries(){
        try{
            //Gets reference to Firebase
            final ArrayList<Delivery> lstDeliveries = new ArrayList<>();
            final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = firebaseDatabase.getReference().child(new User(this).getUserKey()).child("deliveries");

            //Adds Listeners for when the data is changed
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Loops through all Deliveries and adds them to the lstClients ArrayList
                    Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();
                    for(DataSnapshot snapshot : lstSnapshots){
                        //Retrieves the Deliveries from Firebase and adds the Deliveries to an ArrayList of Delivery objects
                        Delivery delivery = snapshot.getValue(Delivery.class);
                        if(delivery.getDeliveryComplete() == 0){
                            lstDeliveries.add(delivery);
                        }
                    }
                    databaseReference.removeEventListener(this);

                    //Displays error message if there are no Deliveries to display
                    if(lstDeliveries.size() > 0){
                        displayDeliveries(lstDeliveries);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "There are currently no Deliveries added", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.i("Data", "Failed to read data, please check your internet connection");
                }
            });
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    //Method populates the ListView on this Activity
    public void displayDeliveries(final ArrayList<Delivery> lstDeliveries){
        try{
            if(lstDeliveries.size() > 0){
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
                        startActivity(intent);
                    }
                });
            }
            else{
                Toast.makeText(getApplicationContext(), "There are currently no Deliveries added", Toast.LENGTH_LONG).show();
            }
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
}
