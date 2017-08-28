/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class displays the screenshot of a Run that was saved by the user
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RouteViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_route_viewer);

            //Displays Back button in ActionBar
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null){
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle("Route Taken");
            }

            //Fetches the screenshot of the Map from Firebase and displays it
            displayImage();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Takes the user back to the RouteHistoryActivity when the back button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try{
            int id = item.getItemId();

            //Takes the user back to the RouteHistoryActivity if the button that was pressed was the back button
            if (id == android.R.id.home) {
                Intent intent = new Intent(RouteViewerActivity.this, RouteHistoryActivity.class);
                startActivity(intent);
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    //Method fetches the screenshot of the Run (from the Firebase Database) that was passed into this Activity, and displays it
    public void displayImage(){
        try{
            //Displays ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);

            //Fetches the data that was passed into this Activity and gets a reference to Firebase Storage
            Bundle bundle = getIntent().getExtras();
            String imageURL = bundle.getString("imageURL");
            final ImageView imageView = (ImageView) findViewById(R.id.image_run_route);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();

            //Fetches the image associated with the run from Firebase Storage
            StorageReference imageReference = storageReference.child(imageURL);
            imageReference.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageView.setImageBitmap(bitmap);

                    //Hides ProgressBar
                    toggleProgressBarVisibility(View.INVISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
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
}