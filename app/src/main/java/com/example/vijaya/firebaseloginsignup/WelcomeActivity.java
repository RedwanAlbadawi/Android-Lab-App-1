package com.example.vijaya.firebaseloginsignup;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.vijaya.firebaseloginsignup.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
public class WelcomeActivity extends AppCompatActivity{
    CircleImageView profileImage;
    TextView username,university,branch,gender;
    Button signOutButton;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // set app title and get the user information from the intent.
        getSupportActionBar().setTitle("Welcome");
        User user = (User) getIntent().getSerializableExtra("user");

        // initialize the views
        profileImage = findViewById(R.id.imageView);
        username = findViewById(R.id.welcomeMessage);
        university = findViewById(R.id.university);
        branch = findViewById(R.id.branch);
        gender = findViewById(R.id.gender);
        signOutButton = findViewById(R.id.logoutButton);
        firebaseAuth = FirebaseAuth.getInstance();


        // set the user information
        username.setText("Welcome:  "+user.getUsername());
        university.setText("Univeristy:  "+user.getUniveristy());
        branch.setText("Branch:  "+user.getBranch());
        gender.setText("Gender:  "+user.getGender());


        // set the user profile image..
        Picasso.with(getApplicationContext())
                .load(user.getImageUrl())
                .resize(400,400)
                .into(profileImage);


        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        // sign out the current firebase user and navigate to MainActivity (Login Page)
                firebaseAuth.signOut();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }
}
