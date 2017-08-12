package a15008377.opsc7312assign1_15008377;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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

/**
 * Class populates a ListView with the data that is passed into the constructor
 */

@SuppressWarnings("WeakerAccess")
public class RunListViewAdapter extends ArrayAdapter {
    //Declarations
    private ArrayList<Run> runs = null;
    private Context context;
    private StorageReference storageReference;

    //Constructor
    public RunListViewAdapter(Context context, ArrayList<Run> runs) {
        super(context, R.layout.list_row_runs,runs);
        this.context = context;
        this.runs = runs;
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent)
    {
        //Inflates the list_row view for the ListView
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_row_runs, parent, false);

        //Component assignments
        final ImageView image = (ImageView) convertView.findViewById(R.id.image_run);
        final TextView startTime = (TextView) convertView.findViewById(R.id.text_run_start_date);
        final TextView endTime = (TextView) convertView.findViewById(R.id.text_run_end_date);
        final TextView distanceCovered = (TextView) convertView.findViewById((R.id.text_run_distance_covered));
        final TextView averageSpeed = (TextView) convertView.findViewById((R.id.text_run_average_speed));

        //Displays the data in the appropriate Views
        Resources resources = context.getResources();
        startTime.setText(resources.getString(R.string.text_start_time, runs.get(position).getStartTime()));
        endTime.setText(resources.getString(R.string.text_end_time, runs.get(position).getEndTime()));
        distanceCovered.setText(resources.getString(R.string.text_distance_covered, runs.get(position).getDistanceCovered() + ""));
        averageSpeed.setText(resources.getString(R.string.text_average_speed, runs.get(position).getAverageSpeed() + ""));

        //Fetches the image associated with the run from Firebase Storage
        StorageReference imageReference = storageReference.child(runs.get(position).getImageUrl());
        imageReference.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                image.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        return convertView;
    }
}