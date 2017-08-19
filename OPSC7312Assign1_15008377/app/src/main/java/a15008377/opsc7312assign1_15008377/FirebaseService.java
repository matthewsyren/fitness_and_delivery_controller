package a15008377.opsc7312assign1_15008377;

import android.app.IntentService;
import android.content.Intent;
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

    //Action Declarations
    public static final String ACTION_FETCH_STOCK =  "a15008377.opsc7312assign1_15008377.action.FETCH_STOCK";
    public static final String ACTION_WRITE_STOCK =  "a15008377.opsc7312assign1_15008377.action.WRITE_STOCK";
    public static final String ACTION_WRITE_STOCK_INFORMATION =  "a15008377.opsc7312assign1_15008377.action.WRITE_STOCK_INFORMATION";
    public static final String ACTION_FETCH_CLIENTS =  "a15008377.opsc7312assign1_15008377.action.FETCH_CLIENTS";
    public static final String ACTION_WRITE_CLIENT =  "a15008377.opsc7312assign1_15008377.action.WRITE_CLIENT";
    public static final String ACTION_WRITE_CLIENT_INFORMATION =  "a15008377.opsc7312assign1_15008377.action.WRITE_CLIENT_INFORMATION";
    public static final String ACTION_FETCH_DELIVERIES =  "a15008377.opsc7312assign1_15008377.action.FETCH_DELIVERIES";
    public static final String ACTION_WRITE_DELIVERY =  "a15008377.opsc7312assign1_15008377.action.WRITE_DELIVERY";
    public static final String ACTION_WRITE_DELIVERY_INFORMATION =  "a15008377.opsc7312assign1_15008377.action.WRITE_DELIVERY_INFORMATION";
    public static final String ACTION_FETCH_RUNS =  "a15008377.opsc7312assign1_15008377.action.FETCH_RUNS";

    //Result Codes
    public static final int ACTION_FETCH_STOCK_RESULT_CODE = 1;
    public static final int ACTION_WRITE_STOCK_RESULT_CODE = 4;
    public static final int ACTION_FETCH_CLIENTS_RESULT_CODE = 2;
    public static final int ACTION_WRITE_CLIENT_RESULT_CODE = 5;
    public static final int ACTION_FETCH_DELIVERIES_RESULT_CODE = 3;
    public static final int ACTION_WRITE_DELIVERY_RESULT_CODE = 6;
    public static final int ACTION_FETCH_RUNS_RESULT_CODE = 7;
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
            else if(action.equals(ACTION_WRITE_STOCK)){
                Stock stock = (Stock) intent.getSerializableExtra(ACTION_WRITE_STOCK);
                String writeInformation = intent.getStringExtra(ACTION_WRITE_STOCK_INFORMATION);
                startActionWriteStock(userKey, stock, writeInformation);
            }
            else if (action.equals(ACTION_FETCH_CLIENTS)) {
                startActionFetchClients(userKey, searchTerm);
            }
            else if(action.equals(ACTION_WRITE_CLIENT)) {
                Client client = (Client) intent.getSerializableExtra(ACTION_WRITE_CLIENT);
                String writeInformation = intent.getStringExtra(ACTION_WRITE_CLIENT_INFORMATION);
                startActionWriteClient(userKey, client, writeInformation);
            }
            else if (action.equals(ACTION_FETCH_DELIVERIES)) {
                int deliveryComplete = intent.getIntExtra(DELIVERY_COMPLETE, 0);
                startActionFetchDeliveries(userKey, deliveryComplete, searchTerm);
            }
            else if(action.equals(ACTION_WRITE_DELIVERY)) {
                Delivery delivery = (Delivery) intent.getSerializableExtra(ACTION_WRITE_DELIVERY);
                String writeInformation = intent.getStringExtra(ACTION_WRITE_DELIVERY_INFORMATION);
                startActionWriteDelivery(userKey, delivery, writeInformation);
            }
            else if(action.equals(ACTION_FETCH_RUNS)){
                startActionFetchRuns(userKey);
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
    private void startActionWriteStock(String userKey, final Stock stock, final String writeInformation){
        //Gets reference to Firebase
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey).child("stock");

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean valid = true;


                if(writeInformation.equals("add")){
                    if(dataSnapshot.child(stock.getStockID()).exists()){
                        valid = false;
                    }
                }

                if(writeInformation.equals("delete")){
                    databaseReference.child(stock.getStockID()).setValue(null);
                }
                else if(valid){
                    databaseReference.child(stock.getStockID()).setValue(stock);
                }
                databaseReference.removeEventListener(this);
                returnWriteStockResult(valid);
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
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey).child("clients");

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
    private void startActionWriteClient(String userKey, final Client client, final String writeInformation){
        //Gets reference to Firebase
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey).child("clients");

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean valid = true;

                if(writeInformation.equals("add")){
                    if(dataSnapshot.child(client.getClientID()).exists()){
                        valid = false;
                    }
                }
                if(writeInformation.equals("delete")){
                    databaseReference.child(client.getClientID()).setValue(null);
                }
                else if(valid){
                    databaseReference.child(client.getClientID()).setValue(client);
                }
                databaseReference.removeEventListener(this);
                returnWriteClientResult(valid);
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
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey).child("deliveries");

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
    private void startActionWriteDelivery(String userKey, final Delivery delivery, final String writeInformation){
        //Gets reference to Firebase
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey).child("deliveries");

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean valid = true;

                if(writeInformation.equals("add")){
                    if(dataSnapshot.child(delivery.getDeliveryID()).exists()){
                        valid = false;
                    }
                }
                if(writeInformation.equals("delete")){
                    databaseReference.child(delivery.getDeliveryID()).setValue(null);
                }
                else if(valid){
                    databaseReference.child(delivery.getDeliveryID()).setValue(delivery);
                }
                databaseReference.removeEventListener(this);
                returnWriteDeliveryResult(valid);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Data", "Failed to read data, please check your internet connection");
            }
        });
    }

    private void startActionFetchRuns(String userKey){
        //Gets reference to Firebase
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey).child("runs");

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Loops through all runs and adds them to the lstRuns ArrayList
                Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();
                ArrayList<Run> lstRuns = new ArrayList<>();
                for(DataSnapshot snapshot : lstSnapshots){
                    //Retrieves the run from Firebase, sets the imageURL for the run and adds the Run to the lstRuns ArrayList
                    Run run = snapshot.getValue(Run.class);
                    run.setImageUrl(snapshot.getKey() + ".jpg");
                    lstRuns.add(run);
                }

                returnFetchRunsResult(lstRuns);
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

    private void returnWriteStockResult(boolean success){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_WRITE_STOCK, success);
        resultReceiver.send(ACTION_WRITE_STOCK_RESULT_CODE, bundle);
    }

    private void returnFetchClientsResult(ArrayList<Client> lstClients){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_FETCH_CLIENTS, lstClients);
        resultReceiver.send(ACTION_FETCH_CLIENTS_RESULT_CODE, bundle);
    }

    private void returnWriteClientResult(boolean success){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_WRITE_CLIENT, success);
        resultReceiver.send(ACTION_WRITE_CLIENT_RESULT_CODE, bundle);
    }

    private void returnFetchDeliveriesResult(ArrayList<Delivery> lstDeliveries){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_FETCH_DELIVERIES, lstDeliveries);
        resultReceiver.send(ACTION_FETCH_DELIVERIES_RESULT_CODE, bundle);
    }

    private void returnWriteDeliveryResult(boolean success){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_WRITE_DELIVERY, success);
        resultReceiver.send(ACTION_WRITE_DELIVERY_RESULT_CODE, bundle);
    }

    private void returnFetchRunsResult(ArrayList<Run> lstRuns){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_FETCH_RUNS, lstRuns);
        resultReceiver.send(ACTION_FETCH_RUNS_RESULT_CODE, bundle);
    }
}