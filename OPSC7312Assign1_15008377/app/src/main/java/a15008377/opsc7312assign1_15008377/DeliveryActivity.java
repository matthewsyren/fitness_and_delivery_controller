/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class allows you to add and update Delivery information
 */

package a15008377.opsc7312assign1_15008377;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
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
import java.util.TimeZone;

public class DeliveryActivity extends AppCompatActivity {
    //Declarations
    private String action;
    private String firebaseAction;
    private ArrayList<Stock> lstStock = new ArrayList<>();
    private ArrayList<DeliveryItem> lstOriginalDeliveryItems = new ArrayList<>();
    private Delivery newDelivery;
    private ArrayList<Client> lstClients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_delivery);

            //Methods display all required information for the Activity
            displayViews();
            firebaseAction = "stock";
            new Stock().requestStockItems(null, this, new DataReceiver(new Handler()));
            new Client().requestClients(null, this, new DataReceiver(new Handler()));
            checkPermissions();

            //Makes ListView within a ScrollView scrollable (Learnt from https://stackoverflow.com/questions/18367522/android-list-view-inside-a-scroll-view)
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

    //Method checks the permissions required for this page, and requests the permissions if they haven't been granted
    public void checkPermissions(){
        try{
            //Checks for permission to send an SMS, and requests the permission if it is not granted
            if(ContextCompat.checkSelfPermission(DeliveryActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(DeliveryActivity.this, new String[]{Manifest.permission.SEND_SMS}, 1);
            }

            //Checks for permission to write to the phone's calendar, and asks for permission if the permission is disabled
            if(ContextCompat.checkSelfPermission(DeliveryActivity.this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(DeliveryActivity.this, new String[]{Manifest.permission.WRITE_CALENDAR}, PackageManager.PERMISSION_GRANTED);
            }
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
            //Displays ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);

            //Fetches the user's action from the Bundle
            Bundle bundle = getIntent().getExtras();
            action = bundle.getString("action");
            Button button = (Button) findViewById(R.id.button_add_delivery);

            //Changes Activity based on the user's action (add or update)
            if(action.equals("update")){
                EditText txtDeliveryID = (EditText) findViewById(R.id.text_delivery_id);
                txtDeliveryID.setEnabled(false);
                button.setText(R.string.button_update_delivery);
                Delivery delivery = (Delivery) bundle.getSerializable("deliveryObject");
                displayDelivery(delivery);
                displayDeliveryItems(delivery.getLstDeliveryItems());
                for(DeliveryItem deliveryItem : delivery.getLstDeliveryItems()){
                    lstOriginalDeliveryItems.add(deliveryItem);
                }
            }
            else if(action.equals("add")){
                button.setText(R.string.button_add_delivery);

                //Sets Adapter for the list_view_delivery_items ListView (there will be no data initially as the user is adding a new Delivery
                ListView listView = (ListView) findViewById(R.id.list_view_delivery_items);
                DeliveryItemListViewAdapter deliveryItemListViewAdapter = new DeliveryItemListViewAdapter(this, new ArrayList<DeliveryItem>());
                listView.setAdapter(deliveryItemListViewAdapter);

                //Sets DataSetObserver for the ListView's adapter, which will update the items displayed in the Spinner for Delivery Items whenever an item is added to/removed from the ListView
                deliveryItemListViewAdapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        firebaseAction = "stock";
                        new Stock().requestStockItems(null, getApplicationContext(), new DataReceiver(new Handler()));
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
    public void displaySpinnerClients(){
        try{
            //Sets Adapter for the Spinner using lstClients
            ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_row, R.id.text_spinner_item_id);
            for(int i = 0; i < lstClients.size(); i++){
                adapter.add(lstClients.get(i).getClientID() + " - " + lstClients.get(i).getClientName());
            }
            Spinner spinner = (Spinner) findViewById(R.id.spinner_delivery_client);
            spinner.setAdapter(adapter);

            //Sets the selected index if the Delivery is being updated to the current Client for the Delivery
            if(action.equals("update")){
                Bundle bundle = getIntent().getExtras();
                Delivery delivery = (Delivery) bundle.getSerializable("deliveryObject");
                for(int i = 0; i < lstClients.size(); i++){
                    if(lstClients.get(i).getClientID().equals(delivery.getDeliveryClientID())){
                        spinner.setSelection(i);
                        break;
                    }
                }
            }

            //Hides ProgressBar
            toggleProgressBarVisibility(View.INVISIBLE);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method populates the spinner_delivery_items with items from the Stock text file, and removes the items that have been taken by the current Delivery already
    public void displaySpinnerDeliveryItems(ArrayList<Stock> lstStock){
        try{
            //Declarations
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
                    firebaseAction = "stock";
                    new Stock().requestStockItems(null, getApplicationContext(), new DataReceiver(new Handler()));
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
            //Gets Calendar instance and displays a DatePicker
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
            toggleProgressBarVisibility(View.VISIBLE);

            //Sets the firebaseAction variable to nothing (this variable is used in the DataReceiver class to determine which methods to call once the Stock has been downloaded)
            firebaseAction = "";
            new Stock().requestStockItems(null, this, new DataReceiver(new Handler()));
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //Method reads the list_view_delivery_items ListView and returns an ArrayList containing all the DeliveryItem objects within it
    public ArrayList<DeliveryItem> getDeliveryItems() throws NullPointerException{
        ArrayList<DeliveryItem> lstDeliveryItems = new ArrayList<>();

        try{
            //Fetches the DeliveryItem objects that have been added to the Delivery
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
            //Declarations
            Spinner spinner = (Spinner) findViewById(R.id.spinner_delivery_items);
            EditText txtQuantity = (EditText) findViewById(R.id.text_delivery_item_quantity);
            ArrayAdapter<String> spinnerAdapter = (ArrayAdapter<String>) spinner.getAdapter();

            //Adds Stock item to Delivery (if there is a value selected in the Spinner)
            if(spinnerAdapter.getCount() == 0){
                Toast.makeText(getApplicationContext(), "All items of stock have been added to the delivery already. You can change their quantities by clicking the + and - buttons in the list", Toast.LENGTH_LONG).show();
            }
            else{
                //Creates a new DeliveryItem object and adds it to the lstDeliveryItems ArrayList
                String deliveryItemID = spinner.getSelectedItem().toString();
                int deliveryItemQuantity = Integer.parseInt(txtQuantity.getText().toString());

                //Gets the Delivery Item's ID (the space in the Spinner signifies the end of the ID)
                deliveryItemID = deliveryItemID.substring(0, deliveryItemID.indexOf(" "));

                //Removes the Delivery Item from the Spinner
                DeliveryItem deliveryItem = new DeliveryItem(deliveryItemID, deliveryItemQuantity);
                spinnerAdapter.remove(spinner.getSelectedItem().toString());
                spinnerAdapter.notifyDataSetChanged();

                //Updates the adapter for list_view_delivery_items by adding the Delivery Item
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

            //Assigns values that will be used for the Delivery object
            String deliveryID = txtDeliveryID.getText().toString();
            String deliveryDate = txtDeliveryDate.getText().toString();
            String clientID = spinner.getSelectedItem().toString();
            clientID = clientID.substring(0, clientID.indexOf(" "));
            ArrayList<DeliveryItem> lstDeliveryItems = getDeliveryItems();
            final ArrayList<Stock> lstUpdatedStockItems = new ArrayList<>();

            //Loops through the original items from the Delivery (as when updating the Delivery the Delivery Items may change), and determines if the updated Delivery has removed any Delivery Items
            for(DeliveryItem originalDeliveryItem : lstOriginalDeliveryItems){
                boolean found = false;
                for(DeliveryItem deliveryItem : lstDeliveryItems){
                    if(originalDeliveryItem.getDeliveryStockID().equals(deliveryItem.getDeliveryStockID())){
                        found = true;
                        break;
                    }
                }

                //Adds the removed Delivery Item back to the list of Delivery Items for the updated Delivery, but with a quantity of 0 (this is then used to update the available Stock levels of the removed item)
                if(!found){
                    originalDeliveryItem.setDeliveryItemQuantity(0);
                    lstDeliveryItems.add(originalDeliveryItem);
                }
            }

            //Loops through available Stock to ensure that there is enough Stock available to cater for the Delivery
            for(int i = 0; i < lstDeliveryItems.size(); i++){
                String deliveryStockID = lstDeliveryItems.get(i).getDeliveryStockID();
                int numberOfItems = lstDeliveryItems.get(i).getDeliveryItemQuantity();
                for(int j = 0; j < lstStock.size(); j++){
                    String stockID = lstStock.get(j).getStockID();
                    int availableStockQuantity = lstStock.get(j).getStockQuantity();

                    //Resets available Stock quantity for each Stock item
                    if(deliveryStockID.equals(stockID)){
                        //Resets the available Stock quantity when updating the Delivery
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

                        //Checks to see if there is enough Stock to cater for each item
                        if(numberOfItems > availableStockQuantity){
                            Toast.makeText(getApplicationContext(), "There are only " + availableStockQuantity + " item/s left of " + deliveryStockID + " in stock. Please reduce the number of " + deliveryStockID + " items for this delivery", Toast.LENGTH_LONG).show();
                            enoughStock = false;
                        }
                        else{
                            //Available Stock quantity is updated if there is enough Stock to cater for the Delivery (the amount of items used in the Delivery is subtracted from the available Stock quantity)
                            if(numberOfItems < 0){
                                Toast.makeText(getApplicationContext(), "Avail: " + availableStockQuantity + "    Quan: " + numberOfItems, Toast.LENGTH_LONG).show();
                            }
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
                    if(action.equals("update") || (action.equals("add") && delivery.checkDeliveryDate(getApplicationContext()))){
                        //Displays ProgressBar
                        toggleProgressBarVisibility(View.VISIBLE);

                        //Attempts to write Delivery details to the Firebase Database
                        delivery.requestWriteOfDelivery(this, action, new DataReceiver(new Handler()));

                        //Writes the updated available Stock quantities to the Firebase Database
                        newDelivery = delivery;
                        updateStockLevels(lstUpdatedStockItems);

                        //Sends an SMS to the Client that tells them when their Delivery is scheduled for
                        sendSMS(delivery);
                    }
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

    //Method receives an ArrayList of Stock items with an updated quantity, and updates the available quantity of Stock in the Firebase Database
    public void updateStockLevels(final ArrayList<Stock> lstStock){
        try{
            //Gets Firebase Database reference
            final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = firebaseDatabase.getReference().child(new User(this).getUserKey()).child("stock");

            //Adds Listeners for when the data is changed
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Writes the updated Stock to the Firebase Database
                    for(int i = 0; i < lstStock.size(); i++){
                        databaseReference.child(lstStock.get(i).getStockID()).setValue(lstStock.get(i));
                    }

                    //Removes the EventListener for the Firebase Database and displays a message to the user
                    databaseReference.removeEventListener(this);
                    Toast.makeText(getApplicationContext(), "Stock levels updated", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.i("Data", "An error occurred while connecting to Firebase");
                }
            });
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method writes the Delivery details to the calendar
    public void addDeliveryToCalendar() throws SecurityException{
        try{
            //Checks for permission to write to calendar, and updates the Calendar event if permission has been granted
            if(ContextCompat.checkSelfPermission(DeliveryActivity.this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                //Gets Calendar instance and assigns values to the required ContentValues attributes
                Calendar deliveryDate = Calendar.getInstance();
                String dateOfDelivery = newDelivery.getDeliveryDate();
                String[] parsedDeliveryDate = dateOfDelivery.split("/");
                deliveryDate.set(Integer.parseInt(parsedDeliveryDate[2]), Integer.parseInt(parsedDeliveryDate[1]) - 1, Integer.parseInt(parsedDeliveryDate[0]), 8, 0);
                long dateMilliseconds = deliveryDate.getTimeInMillis();
                ContentResolver contentResolver = getContentResolver();
                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.DTSTART, dateMilliseconds);
                values.put(CalendarContract.Events.DTEND, dateMilliseconds);
                values.put(CalendarContract.Events.TITLE, "Delivery " + newDelivery.getDeliveryID());
                values.put(CalendarContract.Events.DESCRIPTION, "Client: " + newDelivery.getDeliveryClientID());
                values.put(CalendarContract.Events.CALENDAR_ID, 1);
                values.put(CalendarContract.Events.ALL_DAY, true);
                values.put(CalendarContract.Events.HAS_ALARM, true);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

                //Adds the Delivery to the user's Calendar
                contentResolver.insert(CalendarContract.Events.CONTENT_URI, values);
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Updates the details of a Calendar event when a Delivery is updated (learnt from https://stackoverflow.com/questions/13232717/how-to-get-all-the-events-from-calendar and https://stackoverflow.com/questions/22942473/how-to-update-and-remove-calendar-event-in-android)
    public void updateDeliveryInCalendar() throws SecurityException{
        try{
            //Checks for permission to write to calendar, and updates the Calendar event if permission has been granted
            if(ContextCompat.checkSelfPermission(DeliveryActivity.this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED){
                //Gets ContentResolver and fetches the available Calendars from the phone
                ContentResolver contentResolver = getContentResolver();

                //Fetches all events from the default Calendar
                Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/events"), new String[] { "_id", "title"}, CalendarContract.Instances.CALENDAR_ID + " = ?", new String[] {"1"}, null);

                //Loops through the Calendar events until the event that is to be updated is found
                while (cursor.moveToNext()){
                    final long id = cursor.getLong(0);
                    final String title = cursor.getString(1);

                    //Compares Calendar event title to the title of the event that needs to be updated, and updates the event if they match
                    if(title.equals("Delivery " + newDelivery.getDeliveryID())){
                        //Sets the required values for the ContentValues attributes
                        ContentValues values = new ContentValues();
                        Calendar deliveryDate = Calendar.getInstance();
                        String dateOfDelivery = newDelivery.getDeliveryDate();
                        String[] parsedDeliveryDate = dateOfDelivery.split("/");
                        deliveryDate.set(Integer.parseInt(parsedDeliveryDate[2]), Integer.parseInt(parsedDeliveryDate[1]) - 1, Integer.parseInt(parsedDeliveryDate[0]), 8, 0);
                        long dateMilliseconds = deliveryDate.getTimeInMillis();
                        values.put(CalendarContract.Events.DTSTART, dateMilliseconds);
                        values.put(CalendarContract.Events.DTEND, dateMilliseconds);

                        //Updates the Calendar event
                        Uri eventsUri = Uri.parse("content://com.android.calendar/events");
                        Uri eventUri = ContentUris.withAppendedId(eventsUri, id);
                        getContentResolver().update(eventUri, values, null, null);
                    }
                }

                //Closes Cursor
                cursor.close();
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method sends an SMS to the Client when a Delivery is scheduled/updated
    public void sendSMS(Delivery delivery){
        try{
            //Checks for permission to send an SMS, and sends the SMS if permission is granted
            if(ContextCompat.checkSelfPermission(DeliveryActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                String clientID = delivery.getDeliveryClientID();
                String clientPhoneNumber = "";

                //Fetches the Client's phone number
                for(Client client : lstClients){
                    if(client.getClientID().equals(clientID)){
                        clientPhoneNumber = client.getClientPhoneNumber();
                        break;
                    }
                }

                //Sets the content of the SMS
                String messageContent = "";
                if(action.equals("add")){
                    messageContent = "Hello, please note that your Delivery (ID: " + delivery.getDeliveryID() + ") has been scheduled to be delivered on " + delivery.getDeliveryDate() + ".";
                }
                else{
                    messageContent = "Hello, please note that your Delivery (ID: " + delivery.getDeliveryID() + ") has been rescheduled to be delivered on " + delivery.getDeliveryDate() + ".";
                }

                //Sends the SMS
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(clientPhoneNumber, null, messageContent, null, null);
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method toggles the ProgressBar's visibility
    public void toggleProgressBarVisibility(int visibility){
        try{
            //Toggles ProgressBar visibility
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar) ;
            progressBar.setVisibility(visibility);
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
            //Processes the result when the Stock is fetched from the Firebase Database
            if(resultCode == FirebaseService.ACTION_FETCH_STOCK_RESULT_CODE){
                lstStock = (ArrayList<Stock>) resultData.getSerializable(FirebaseService.ACTION_FETCH_STOCK);

                //Displays error message if there are no Stock items to display
                if(lstStock.size() > 0){
                    //Displays the Stock or fetches the Deliveries (based on the value of the firebaseAction variable)
                    if(firebaseAction.equals("stock")){
                        displaySpinnerDeliveryItems(lstStock);
                    }
                    else{
                        new Delivery().requestDeliveries(null, getApplicationContext(), new DataReceiver(new Handler()), 0);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "There are currently no Stock items added", Toast.LENGTH_LONG).show();
                }
            }
            //Processes the result when the Client data is fetched from the Firebase Database
            else if(resultCode == FirebaseService.ACTION_FETCH_CLIENTS_RESULT_CODE){
                lstClients = (ArrayList<Client>) resultData.getSerializable(FirebaseService.ACTION_FETCH_CLIENTS);

                //Displays the Client data, or displays an error message if there are no Clients to display
                if(lstClients.size() > 0){
                    displaySpinnerClients();
                }
                else{
                    Toast.makeText(getApplicationContext(), "There are currently no Clients added", Toast.LENGTH_LONG).show();
                }
            }
            //Processes the result when the Delivery has been written to the Firebase Database
            else if(resultCode == FirebaseService.ACTION_WRITE_DELIVERY_RESULT_CODE){
                boolean success = resultData.getBoolean(FirebaseService.ACTION_WRITE_DELIVERY);

                //Performs the appropriate action based on whether the Delivery was written to the Firebase Database successfully
                if(success){
                    Intent intent;
                    if(action.equals("add")){
                        addDeliveryToCalendar();
                        Toast.makeText(getApplicationContext(), "Delivery successfully added", Toast.LENGTH_LONG).show();

                        //Refreshes the current Activity to allow the user to add more Deliveries if need be
                        intent = getIntent();
                        finish();
                    }
                    else{
                        updateDeliveryInCalendar();
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
                toggleProgressBarVisibility(View.INVISIBLE);
            }
            //Processes the result once the Deliveries have been fetched from the Firebase Database
            else if(resultCode == FirebaseService.ACTION_FETCH_DELIVERIES_RESULT_CODE){
                //Attempts to write the Delivery to the Firebase Database
                ArrayList<Delivery> lstDeliveries = (ArrayList<Delivery>) resultData.getSerializable(FirebaseService.ACTION_FETCH_DELIVERIES);
                saveDeliveryDetails(lstStock, lstDeliveries);

                //Hides ProgressBar
                toggleProgressBarVisibility(View.INVISIBLE);
            }
        }
    }
}