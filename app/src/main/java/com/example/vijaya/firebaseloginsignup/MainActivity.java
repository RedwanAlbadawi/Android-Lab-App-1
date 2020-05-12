package com.example.vijaya.firebaseloginsignup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.vijaya.firebaseloginsignup.Model.User;
import com.example.vijaya.firebaseloginsignup.Utilities.DialogManager;
import com.example.vijaya.firebaseloginsignup.Utilities.InternetConnectivityManager;
import com.facebook.CallbackManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.Serializable;
public class MainActivity extends AppCompatActivity {
    EditText email,password;
    Button loginButton,facebookLoginButton;
    Dialog progressDialog;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set app title bar name
        getSupportActionBar().setTitle("Login");

        callbackManager = CallbackManager.Factory.create();


        // initialize the views.
        initViews();


        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // first of all check whether the credentials are provided or not.
                if(isCredentialsProvided()){
                    // then check if we have internet available
                    if(InternetConnectivityManager.isNetworkConnected(getApplicationContext())){
                        // if both..the credentials are provided and we have internet available show progress dialog and sign in using
                        // user email and password
                        progressDialog.show();
                        firebaseAuth.signInWithEmailAndPassword(email.getText().toString().trim(),
                                password.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            // if user sign in is successfull check whether the user has verified his/her email address
                                            // or not.
                                            if(firebaseAuth.getCurrentUser().isEmailVerified()){
                                                // if email address is also verified then get current userid
                                                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                                String userId = firebaseUser.getUid();

                                                // get the user information stored under that id in firebase database.
                                                databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                        // check whether record exists or not.
                                                        if(dataSnapshot.exists()){
                                                            // if we have record available then create User object and put that record in
                                                            // that object and pass that object using intent

                                                            User user = dataSnapshot.getValue(User.class);
                                                            progressDialog.dismiss();
                                                            Intent intent = new Intent(getApplicationContext(),WelcomeActivity.class);
                                                            intent.putExtra("user",(Serializable) user);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                        else{
                                                            progressDialog.dismiss();
                                                            Toast.makeText(MainActivity.this, "User Information does not exist..", Toast.LENGTH_SHORT).show();
                                                        }


                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(MainActivity.this, "Exception: "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });


                                            }
                                            else{

                                                // if email address is not verified show user a dialog message and send verification link
                                                progressDialog.dismiss();
                                                email.setText("");
                                                password.setText("");
                                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                                                builder.setTitle("Alert");
                                                builder.setMessage("Looks like your email is not yet verified. Check your email account for verification link.");
                                                builder.setCancelable(true);
                                                builder.setPositiveButton(
                                                        "Ok",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                                firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            Toast.makeText(MainActivity.this, "Verification Link Sent.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        else{
                                                                            Toast.makeText(MainActivity.this, "Error Sending Verification Link.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });


                                                AlertDialog alertDialog = builder.create();
                                                alertDialog.show();
                                            }
                                        }
                                        else{
                                            progressDialog.dismiss();
                                            if(task.getException() instanceof FirebaseAuthInvalidUserException){
                                                Toast.makeText(MainActivity.this, "this email is not registered", Toast.LENGTH_SHORT).show();
                                            }
                                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                                Toast.makeText(MainActivity.this, "Your password is wrong", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    }
                                });

                    }
                    else{
                        DialogManager.showAlertDialog(MainActivity.this,getResources().getString(R.string.connectionProblemTitle),
                                getResources().getString(R.string.connectionProblemMessage),R.drawable.no_internet,null);
                    }
                }

            }
        });

        findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignupActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.facebookLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    // this method is used to initialze the views.
    private void initViews() {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        facebookLoginButton = findViewById(R.id.facebookLogin);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");















        progressDialog = new Dialog(MainActivity.this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.custom_dialog_progress);
        /* Custom setting to change TextView text,Color and Text Size according to your Preference*/
        TextView progressTv = progressDialog.findViewById(R.id.progress_tv);
        progressTv.setText(getResources().getString(R.string.loading));
        progressTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        progressTv.setTextSize(19F);
        if(progressDialog.getWindow() != null)
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setCancelable(false);





    }


    // this method is used to check that user has provided credentials or not.
    private boolean isCredentialsProvided(){
        if(TextUtils.isEmpty(email.getText())){
            email.setError("Please Enter Email");
            return false;
        }
        else if(TextUtils.isEmpty(password.getText())){
            password.setError("Please Enter Password");
            return false;
        }
        return true;
    }


}
