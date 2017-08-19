package a15008377.opsc7312assign1_15008377;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class RouteHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_route_history);

            //Fetches run details from Firebase
            requestRuns(new User(this).getUserKey());
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method fetches all the runs associated with the user's key, and adds them to an ArrayList. The data is then displayed in a ListView
    public void requestRuns(final String userKey){
        //Displays ProgressBar
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
        progressBar.setVisibility(View.VISIBLE);

        //Requests location information from the LocationService class
        String firebaseKey = new User(this).getUserKey();
        Intent intent = new Intent(getApplicationContext(), FirebaseService.class);
        intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
        intent.setAction(FirebaseService.ACTION_FETCH_RUNS);
        intent.putExtra(FirebaseService.RECEIVER, new DataReceiver(new Handler()));
        startService(intent);
    }

    //Creates a ResultReceiver to retrieve information from the FirebaseService
    private class DataReceiver extends ResultReceiver {
        private DataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
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
                        Intent intent = new Intent(RouteHistoryActivity.this, RouteViewerActivity.class);
                        intent.putExtra("imageURL", lstRuns.get(position).getImageUrl());
                        startActivity(intent);
                    }
                });

                //Hides ProgressBar
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }
}