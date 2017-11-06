/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
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
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.Locale;

public class StockActivity extends AppCompatActivity {
    //Declarations
    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_stock);

            //Displays the Views for this Activity
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
            //View assignments
            EditText txtStockID = (EditText) findViewById(R.id.text_stock_id);
            EditText txtStockDescription = (EditText) findViewById(R.id.text_stock_description);
            EditText txtStockQuantity = (EditText) findViewById(R.id.text_stock_quantity);

            //Variable assignments
            String stockID = txtStockID.getText().toString();
            String stockDescription = txtStockDescription.getText().toString();
            int stockQuantity = Integer.parseInt(txtStockQuantity.getText().toString());

            //Ensures that Stock object is valid
            final Stock stock = new Stock(stockID, stockDescription, stockQuantity);
            if(stock.validateStock(this)){
                //Displays ProgressBar
                toggleProgressBarVisibility(View.VISIBLE);

                //Sends the Stock item to be written to the Firebase Database
                stock.requestWriteOfStockItem(this, action, new DataReceiver(new Handler()));
            }
        }
        catch(NumberFormatException nfe){
            Toast.makeText(getApplicationContext(), "Please enter a whole number for the Stock Quantity", Toast.LENGTH_LONG).show();
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
            //Processes the result when the Stock has been written to the Firebase Database
            if(resultCode == FirebaseService.ACTION_WRITE_STOCK_RESULT_CODE){
                boolean success = resultData.getBoolean(FirebaseService.ACTION_WRITE_STOCK);

                //Performs the appropriate action based on whether the data was written to the Firebase Database successfully
                if(success){
                    Intent intent;
                    if(action.equals("add")){
                        Toast.makeText(getApplicationContext(), "Stock successfully added", Toast.LENGTH_LONG).show();

                        //Refreshes the Activity to allow the user to add more Stock if need be
                        intent = getIntent();
                        finish();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Stock successfully updated", Toast.LENGTH_LONG).show();

                        //Takes the user back to the StockControlActivity once the update is complete
                        intent = new Intent(StockActivity.this, StockControlActivity.class);
                    }
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "The Stock ID has already been used, please choose another Stock ID", Toast.LENGTH_LONG).show();
                }

                //Hides ProgressBar
                toggleProgressBarVisibility(View.INVISIBLE);
            }
        }
    }
}