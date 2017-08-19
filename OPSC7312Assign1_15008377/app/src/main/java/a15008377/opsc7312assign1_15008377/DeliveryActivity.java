/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class allows you to add and update Delivery information
 */

package a15008377.opsc7312assign1_15008377;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class DeliveryActivity extends AppCompatActivity {
    //Declarations
    ArrayList<DeliveryItem> lstDeliveryItems;
    String action;
    String firebaseAction;
    ArrayList<Stock> lstStock = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_delivery);

            //Methods display all required information for the Activity
            displayViews();
            firebaseAction = "stock";
            requestStockItems();
            requestClients();

            //Makes ListView scrollable in a ScrollView (see https://stackoverflow.com/questions/18367522/android-list-view-inside-a-scroll-view)
            ListView listView = (ListView) findViewById(R.id.list_view_delivery_items);
            listView.setOnTouchListener(new View.OnTouchListener() {
                //Sets onTouchListener to allow scrolling in the ListView within a ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Takes the user back to the DeliveryControlActivity when the back button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try{
            int id = item.getItemId();

            //Takes the user back to the DeliveryControlActivity if the button that was pressed was the back button
            if (id == android.R.id.home) {
                Intent intent = new Intent(DeliveryActivity.this, DeliveryControlActivity.class);
                startActivity(intent);
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    //Method calls the FirebaseService class and requests the Clients from the Firebase Database
    public void requestClients(){
        try{
            //Requests location information from the LocationService class
            String firebaseKey = new User(this).getUserKey();
            Intent intent = new Intent(getApplicationContext(), FirebaseService.class);
            intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
            intent.setAction(FirebaseService.ACTION_FETCH_CLIENTS);
            intent.putExtra(FirebaseService.RECEIVER, new DataReceiver(new Handler()));
            startService(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
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

    //Method alters Activity based on the action the user is performing
    public void displayViews(){
        try{
            //Fetches the user's action from the Bundle
            Bundle bundle = getIntent().getExtras();
            action = bundle.getString("action");
            Button button = (Button) findViewById(R.id.button_add_delivery);
            lstDeliveryItems = new ArrayList<>();

            //Changes Activity based on the user's action
            if(action.equals("update")){
                EditText txtDeliveryID = (EditText) findViewById(R.id.text_delivery_id);
                txtDeliveryID.setEnabled(false);
                button.setText(R.string.button_update_delivery);
                Delivery delivery = (Delivery) bundle.getSerializable("deliveryObject");
                displayDelivery(delivery);
                displayDeliveryItems(delivery.getLstDeliveryItems());
            }
            else if(action.equals("add")){
                button.setText(R.string.button_add_delivery);

                //Sets Adapter for the list_view_delivery_items ListView (there will be no data initially as the user is adding a new Delivery
                ListView listView = (ListView) findViewById(R.id.list_view_delivery_items);
                DeliveryItemListViewAdapter adapter = new DeliveryItemListViewAdapter(this, new ArrayList<DeliveryItem>());
                listView.setAdapter(adapter);

                //Sets DataSetObserver for the ListView's adapter, which will update the items displayed in the Spinner for Delivery Items whenever an item is added to/removed from the ListView
                adapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        firebaseAction = "stock";
                        requestStockItems();
                    }
                });
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

    //Method displays the Delivery object's values
    public void displayDelivery(Delivery delivery){
        try{
            //View assignments
            EditText txtDeliveryID = (EditText) findViewById(R.id.text_delivery_id);
            TextView txtDeliveryDate = (TextView) findViewById(R.id.text_delivery_date);

            //Populate view data
            txtDeliveryID.setText(delivery.getDeliveryID());
            txtDeliveryDate.setText(delivery.getDeliveryDate());
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method populates the spinner_delivery_client with all available clients
    public void displaySpinnerClients(ArrayList<Client> lstClients){
        try{
            //Sets Adapter for the Spinner using lstClients
            ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_row, R.id.text_spinner_item_id);
            for(int i = 0; i < lstClients.size(); i++){
                adapter.add(lstClients.get(i).getClientID() + " - " + lstClients.get(i).getClientName());
            }
            Spinner spinner = (Spinner) findViewById(R.id.spinner_delivery_client);
            spinner.setAdapter(adapter);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Requests Stock Items from the Firebase Database
    public void requestStockItems(){
        try{
            //Requests location information from the LocationService class
            String firebaseKey = new User(this).getUserKey();
            Intent intent = new Intent(getApplicationContext(), FirebaseService.class);
            intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
            intent.setAction(FirebaseService.ACTION_FETCH_STOCK);
            intent.putExtra(FirebaseService.RECEIVER, new DataReceiver(new Handler()));
            startService(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method populates the spinner_delivery_items with items from the Stock text file, and removes the items that have been taken by the current Delivery already
    public void displaySpinnerDeliveryItems(ArrayList<Stock> lstStock){
        try{
            Spinner spinner = (Spinner) findViewById(R.id.spinner_delivery_items);
            ArrayAdapter<String> adapter;
            ArrayList<String> lstItems = new ArrayList<>();
            ArrayList<String> lstUsedStock = new ArrayList<>();

            //Removes the items that have already been added to the delivery from the Spinner (used for updates only)
            ListView listViewDeliveryItems = (ListView) findViewById(R.id.list_view_delivery_items);
            Adapter lstDeliveryItems =  listViewDeliveryItems.getAdapter();

            //Adds Stock that has been used by the Delivery to the lstUsedStock ArrayList
            for(int i = 0; i < lstDeliveryItems.getCount(); i++){
                DeliveryItem item = (DeliveryItem) lstDeliveryItems.getItem(i);
                lstUsedStock.add(item.getDeliveryStockID());
            }

            //Adds stock items that haven't been used by the Delivery yet to the Spinner
            for(int i = 0; i < lstStock.size(); i++){
                if(!lstUsedStock.contains(lstStock.get(i).getStockID())){
                    lstItems.add(lstStock.get(i).getStockID() + " - " + lstStock.get(i).getStockDescription());
                }
            }

            //Sets the adapter for the Spinner
            adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_row, R.id.text_spinner_item_id, lstItems);
            spinner.setAdapter(adapter);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method displays the Delivery's selected items in the list_view_delivery_items
    public void displayDeliveryItems(ArrayList<DeliveryItem> lstDeliveryItems){
        try{
            //Sets adapter for the ListView
            DeliveryItemListViewAdapter deliveryItemListViewAdapter = new DeliveryItemListViewAdapter(this, lstDeliveryItems);
            ListView listView = (ListView) findViewById(R.id.list_view_delivery_items);
            listView.setAdapter(deliveryItemListViewAdapter);

            //Sets DataSetObserver for the ListView's adapter, which will update the items displayed in the Spinner for Delivery Items whenever an item is added to/removed from the ListView
            deliveryItemListViewAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                }
            });
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method displays a DatePickerDialog for the user to choose the Delivery date
    public void chooseDateOnClick(View view){
        try{
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog (this, new DatePickerDialog.OnDateSetListener(){
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    //Displays the chosen date in the text_delivery_date TextView
                    TextView txtDate = (TextView) findViewById(R.id.text_delivery_date);
                    txtDate.setText((dayOfMonth + "/" + (month + 1) + "/" + year));
                }
            },calendar.get(Calendar.YEAR) , calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method adds/updates the Delivery details to the database
    public void addDeliveryOnClick(View view){
        try {
            //Displays ProgressBar
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
            progressBar.setVisibility(View.VISIBLE);

            firebaseAction = "";
            requestStockItems();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //Method reads the list_view_delivery_items ListView and returns an ArrayList containing all the DeliveryItem objects within it
    public ArrayList<DeliveryItem> getDeliveryItems() throws NullPointerException{
        ArrayList<DeliveryItem> lstDeliveryItems = new ArrayList<>();

        try{
            ListView listView = (ListView) findViewById(R.id.list_view_delivery_items);
            for(int i = 0; i < listView.getCount(); i++){
                lstDeliveryItems.add((DeliveryItem) listView.getItemAtPosition(i));
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return lstDeliveryItems;
    }

    //Method adds the a DeliveryItem to the list_view_delivery_items ListView
    public void addDeliveryItemOnClick(View view){
        try{
            Spinner spinner = (Spinner) findViewById(R.id.spinner_delivery_items);
            EditText txtQuantity = (EditText) findViewById(R.id.text_delivery_item_quantity);
            ArrayAdapter<String> spinnerAdapter = (ArrayAdapter<String>) spinner.getAdapter();

            if(spinnerAdapter.getCount() == 0){
                Toast.makeText(getApplicationContext(), "All items of stock have been added to the delivery already. You can change their quantities by clicking the + and - buttons in the list", Toast.LENGTH_LONG).show();
            }
            else{
                //Creates a new DeliveryItem object and adds it to the lstDeliveryItems ArrayList
                String deliveryItemID = spinner.getSelectedItem().toString();
                int deliveryItemQuantity = Integer.parseInt(txtQuantity.getText().toString());
                deliveryItemID = deliveryItemID.substring(0, deliveryItemID.indexOf(" "));
                DeliveryItem deliveryItem = new DeliveryItem(deliveryItemID, deliveryItemQuantity);
                lstDeliveryItems.add(deliveryItem);
                spinnerAdapter.remove(spinner.getSelectedItem().toString());
                spinnerAdapter.notifyDataSetChanged();

                //Updates the adapter for list_view_delivery_items
                ListView listView = (ListView) findViewById(R.id.list_view_delivery_items);
                DeliveryItemListViewAdapter deliveryItemListViewAdapter = (DeliveryItemListViewAdapter) listView.getAdapter();
                deliveryItemListViewAdapter.add(deliveryItem);
                deliveryItemListViewAdapter.notifyDataSetChanged();
            }
        }
        catch(NullPointerException nfe){
            Toast.makeText(getApplicationContext(), "There are currently no items in Stock, please go to Stock Control to add Stock items before scheduling a Delivery", Toast.LENGTH_LONG).show();
        }
        catch(NumberFormatException nfe){
            Toast.makeText(this, "Please enter a whole number for the Delivery Item Quantity", Toast.LENGTH_SHORT).show();
        }
        catch(Exception exc){
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //Method writes the Delivery details to the Firebase database
    public void saveDeliveryDetails(final ArrayList<Stock> lstStock, ArrayList<Delivery> lstDeliveries){
        try{
            //Assignments
            EditText txtDeliveryID = (EditText) findViewById(R.id.text_delivery_id);
            TextView txtDeliveryDate = (TextView) findViewById(R.id.text_delivery_date);
            Spinner spinner = (Spinner) findViewById(R.id.spinner_delivery_client);
            boolean enoughStock = true;

            //Assigns values to the Delivery object
            String deliveryID = txtDeliveryID.getText().toString();
            String deliveryDate = txtDeliveryDate.getText().toString();
            String clientID = spinner.getSelectedItem().toString();
            clientID = clientID.substring(0, clientID.indexOf(" "));
            ArrayList<DeliveryItem> lstDeliveryItems = getDeliveryItems();
            final ArrayList<Stock> lstUpdatedStockItems = new ArrayList<>();

            //Loops through available Stock to ensure that there is enough Stock available to cater for the Delivery
            for(int i = 0; i < lstDeliveryItems.size(); i++){
                String deliveryStockID = lstDeliveryItems.get(i).getDeliveryStockID();
                int numberOfItems = lstDeliveryItems.get(i).getDeliveryItemQuantity();
                for(int j = 0; j < lstStock.size(); j++){
                    String stockID = lstStock.get(j).getStockID();
                    int availableStockQuantity = lstStock.get(j).getStockQuantity();

                    //Checks if there is enough Stock of each item to cater for the Delivery
                    if(deliveryStockID.equals(stockID)){
                        //Resets the available Stock quantity when updating the Delivery, before subtracting the new number of Stock items
                        if(action.equals("update")){
                            for(Delivery delivery : lstDeliveries){
                                if(delivery.getDeliveryID().equals(deliveryID)){
                                    for(DeliveryItem deliveryItem : delivery.getLstDeliveryItems()){
                                        if(deliveryItem.getDeliveryStockID().equals(deliveryStockID)){
                                            int oldQuantity = deliveryItem.getDeliveryItemQuantity();
                                            availableStockQuantity += oldQuantity;
                                        }
                                    }
                                }
                            }
                        }

                        if(numberOfItems > availableStockQuantity){
                            Toast.makeText(getApplicationContext(), "There are only " + availableStockQuantity + " item/s left of " + deliveryStockID + " in stock. Please reduce the number of " + deliveryStockID + " items for this delivery", Toast.LENGTH_LONG).show();
                            enoughStock = false;
                        }
                        else{
                            lstStock.get(j).setStockQuantity(availableStockQuantity - numberOfItems);
                            lstUpdatedStockItems.add(lstStock.get(j));
                        }
                    }
                }
            }

            //Writes the Delivery details to the database if there is enough Stock of each item for the Delivery
            if(enoughStock){
                final Delivery delivery = new Delivery(deliveryID, clientID, deliveryDate, 0, getDeliveryItems());

                //Writes the Delivery details to the database if the information is valid
                if(delivery.validateDelivery(this)){
                    requestWriteOfDelivery(delivery, action);

                    updateStockLevels(lstUpdatedStockItems);
                }
            }
        }
        catch(NullPointerException npe){
            Spinner spnClients = (Spinner) findViewById(R.id.spinner_delivery_client);

            //Displays appropriate error message based on the quantities of the Spinners
            if(spnClients.getCount() == 0){
                Toast.makeText(getApplicationContext(), "There are currently no Clients in the database, please go to Client Control to add Clients before scheduling a Delivery", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(), npe.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //Method calls the FirebaseService class and passes in a Delivery object that must be written to the Firebase database
    public void requestWriteOfDelivery(Delivery delivery, String action){
        try{
            //Displays ProgressBar
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
            progressBar.setVisibility(View.VISIBLE);

            //Requests location information from the LocationService class
            String firebaseKey = new User(this).getUserKey();
            Intent intent = new Intent(getApplicationContext(), FirebaseService.class);
            intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
            intent.setAction(FirebaseService.ACTION_WRITE_DELIVERY);
            intent.putExtra(FirebaseService.ACTION_WRITE_DELIVERY, delivery);
            intent.putExtra(FirebaseService.ACTION_WRITE_DELIVERY_INFORMATION, action);
            intent.putExtra(FirebaseService.RECEIVER, new DataReceiver(new Handler()));
            startService(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method receives an ArrayList of Stock items with an updated quantity, and updates the available quantity of stock in the Firebase Database
    public void updateStockLevels(final ArrayList<Stock> lstStock){
        try{
            //Gets Firebase Database reference
            final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = firebaseDatabase.getReference().child(new User(this).getUserKey()).child("stock");

            //Adds Listeners for when the data is changed
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(int i = 0; i < lstStock.size(); i++){
                        databaseReference.child(lstStock.get(i).getStockID()).setValue(lstStock.get(i));
                    }
                    databaseReference.removeEventListener(this);
                    Toast.makeText(getApplicationContext(), "Stock levels updated", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.i("Data", "Failed to read data, please check your internet connection");
                }
            });
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
            if(resultCode == FirebaseService.ACTION_FETCH_STOCK_RESULT_CODE){
                lstStock = (ArrayList<Stock>) resultData.getSerializable(FirebaseService.ACTION_FETCH_STOCK);

                //Displays error message if there are no Stock items to display
                if(lstStock.size() > 0){
                    if(firebaseAction.equals("stock")){
                        displaySpinnerDeliveryItems(lstStock);
                    }
                    else{
                        requestDeliveries(null);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "There are currently no Stock items added", Toast.LENGTH_LONG).show();
                }
            }
            else if(resultCode == FirebaseService.ACTION_FETCH_CLIENTS_RESULT_CODE){
                ArrayList<Client> lstClients = (ArrayList<Client>) resultData.getSerializable(FirebaseService.ACTION_FETCH_CLIENTS);

                //Displays error message if there are no Stock items to display
                if(lstClients.size() > 0){
                    displaySpinnerClients(lstClients);
                }
                else{
                    Toast.makeText(getApplicationContext(), "There are currently no Clients added", Toast.LENGTH_LONG).show();
                }
            }
            else if(resultCode == FirebaseService.ACTION_WRITE_DELIVERY_RESULT_CODE){
                boolean success = resultData.getBoolean(FirebaseService.ACTION_WRITE_DELIVERY);

                if(success){
                    Intent intent;
                    if(action.equals("add")){
                        Toast.makeText(getApplicationContext(), "Delivery successfully added", Toast.LENGTH_LONG).show();
                        intent = getIntent();
                        finish();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Delivery successfully updated", Toast.LENGTH_LONG).show();

                        //Takes the user back to the DeliveryControlActivity once the update is complete
                        intent = new Intent(DeliveryActivity.this, DeliveryControlActivity.class);
                    }
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "The Delivery ID has already been used, please choose another Delivery ID", Toast.LENGTH_LONG).show();
                }

                //Hides ProgressBar
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
                progressBar.setVisibility(View.INVISIBLE);
            }
            else if(resultCode == FirebaseService.ACTION_FETCH_DELIVERIES_RESULT_CODE){
                ArrayList<Delivery> lstDeliveries = (ArrayList<Delivery>) resultData.getSerializable(FirebaseService.ACTION_FETCH_DELIVERIES);
                saveDeliveryDetails(lstStock, lstDeliveries);

                //Hides ProgressBar
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }
}