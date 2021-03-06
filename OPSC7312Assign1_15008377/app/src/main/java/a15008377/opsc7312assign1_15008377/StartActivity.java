/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class allows the user to navigate through the app
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_start);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method prevents the user from going back to the previous Activity by clicking the back button
    @Override
    public void onBackPressed() {
    }

    //Opens the Question 1A section of the app
    public void questionOneAOnClick(View view){
        try{
            Intent intent = new Intent(StartActivity.this, Question1A.class);
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Opens the Question 1B section of the app
    public void questionOneBOnClick(View view){
        try{
            Intent intent = new Intent(StartActivity.this, Question1B.class);
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Opens the Question1HelpActivity
    public void questionOneHelpOnClick(View view){
        try{
            Intent intent = new Intent(StartActivity.this, Question1HelpActivity.class);
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Opens the Question 2 section of the app
    public void questionTwoOnClick(View view){
        try{
            Intent intent = new Intent(StartActivity.this, Question2.class);
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Opens the Question2HelpActivity
    public void questionTwoHelpOnClick(View view){
        try{
            Intent intent = new Intent(StartActivity.this, Question2HelpActivity.class);
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Signs the user out of the application and opens the LoginActivity
    public void signOutOnClick(View view){
        try{
            //Signs the user out of the app and Firebase
            User user = new User(this);
            user.setActiveUsername(null, this);
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
