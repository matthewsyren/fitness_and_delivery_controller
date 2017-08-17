package a15008377.opsc7312assign1_15008377;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FirebaseService extends IntentService {
    //Declarations
    public static final String RECEIVER = "a15008377.opsc7312assign1_15008377.action.RECEIVER";
    public static final String FIREBASE_KEY = "a15008377.opsc7312assign1_15008377.action.USER_FIREBASE_KEY";
    public static final String DELIVERY_COMPLETE = "a15008377.opsc7312assign1_15008377.action.DELIVERY_COMPLETE";
    public static final String SEARCH_TERM = "a15008377.opsc7312assign1_15008377.action.SEARCH_TERM";

    public static final String ACTION_FETCH_STOCK =  "a15008377.opsc7312assign1_15008377.action.FETCH_STOCK";
    public static final String ACTION_UPDATE_STOCK =  "a15008377.opsc7312assign1_15008377.action.UPDATE_STOCK";
    public static final String ACTION_FETCH_CLIENTS =  "a15008377.opsc7312assign1_15008377.action.FETCH_CLIENTS";
    public static final String ACTION_UPDATE_CLIENT =  "a15008377.opsc7312assign1_15008377.action.UPDATE_CLIENT";
    public static final String ACTION_FETCH_DELIVERIES =  "a15008377.opsc7312assign1_15008377.action.FETCH_DELIVERIES";
    public static final String ACTION_UPDATE_DELIVERY =  "a15008377.opsc7312assign1_15008377.action.UPDATE_DELIVERY";
    public static final int ACTION_FETCH_STOCK_RESULT_CODE = 1;
    public static final int ACTION_UPDATE_STOCK_RESULT_CODE = 4;
    public static final int ACTION_FETCH_CLIENTS_RESULT_CODE = 2;
    public static final int ACTION_UPDATE_CLIENT_RESULT_CODE = 5;
    public static final int ACTION_FETCH_DELIVERIES_RESULT_CODE = 3;
    public static final int ACTION_UPDATE_DELIVERY_RESULT_CODE = 6;
    private ResultReceiver resultReceiver;

    public FirebaseService() {
        super("FirebaseService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            resultReceiver = intent.getParcelableExtra(RECEIVER);

            final String action = intent.getAction();
            String userKey = intent.getStringExtra(FIREBASE_KEY);
            String searchTerm = intent.getStringExtra(SEARCH_TERM);

            if (action.equals(ACTION_FETCH_STOCK)) {
                startActionFetchStock(userKey, searchTerm);
            }
            else if(action.equals(ACTION_UPDATE_STOCK)){
                Stock stock = (Stock) intent.getSerializableExtra(ACTION_UPDATE_STOCK);
                startActionUpdateStock(userKey, stock);
            }
            else if (action.equals(ACTION_FETCH_CLIENTS)) {
                startActionFetchClients(userKey, searchTerm);
            }
            else if(action.equals(ACTION_UPDATE_CLIENT)) {
                Client client = (Client) intent.getSerializableExtra(ACTION_UPDATE_CLIENT);
                startActionUpdateClient(userKey, client);
            }
            else if (action.equals(ACTION_FETCH_DELIVERIES)) {
                int deliveryComplete = intent.getIntExtra(DELIVERY_COMPLETE, 0);
                startActionFetchDeliveries(userKey, deliveryComplete, searchTerm);
            }
            else if(action.equals(ACTION_UPDATE_DELIVERY)) {
                Delivery delivery = (Delivery) intent.getSerializableExtra(ACTION_UPDATE_DELIVERY);
                startActionUpdateDelivery(userKey, delivery);
            }
        }
    }

    private void startActionFetchStock(String userKey, final String searchTerm){
        //Gets reference to Firebase
        final ArrayList<Stock> lstStock = new ArrayList<>();
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey).child("stock");

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Loops through all Stock and adds them to the lstStock ArrayList
                Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : lstSnapshots){
                    //Retrieves the Stock from Firebase and adds the Stock to an ArrayList of Stock objects
                    Stock stock = snapshot.getValue(Stock.class);
                    if(searchTerm == null || stock.getStockID().contains(searchTerm)) {
                        lstStock.add(stock);
                    }
                }
                databaseReference.removeEventListener(this);

                returnFetchStockResult(lstStock);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Data", "Failed to read data, please check your internet connection");
            }
        });
    }

    //Method writes a Stock object to the Firebase database
    private void startActionUpdateStock(String userKey, final Stock stock){
        //Gets reference to Firebase
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey).child("stock");

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                databaseReference.child(stock.getStockID()).setValue(stock);
                databaseReference.removeEventListener(this);

                returnUpdateStockResult(1);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Data", "Failed to read data, please check your internet connection");
            }
        });
    }

    //Method fetches the Client data from Firebasee
    private void startActionFetchClients(String userKey, final String searchTerm){
        //Gets reference to Firebase
        final ArrayList<Client> lstClients = new ArrayList<>();
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
                    if(searchTerm == null || client.getClientID().contains(searchTerm)) {
                        lstClients.add(client);
                    }
                }
                databaseReference.removeEventListener(this);

                returnFetchClientsResult(lstClients);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Data", "Failed to read data, please check your internet connection");
            }
        });
    }

    //Method writes a Stock object to the Firebase database
    private void startActionUpdateClient(String userKey, final Client client){
        //Gets reference to Firebase
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey).child("clients");

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                databaseReference.child(client.getClientID()).setValue(client);
                databaseReference.removeEventListener(this);

                returnUpdateClientResult(1);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Data", "Failed to read data, please check your internet connection");
            }
        });
    }

    //Method fetches the Client data from Firebasee
    private void startActionFetchDeliveries(String userKey, final int deliveryComplete, final String searchTerm){
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
                    if(delivery.getDeliveryComplete() == deliveryComplete){
                        if(searchTerm == null || delivery.getDeliveryID().contains(searchTerm)) {
                            lstDeliveries.add(delivery);
                        }
                    }
                }
                databaseReference.removeEventListener(this);

                returnFetchDeliveriesResult(lstDeliveries);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Data", "Failed to read data, please check your internet connection");
            }
        });
    }

    //Method writes a Stock object to the Firebase database
    private void startActionUpdateDelivery(String userKey, final Delivery delivery){
        //Gets reference to Firebase
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey).child("deliveries");

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                databaseReference.child(delivery.getDeliveryID()).setValue(delivery);
                databaseReference.removeEventListener(this);

                returnUpdateDeliveryResult(1);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Data", "Failed to read data, please check your internet connection");
            }
        });
    }

    private void returnFetchStockResult(ArrayList<Stock> lstStock){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_FETCH_STOCK, lstStock);
        resultReceiver.send(ACTION_FETCH_STOCK_RESULT_CODE, bundle);
    }

    private void returnUpdateStockResult(int success){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_UPDATE_STOCK, success);
        resultReceiver.send(ACTION_UPDATE_STOCK_RESULT_CODE, bundle);
    }

    private void returnFetchClientsResult(ArrayList<Client> lstClients){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_FETCH_CLIENTS, lstClients);
        resultReceiver.send(ACTION_FETCH_CLIENTS_RESULT_CODE, bundle);
    }

    private void returnUpdateClientResult(int success){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_UPDATE_CLIENT, success);
        resultReceiver.send(ACTION_UPDATE_CLIENT_RESULT_CODE, bundle);
    }

    private void returnFetchDeliveriesResult(ArrayList<Delivery> lstDeliveries){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_FETCH_DELIVERIES, lstDeliveries);
        resultReceiver.send(ACTION_FETCH_DELIVERIES_RESULT_CODE, bundle);
    }

    private void returnUpdateDeliveryResult(int success){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_UPDATE_DELIVERY, success);
        resultReceiver.send(ACTION_UPDATE_DELIVERY_RESULT_CODE, bundle);
    }
}