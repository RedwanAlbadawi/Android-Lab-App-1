package com.example.vijaya.firebaseloginsignup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.vijaya.firebaseloginsignup.Model.User;
import com.example.vijaya.firebaseloginsignup.Utilities.DialogManager;
import com.example.vijaya.firebaseloginsignup.Utilities.InternetConnectivityManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

public class SplashActivity extends AppCompatActivity {


    // firebase classes.
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // set support action title bar name
        getSupportActionBar().setTitle("Firebase Signup & Login");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");
    }

    private void updateUI(){

        // check for internet connectivity.. if internet is available
        if(InternetConnectivityManager.isNetworkConnected(getApplicationContext())){

            // check whether the user is already signed in or not..
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if(firebaseUser!=null){
                // if user is already signed in get the current user id.
                String userId =  firebaseUser.getUid();

                // get the user record stored in  firebase database under userid
                databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // create user class and put the information in it.
                                User user = dataSnapshot.getValue(User.class);
                                Intent intent = new Intent(getApplicationContext(),WelcomeActivity.class);
                                // pass user information in intent
                                intent.putExtra("user",(Serializable) user);
                                startActivity(intent);
                                finish();
                            }
                        },1000);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SplashActivity.this, "Exception: "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },3000);
            }
        }
        else{
            // if there is no internet connection show user a dialog
            DialogManager.showAlertDialog(SplashActivity.this,getResources().getString(R.string.connectionProblemTitle),
                   getResources().getString(R.string.connectionProblemMessage) ,
                    R.drawable.no_internet,MainActivity.class);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }

}