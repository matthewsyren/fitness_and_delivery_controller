/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class allows you to add or update Stock information
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;

public class StockActivity extends AppCompatActivity {
    String action;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_stock);

            displayViews();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Takes the user back to the StockControlActivity when the back button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try{
            int id = item.getItemId();

            //Takes the user back to the StockControlActivity if the button that was pressed was the back button
            if (id == android.R.id.home) {
                Intent intent = new Intent(StockActivity.this, StockControlActivity.class);
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
            Button button = (Button) findViewById(R.id.button_add_stock);

            //Changes Activity based on the user's action
            if(action.equals("update")){
                EditText txtStockID = (EditText) findViewById(R.id.text_stock_id);
                txtStockID.setEnabled(false);
                button.setText(R.string.button_update_stock);

                Stock stock = (Stock) bundle.getSerializable("stockObject");
                displayData(stock);
            }
            else if(action.equals("add")){
                button.setText(R.string.button_add_stock);
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
    //Method pre-populates the TextViews on this Activity with the data from the Stock item that was clicked on in the previous Activity and sent through the bundle
    public void displayData(Stock stock){
        try{
            //View assignments
            EditText txtStockID = (EditText) findViewById(R.id.text_stock_id);
            EditText txtStockDescription = (EditText) findViewById(R.id.text_stock_description);
            EditText txtStockQuantity = (EditText) findViewById(R.id.text_stock_quantity);

            //Displays the Stock item's data in the appropriate Views
            txtStockID.setText(stock.getStockID());
            txtStockDescription.setText(stock.getStockDescription());
            txtStockQuantity.setText(String.format(Locale.ENGLISH, "%d", stock.getStockQuantity()));
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method adds/updates the Stock details to the database
    public void addStockOnClick(View view) {
        try{
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
            progressBar.setVisibility(View.VISIBLE);

            EditText txtStockID = (EditText) findViewById(R.id.text_stock_id);
            EditText txtStockDescription = (EditText) findViewById(R.id.text_stock_description);
            EditText txtStockQuantity = (EditText) findViewById(R.id.text_stock_quantity);

            String stockID = txtStockID.getText().toString();
            String stockDescription = txtStockDescription.getText().toString();
            int stockQuantity = Integer.parseInt(txtStockQuantity.getText().toString());

            final Stock stock = new Stock(stockID, stockDescription, stockQuantity);
            if(stock.validateStock(this)){
                //Gets Firebase Database reference
                final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = firebaseDatabase.getReference().child(new User(this).getUserKey()).child("stock");

                //Adds Listeners for when the data is changed
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean valid = true;
                        //Writes the Stock details to the Firebase Database
                        if(action.equals("add")){
                            if(dataSnapshot.child(stock.getStockID()).exists()){
                                Toast.makeText(getApplicationContext(), "The Stock ID has already been used, please choose another Stock ID", Toast.LENGTH_LONG).show();
                                valid = false;
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                        if(valid){
                            requestUpdateOfStockItem(stock);
                        }
                        databaseReference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.i("Data", "Failed to read data, please check your internet connection");
                    }
                });
            }
        }
        catch(NumberFormatException nfe){
            Toast.makeText(getApplicationContext(), "Please enter a whole number for the Stock Quantity", Toast.LENGTH_LONG).show();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method calls the FirebaseService class and passes in a Stock object that must be written to the Firebase database
    public void requestUpdateOfStockItem(Stock stock){
        try{
            //Displays ProgressBar
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
            progressBar.setVisibility(View.VISIBLE);

            //Requests location information from the LocationService class
            String firebaseKey = new User(this).getUserKey();
            Intent intent = new Intent(getApplicationContext(), FirebaseService.class);
            intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
            intent.setAction(FirebaseService.ACTION_UPDATE_STOCK);
            intent.putExtra(FirebaseService.ACTION_UPDATE_STOCK, stock);
            intent.putExtra(FirebaseService.RECEIVER, new DataReceiver(new Handler()));
            startService(intent);
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

            if(resultCode == FirebaseService.ACTION_UPDATE_STOCK_RESULT_CODE){
                Intent intent = null;

                if(action.equals("add")){
                    Toast.makeText(getApplicationContext(), "Stock successfully added", Toast.LENGTH_LONG).show();
                    intent = getIntent();
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Stock successfully updated", Toast.LENGTH_LONG).show();

                    //Takes the user back to the ClientControlActivity once the update is complete
                    intent = new Intent(StockActivity.this, StockControlActivity.class);
                }

                //Hides ProgressBar
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
                progressBar.setVisibility(View.INVISIBLE);

                startActivity(intent);
            }
        }
    }
}