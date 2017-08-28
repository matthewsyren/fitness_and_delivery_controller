/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class allows the user to create an account
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccountActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_create_account);

            //Hides ProgressBar
            toggleProgressBarVisibility(View.INVISIBLE);

            //Gets an instance of FirebaseAuth, which is used to create the account
            firebaseAuth = FirebaseAuth.getInstance();
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

    //Method creates an account for the user
    public void createAccountOnClick(View view){
        try{
            //View assignments
            EditText txtEmail = (EditText) findViewById(R.id.text_create_account_email);
            EditText txtPassword = (EditText) findViewById(R.id.text_create_account_password);
            EditText txtConfirmPassword = (EditText) findViewById(R.id.text_create_account_confirm_password);

            //Fetches the contents of the Views
            String email = txtEmail.getText().toString();
            String password = txtPassword.getText().toString();
            String confirmPassword = txtConfirmPassword.getText().toString();

            //Displays ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);

            //Creates an account if the user's passwords match and they have entered valid data
            if(password.equals(confirmPassword)){
                final User user = new User(email, password);
                firebaseAuth.createUserWithEmailAndPassword(user.getUserEmailAddress(), user.getUserPassword()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //Registers the user in the Firebase authentication for this app
                            pushUser(user.getUserEmailAddress());
                        }
                        else{
                            Toast.makeText(CreateAccountActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            toggleProgressBarVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
            else{
                Toast.makeText(getApplicationContext(), "Please ensure that your passwords match", Toast.LENGTH_LONG).show();
                toggleProgressBarVisibility(View.INVISIBLE);
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method generates a unique key for the created user, and writes the key and its value (the user's email) to the 'Users' child node in the Firebase database
    public void pushUser(String emailAddress){
        try{
            //Establishes a connection to the Firebase Database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference().child("Users");

            //Generates the user's unique key and saves the value (the user's email address) to the Firebase database
            String key =  databaseReference.push().getKey();
            databaseReference.child(key).setValue(emailAddress);

            //Saves the user's email and key to the device's SharedPreferences, and displays a message to the user
            SharedPreferences preferences = getSharedPreferences("", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("userEmail", emailAddress);
            editor.putString("userKey", key);
            editor.apply();
            Toast.makeText(getApplicationContext(), "Account successfully created!", Toast.LENGTH_LONG).show();

            //Takes the user to the next activity
            Intent intent = new Intent(CreateAccountActivity.this, StartActivity.class);
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}