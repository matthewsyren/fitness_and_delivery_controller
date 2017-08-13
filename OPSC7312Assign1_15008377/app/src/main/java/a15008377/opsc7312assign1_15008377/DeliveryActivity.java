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
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DeliveryActivity extends AppCompatActivity {
    //Declarations
    ArrayList<DeliveryItem> lstDeliveryItems;
    String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_delivery);

            //Methods display all required information for the Activity
            displayViews();
            displaySpinnerClients();
            getStockItems();
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
                        getStockItems();
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
            displayDeliveryItems(delivery.getDeliveryID());
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method populates the spinner_delivery_client with all available clients
    public void displaySpinnerClients(){
        try{
            //Gets reference to Firebase
            final ArrayList<String> lstClients = new ArrayList<>();
            final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = firebaseDatabase.getReference().child(new User(this).getUserKey()).child("clients");

            //Adds Listeners for when the data is changed
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Loops through all Clients and adds them to the lstClients ArrayList
                    Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();
                    for(DataSnapshot snapshot : lstSnapshots){
                        //Retrieves the Clients from Firebase and adds the Clients to an ArrayList of Client objects
                        Client client = snapshot.getValue(Client.class);
                        lstClients.add(client.getClientID() + " - " + client.getClientName());
                    }
                    databaseReference.removeEventListener(this);

                    //Displays error message if there are no Clients to display
                    if(lstClients.size() > 0){
                        //Sets Adapter for the Spinner using lstClients
                        ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_row, R.id.text_spinner_item_id, lstClients);
                        Spinner spinner = (Spinner) findViewById(R.id.spinner_delivery_client);
                        spinner.setAdapter(adapter);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "There are currently no Clients added", Toast.LENGTH_LONG).show();
                    }
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

    public void getStockItems(){
        try{
            //Gets reference to Firebase
            final ArrayList<Stock> lstStock = new ArrayList<>();
            final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = firebaseDatabase.getReference().child(new User(this).getUserKey()).child("stock");

            //Adds Listeners for when the data is changed
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Loops through all Stock and adds them to the lstStock ArrayList
                    Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();
                    for(DataSnapshot snapshot : lstSnapshots){
                        //Retrieves the Stock from Firebase and adds the Stock to an ArrayList of Stock objects
                        Stock stock = snapshot.getValue(Stock.class);
                        lstStock.add(stock);
                    }
                    databaseReference.removeEventListener(this);

                    //Displays error message if there are no Stock items to display
                    if(lstStock.size() > 0){
                        displaySpinnerDeliveryItems(lstStock);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "There are no currently no Stock items added", Toast.LENGTH_LONG).show();
                    }
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
    public void displayDeliveryItems(String deliveryID){
        try{
            /*DBAdapter dbAdapter = new DBAdapter(this);
            dbAdapter.open();
            Cursor cursor = dbAdapter.getDeliveryItems(deliveryID);
            ArrayList<DeliveryItem> lstDeliveryItems = new ArrayList<>();

            if(cursor.moveToFirst()){
                do{
                    DeliveryItem deliveryItem = new DeliveryItem(cursor.getString(0), cursor.getInt(1));
                    lstDeliveryItems.add(deliveryItem);
                }while(cursor.moveToNext());

                //Sets adapter for the ListView
                DeliveryItemListViewAdapter deliveryItemListViewAdapter = new DeliveryItemListViewAdapter(this, lstDeliveryItems);
                ListView listView = (ListView) findViewById(R.id.list_view_delivery_items);
                listView.setAdapter(deliveryItemListViewAdapter);

                //Sets DataSetObserver for the ListView's adapter, which will update the items displayed in the Spinner for Delivery Items whenever an item is added to/removed from the ListView
                deliveryItemListViewAdapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        displaySpinnerDeliveryItems();
                    }
                });
            }
            dbAdapter.close(); */
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
            //Gets reference to Firebase
            final ArrayList<Stock> lstStock = new ArrayList<>();
            final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = firebaseDatabase.getReference().child(new User(this).getUserKey()).child("stock");

            //Adds Listeners for when the data is changed
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Loops through all Stock and adds them to the lstStock ArrayList
                    Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();
                    for(DataSnapshot snapshot : lstSnapshots){
                        //Retrieves the Stock from Firebase and adds the Stock to an ArrayList of Stock objects
                        Stock stock = snapshot.getValue(Stock.class);
                        lstStock.add(stock);
                    }
                    databaseReference.removeEventListener(this);

                    //Displays error message if there are no Stock items to display
                    if(lstStock.size() > 0){
                        saveDeliveryDetails(lstStock);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "There are no currently no Stock items added", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.i("Data", "Failed to read data, please check your internet connection");
                }
            });
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
    public void saveDeliveryDetails(final ArrayList<Stock> lstStock){
        try{
            //Assignments
            EditText txtDeliveryID = (EditText) findViewById(R.id.text_delivery_id);
            TextView txtDeliveryDate = (TextView) findViewById(R.id.text_delivery_date);
            Spinner spinner = (Spinner) findViewById(R.id.spinner_delivery_client);
            Intent intent = null;
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
                            /*DBAdapter dbAdapter = new DBAdapter(this);
                            dbAdapter.open();
                            Cursor cursor = dbAdapter.getDeliveryItem(deliveryID, deliveryStockID);
                            if(cursor.moveToFirst()){
                                int oldQuantity = cursor.getInt(1);
                                availableStockQuantity += oldQuantity;
                            }
                            dbAdapter.close(); */
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

                    //Gets Firebase Database reference
                    final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    final DatabaseReference databaseReference = firebaseDatabase.getReference().child(new User(this).getUserKey()).child("deliveries");

                    //Adds Listeners for when the data is changed
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Intent intent = null;
                            //Loops through all runs and adds them to the lstRuns ArrayList
                            if(action.equals("add")){
                                if(dataSnapshot.child(delivery.getDeliveryID()).exists()){
                                    Toast.makeText(getApplicationContext(), "The Delivery ID you have entered already exists, please choose another one", Toast.LENGTH_LONG).show();
                                }
                                else{
                                    databaseReference.child(delivery.getDeliveryID()).setValue(delivery);
                                    Toast.makeText(getApplicationContext(), "Delivery successfully added", Toast.LENGTH_LONG).show();

                                    updateStockLevels(lstUpdatedStockItems);

                                    //Restarts Activity (clears Views to allow user to enter another Delivery)
                                    intent = getIntent();
                                    finish();
                                }
                            }
                            else{
                                databaseReference.child(delivery.getDeliveryID()).setValue(delivery);
                                Toast.makeText(getApplicationContext(), "Delivery successfully updated", Toast.LENGTH_LONG).show();

                                //Takes the user back to the ClientControlActivity once the update is complete
                                intent = new Intent(DeliveryActivity.this, DeliveryControlActivity.class);
                            }
                            databaseReference.removeEventListener(this);
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.i("Data", "Failed to read data, please check your internet connection");
                        }
                    });


                   /* if(action.equals("add") && delivery.checkDeliveryDate(this)){
                        //Writes the Delivery details to the database when the user adds a new Delivery
                        dbAdapter.insertDelivery(delivery);
                        Toast.makeText(getApplicationContext(), "Delivery successfully added", Toast.LENGTH_LONG).show();
                        dbAdapter.insertDeliveryItems(delivery.getDeliveryID(), delivery.getLstDeliveryItems());

                        //Resets the Activity to allow the user to add new Deliveries
                        intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                    else if(action.equals("update")){
                        //Updates the Delivery, and rewrites its Delivery Items to the database when the user updates a Delivery
                        dbAdapter.updateDelivery(delivery);
                        dbAdapter.deleteDeliveryItems(delivery.getDeliveryID());
                        dbAdapter.insertDeliveryItems(delivery.getDeliveryID(), delivery.getLstDeliveryItems());
                        new Stock().rewriteFile(lstStock, this);
                        Toast.makeText(getApplicationContext(), "Updated delivery successfully", Toast.LENGTH_LONG).show();

                        //Takes the user back to the DeliveryControlActivity once the update is complete
                        intent = new Intent(DeliveryActivity.this, DeliveryControlActivity.class);
                        startActivity(intent);
                    }
                    dbAdapter.close(); */
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
}