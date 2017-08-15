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

    public static final String ACTION_FETCH_STOCK =  "a15008377.opsc7312assign1_15008377.action.FETCH_STOCK";
    public static final String ACTION_FETCH_CLIENTS =  "a15008377.opsc7312assign1_15008377.action.FETCH_CLIENTS";
    public static final String ACTION_FETCH_DELIVERIES =  "a15008377.opsc7312assign1_15008377.action.FETCH_DELIVERIES";
    public static final int ACTION_FETCH_STOCK_RESULT_CODE = 1;
    public static final int ACTION_FETCH_CLIENTS_RESULT_CODE = 2;
    public static final int ACTION_FETCH_DELIVERIES_RESULT_CODE = 3;
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

            if (action.equals(ACTION_FETCH_STOCK)) {
                startActionFetchStock(userKey);
            }
            else if (action.equals(ACTION_FETCH_CLIENTS)) {
                startActionFetchClients(userKey);
            }
            else if (action.equals(ACTION_FETCH_DELIVERIES)) {
                int deliveryComplete = intent.getIntExtra(DELIVERY_COMPLETE, 0);
                startActionFetchDeliveries(userKey, deliveryComplete);
            }
        }
    }

    private void startActionFetchStock(String userKey){
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
                    lstStock.add(stock);
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

    //Method fetches the Client data from Firebasee
    private void startActionFetchClients(String userKey){
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
                    lstClients.add(client);
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
    //Method fetches the Client data from Firebasee
    private void startActionFetchDeliveries(String userKey, final int deliveryComplete){
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
                        lstDeliveries.add(delivery);
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


    private void returnFetchStockResult(ArrayList<Stock> lstStock){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_FETCH_STOCK, lstStock);
        resultReceiver.send(ACTION_FETCH_STOCK_RESULT_CODE, bundle);
    }

    private void returnFetchClientsResult(ArrayList<Client> lstClients){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_FETCH_CLIENTS, lstClients);
        resultReceiver.send(ACTION_FETCH_CLIENTS_RESULT_CODE, bundle);
    }

    private void returnFetchDeliveriesResult(ArrayList<Delivery> lstDeliveries){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_FETCH_DELIVERIES, lstDeliveries);
        resultReceiver.send(ACTION_FETCH_DELIVERIES_RESULT_CODE, bundle);
    }
}