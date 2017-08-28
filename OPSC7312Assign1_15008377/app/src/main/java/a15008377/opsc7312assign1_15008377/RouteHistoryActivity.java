/*
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class displays the Runs that the user has saved
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.ArrayList;

public class RouteHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_route_history);

            //Displays ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);

            //Displays Back button in ActionBar
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null){
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle("Run History");
            }

            //Fetches run details from Firebase
            new Run().requestRuns(this, new DataReceiver(new Handler()));
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Takes the user back to the Question1B Activity when the back button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try{
            int id = item.getItemId();

            //Takes the user back to the Question1B Activity if the button that was pressed was the back button
            if (id == android.R.id.home) {
                Intent intent = new Intent(RouteHistoryActivity.this, Question1B.class);
                startActivity(intent);
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
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
            //Processes the result when the Runs are retrieved from the FirebaseDatabase
            if(resultCode == FirebaseService.ACTION_FETCH_RUNS_RESULT_CODE){
                final ArrayList<Run> lstRuns = (ArrayList<Run>) resultData.getSerializable(FirebaseService.ACTION_FETCH_RUNS);
                Toast.makeText(getApplicationContext(), "Runs fetched", Toast.LENGTH_LONG).show();

                //Sets the Adapter for the ListView
                RunListViewAdapter runListViewAdapter = new RunListViewAdapter(RouteHistoryActivity.this, lstRuns);
                ListView listView = (ListView) findViewById(R.id.list_view_runs);
                listView.setAdapter(runListViewAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //Passes the image URL of the Run to the RouteViewerActivity, and opens the RouteViewerActivity
                        Intent intent = new Intent(RouteHistoryActivity.this, RouteViewerActivity.class);
                        intent.putExtra("imageURL", lstRuns.get(position).getImageUrl());
                        startActivity(intent);
                    }
                });

                //Hides ProgressBar
                toggleProgressBarVisibility(View.INVISIBLE);
            }
        }
    }
}