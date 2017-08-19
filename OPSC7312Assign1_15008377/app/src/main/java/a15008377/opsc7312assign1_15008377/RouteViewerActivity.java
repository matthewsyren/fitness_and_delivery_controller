package a15008377.opsc7312assign1_15008377;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_viewer);
        displayImage();
    }

    public void displayImage(){
        try{
            //Displays ProgressBar
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar) ;
            progressBar.setVisibility(View.VISIBLE);

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
                    progressBar.setVisibility(View.INVISIBLE);
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
}