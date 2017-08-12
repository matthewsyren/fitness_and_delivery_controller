package a15008377.opsc7312assign1_15008377;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
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
            getRuns(new User(this).getUserKey());
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method fetches all the runs associated with the user's key, and adds them to an ArrayList. The data is then displayed in a ListView
    public void getRuns(final String userKey){
        //Gets reference to Firebase
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey);

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

                //Sets the Adapter for the ListView
                RunListViewAdapter runListViewAdapter = new RunListViewAdapter(RouteHistoryActivity.this, lstRuns);
                ListView listView = (ListView) findViewById(R.id.list_view_runs);
                listView.setAdapter(runListViewAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Data", "Failed to read data, please check your internet connection");
            }
        });
    }
}