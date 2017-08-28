/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class displays a report of all incomplete Deliveries
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.ArrayList;

public class DeliveryControlActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_delivery_control);

            //Sets the NavigationDrawer for the Activity and sets the selected item in the NavigationDrawer to Home
            super.onCreateDrawer();
            super.setSelectedNavItem(R.id.nav_delivery_control);

            //Sets the TextChangedListener for the text_search_delivery, which will perform a search when a key is pressed
            final EditText txtSearchDelivery = (EditText) findViewById(R.id.text_search_delivery);
            txtSearchDelivery.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchDeliveries(txtSearchDelivery);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            //Displays ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);

            //Fetches Deliveries from the Firebase Database
            new Delivery().requestDeliveries(null, this, new DataReceiver(new Handler()), 0);
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

            //Displays ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);

            //Fetches incomplete Deliveries from the Firebase Database
            new Delivery().requestDeliveries(null, this, new DataReceiver(new Handler()), 0);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method fetches the Deliveries that match the search result and send them to the displayDeliveries method
    public void searchDeliveries(EditText txtSearchDelivery){
        try{
            //Fetches the search term and requests Deliveries that match the search term
            String searchTerm = txtSearchDelivery.getText().toString();

            //Displays ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);

            //Fetches incomplete Deliveries from the Firebase Database
            new Delivery().requestDeliveries(searchTerm, this, new DataReceiver(new Handler()), 0);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    //Method populates the ListView on this Activity with the incomplete Deliveries
    public void displayDeliveries(final ArrayList<Delivery> lstDeliveries){
        try{
            //Sets the Adapter for the list_view_deliveries ListView
            DeliveryReportListViewAdapter adapter = new DeliveryReportListViewAdapter(this, lstDeliveries);
            final ListView listView = (ListView) findViewById(R.id.list_view_deliveries);
            listView.setAdapter(adapter);

            //Sets OnItemClickListener, which will pass the information of the Delivery clicked to the DeliveryActivity
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(DeliveryControlActivity.this, DeliveryActivity.class);
                    intent.putExtra("action", "update");
                    intent.putExtra("deliveryObject", lstDeliveries.get(position));
                    startActivity(intent);}
            });
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method takes the user to the DeliveryActivity
    public void addDeliveryOnClick(View view){
        try{
            Intent intent = new Intent(DeliveryControlActivity.this, DeliveryActivity.class);
            intent.putExtra("action", "add");
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    //Method toggles the ProgressBar's visibility and disables touches when the ProgressBar is visible
    public void toggleProgressBarVisibility(int visibility){
        try{
            //Toggles ProgressBar visibility
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar) ;
            progressBar.setVisibility(visibility);

            //Enables touches on the screen if the ProgressBar is hidden, and disables touches on the screen when the ProgressBar is visible
            if(visibility == View.VISIBLE){
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            else{
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
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
            //Processes the result when the Deliveries have been fetched from the Firebase Database
            if(resultCode == FirebaseService.ACTION_FETCH_DELIVERIES_RESULT_CODE){
                ArrayList<Delivery> lstDeliveries = (ArrayList<Delivery>) resultData.getSerializable(FirebaseService.ACTION_FETCH_DELIVERIES);

                //Displays an error message if there are no Delivery items to display
                if(lstDeliveries.size() == 0){
                    Toast.makeText(getApplicationContext(), "There are currently no Deliveries added", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Deliveries fetched", Toast.LENGTH_LONG).show();
                }

                //Displays Deliveries
                displayDeliveries(lstDeliveries);

                //Hides ProgressBar
                toggleProgressBarVisibility(View.INVISIBLE);
            }
        }
    }
}