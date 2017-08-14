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
    public static final String ACTION_FETCH_STOCK =  "a15008377.opsc7312assign1_15008377.action.FETCH_STOCK";
    public static final String ACTION_FETCH_CLIENTS =  "a15008377.opsc7312assign1_15008377.action.FETCH_CLIENTS";
    public static final String ACTION_FETCH_DELIVERIES =  "a15008377.opsc7312assign1_15008377.action.FETCH_DELIVERIES";
    public static final int ACTION_FETCH_STOCK_RESULT_CODE = 1;
    private ResultReceiver resultReceiver;

    public FirebaseService() {
        super("FirebaseService");
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

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            resultReceiver = intent.getParcelableExtra(RECEIVER);

            final String action = intent.getAction();
            if (ACTION_FETCH_STOCK.equals(action)) {
                String userKey = intent.getStringExtra(FIREBASE_KEY);
                startActionFetchStock(userKey);
            }
        }
    }

    private void returnFetchStockResult(ArrayList<Stock> lstStock){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ACTION_FETCH_STOCK, lstStock);
        resultReceiver.send(ACTION_FETCH_STOCK_RESULT_CODE, bundle);
    }
}