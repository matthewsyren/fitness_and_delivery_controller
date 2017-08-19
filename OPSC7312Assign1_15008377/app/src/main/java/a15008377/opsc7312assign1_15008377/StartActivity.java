package a15008377.opsc7312assign1_15008377;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
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

    //Opens the Question 2 section of the app
    public void questionTwoOnClick(View view){
        try{
            Intent intent = new Intent(StartActivity.this, HomeActivity.class);
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Signs the user out of the application and opens the LoginActivity
    public void signOutOnClick(View view){
        try{
            Intent intent = new Intent(StartActivity.this, Question1A.class);
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
