/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class displays a report of all Stock items in the Stock.txt text file
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Context;
import android.content.Intent;
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

import java.io.IOException;
import java.util.ArrayList;

public class StockControlActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_stock_control);

            //Sets the NavigationDrawer for the Activity and sets the selected item in the NavigationDrawer to Home
            super.onCreateDrawer();
            super.setSelectedNavItem(R.id.nav_stock_control);

            //Sets the TextChangedListener for the text_search_stock, which will perform a search when the enter key is pressed
            final EditText txtSearchStock = (EditText) findViewById(R.id.text_search_stock);
            txtSearchStock.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchStock(txtSearchStock);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            //Method populates the Stock report
            requestStockItems(null);
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
            requestStockItems(null);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method fetches all Stock items that match the search and sends them to the displayStock method
    public void searchStock(EditText txtSearchStock){
        try{
            //Fetches the search term and requests Stock items that match the search term
            String searchTerm = txtSearchStock.getText().toString();
            requestStockItems(searchTerm);
        }
        catch(Exception exc){
            Toast.makeText(getBaseContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method populates the list_view_available_stock ListView
    public void displayStock(final ArrayList<Stock> lstStock){
        try{
            //Sets the custom adapter for the ListView to display the Stock data
            StockReportListViewAdapter adapter = new StockReportListViewAdapter(this, lstStock);
            ListView listView = (ListView) findViewById(R.id.list_view_available_stock);
            listView.setAdapter(adapter);

            //Sets an OnItemClickListener on the ListView, which will take the user to the StockActivity, where the user will be able to update the Stock's information
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                    Intent intent = new Intent(StockControlActivity.this, StockActivity.class);
                    intent.putExtra("action", "update");
                    intent.putExtra("stockObject", lstStock.get(pos));
                    startActivity(intent);
                }
            });
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method calls the StockActivity
    public void addStockOnClick(View view){
        try{
            Intent intent = new Intent(StockControlActivity.this, StockActivity.class);
            intent.putExtra("action", "add");
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method calls the FirebaseService class and requests the Stock items from the Firebase Database
    public void requestStockItems(String searchTerm){
        try{
            //Displays ProgressBar
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
            progressBar.setVisibility(View.VISIBLE);

            //Requests location information from the LocationService class
            String firebaseKey = new User(this).getUserKey();
            Intent intent = new Intent(getApplicationContext(), FirebaseService.class);
            intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
            intent.setAction(FirebaseService.ACTION_FETCH_STOCK);
            intent.putExtra(FirebaseService.SEARCH_TERM, searchTerm);
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
            if(resultCode == FirebaseService.ACTION_FETCH_STOCK_RESULT_CODE){
                ArrayList<Stock> lstStock = (ArrayList<Stock>) resultData.getSerializable(FirebaseService.ACTION_FETCH_STOCK);

                //Displays error message if there are no Stock items to display
                if(lstStock.size() == 0){
                    Toast.makeText(getApplicationContext(), "There are currently no Stock items added", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Stock fetched", Toast.LENGTH_LONG).show();
                }
                displayStock(lstStock);

                //Hides ProgressBar
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }
}