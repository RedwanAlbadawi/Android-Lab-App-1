package com.example.vijaya.firebaseloginsignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vijaya.firebaseloginsignup.Model.User;
import com.example.vijaya.firebaseloginsignup.Utilities.DialogManager;
import com.example.vijaya.firebaseloginsignup.Utilities.FileUtils;
import com.example.vijaya.firebaseloginsignup.Utilities.InternetConnectivityManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class SignupActivity extends AppCompatActivity {

    CircleImageView profileImage;
    EditText username,email,password,cpassword,university,branch;
    RadioGroup radioGroup;
    Button signUpButton,facebookSignUpbutton;
    Dialog progressDialog;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    FirebaseUser firebaseUser;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initViews();

        // set app title bar name
        getSupportActionBar().setTitle("Signup");

        // get image from the device upon profile image click.
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermissionREAD_EXTERNAL_STORAGE(SignupActivity.this)){

                    // create gallery intent,set type to image/* so that we can have images only
                    Intent gallery = new Intent();
                    gallery.setType("image/*");
                    gallery.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);
                }

            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if signup data is completely provided by the user
                if(isDataProvided()){

                    // check for internet connectivity

                    if(InternetConnectivityManager.isNetworkConnected(getApplicationContext())){

                        // show progress dialog and do some stuff.
                        progressDialog.show();

                        // create user with email address and password
                        firebaseAuth.createUserWithEmailAndPassword(
                                email.getText().toString(),
                                password.getText().toString()
                        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){

                                    // on successfull creation send user email verification link
                                    firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                // once verification link is sent to the user store user information in database.

                                                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                                String userId = firebaseUser.getUid();
                                                //Toast.makeText(SignupActivity.this, "User id = "+userId, Toast.LENGTH_SHORT).show();
                                                String mUsername = username.getText().toString();
                                                String mUniversity = university.getText().toString();
                                                String mBranch = branch.getText().toString();
                                                String mGender = getGender();
                                                final User user = new User(mUsername,mUniversity,mBranch,mGender,null);

                                                // get file path and name of the image selected by the user.

                                                String filePath = FileUtils.getPath(getApplicationContext(),imageUri);

                                                String filename=filePath.substring(filePath.lastIndexOf("/")+1);

                                                // upload image and then store record in db
                                                final StorageReference fileReference = storageReference.child(userId).child(filename);
                                                fileReference.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                    @Override
                                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                        if (!task.isSuccessful()) {
                                                            throw task.getException();
                                                        }
                                                        return fileReference.getDownloadUrl();
                                                        }
                                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Uri> task) {
                                                        if (task.isSuccessful()) {
                                                            Uri downloadUri = task.getResult();
                                                            user.setImageUrl(downloadUri.toString());
                                                            setDbNodeWith(user);
                                                        } else {
                                                            Toast.makeText(SignupActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                            }
                                            else{
                                                Toast.makeText(SignupActivity.this, "Exception: "+task.getException(), Toast.LENGTH_SHORT).show();
                                            }


                                        }


                                    });

                                }
                                else{
                                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                        Toast.makeText(SignupActivity.this,R.string.errorEmailExists, Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            }
                        });
                    }
                    else{
                        DialogManager.showAlertDialog(SignupActivity.this,getResources().getString(R.string.connectionProblemTitle),
                                getResources().getString(R.string.connectionProblemMessage),R.drawable.no_internet,null);
                    }


                }

            }
        });


    }


    // once the image is stored in firebase storage its time to store the record in database.
    private void setDbNodeWith(User user) {

        databaseReference.child(firebaseUser.getUid()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                        builder.setTitle("Registration Successful!");
                        builder.setMessage("An email with verification link send to your email account.");
                        builder.setCancelable(true);
                        builder.setPositiveButton(
                                "Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });


                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignupActivity.this, "Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

    }


    // form validation checks here.
    private boolean isDataProvided() {
        if(imageUri == null){
            Toast.makeText(this, "Select Profile Image ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(username.getText())){
            username.setError("Enter username");
            return false;
        }
        else if(TextUtils.isEmpty(email.getText())){
            email.setError("Enter email");
            return false;
        }
        else if(TextUtils.isEmpty(password.getText())){
            password.setError("Enter password");
            return false;
        }
        else if(TextUtils.isEmpty(cpassword.getText())){
            cpassword.setError("Enter password again");
            return false;
        }
        else if(! password.getText().toString().equals(cpassword.getText().toString())){
            Toast.makeText(this, "Both password should be same", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(TextUtils.isEmpty(university.getText())){
            university.setError("Enter University");
            return false;
        }
        else if(TextUtils.isEmpty(branch.getText())){
            branch.setError("Enter branch");
            return false;
        }
        else if(radioGroup.getCheckedRadioButtonId() == -1){
            Toast.makeText(this, "Select Gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    // views initialization
    private void initViews() {

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        cpassword = findViewById(R.id.cpassword);
        university = findViewById(R.id.university);
        branch = findViewById(R.id.branch);
        radioGroup = findViewById(R.id.gender);
        signUpButton = findViewById(R.id.registerButton);




        // firebase related initialization

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase  = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");

        firebaseStorage  = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("profileImages");




        progressDialog = new Dialog(SignupActivity.this);
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

    // this method is used to get the selected gender name from the radio buttons.
    private String getGender(){
        int radioButtonID = radioGroup.getCheckedRadioButtonId();
        View radioButton = radioGroup.findViewById(radioButtonID);
        int idx = radioGroup.indexOfChild(radioButton);
        RadioButton r = (RadioButton) radioGroup.getChildAt(idx);
        return r.getText().toString();
    }













    // FOR STORAGE PERMISSIONS


    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }


    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }



    private static final int PICK_IMAGE = 1;
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
