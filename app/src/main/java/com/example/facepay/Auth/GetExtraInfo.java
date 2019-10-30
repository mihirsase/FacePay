package com.example.facepay.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.facepay.Home.Home;
import com.example.facepay.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GetExtraInfo extends AppCompatActivity {
    private TextView tv_email,tv_notVerified;
    private Button save;
    private EditText name,mobile;
    private ImageView face,dummy;
    private ProgressBar progressBar;
    FirebaseAuth auth;
    private StorageReference mStorageRef;
    Bitmap bitmap;
    private boolean flag=false;
    private File file=null;
    String mCurrentPhotoPath;
    FirebaseDatabase database ;
    DatabaseReference dbRef;
    Map<String, Object> childUpdates;
    int myflag=0;
    String username,contact;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_get_extra_info);




        tv_email = findViewById(R.id.text_email);
        tv_notVerified=findViewById(R.id.text_not_verified);
        name = findViewById(R.id.edit_text_name);
        mobile = findViewById(R.id.text_phone);
        save = findViewById(R.id.button_save);
        face = findViewById(R.id.face);
        dummy = findViewById(R.id.dummy);
        auth = FirebaseAuth.getInstance();
        progressBar =findViewById(R.id.progressBar);
        FirebaseUser currentUser = auth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        dbRef = database.getReference("/Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
        tv_email.setText(currentUser.getEmail());
        childUpdates = new HashMap<>();


        face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkCameraPermission()) {

                    dispatchTakePictureIntent();

                }else{
                    requestCameraPermission();
                }
            }
        });



        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = name.getText().toString();
                contact = mobile.getText().toString();
                myflag=0;
                if(username.isEmpty()){
                    name.setError("Name Required");
                    name.requestFocus();
                    return;
                }
                if(contact.isEmpty() || contact.length()!=10){
                    mobile.setError("Valid Mobile Number Required");
                    mobile.requestFocus();
                    return;
                }

                //Check Original Mobile Validation
                DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                Query searchQuery = database.child("Users").orderByChild("Contact").equalTo(contact);
                searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            mobile.setError("Mobile Number Is Previously Used");
                            mobile.requestFocus();
                        }else{
                            //new contact add details and image to database
                            if(bitmap==null){
                                tv_notVerified.setVisibility(View.VISIBLE);

                            }else {
                                tv_notVerified.setVisibility(View.INVISIBLE);

                                progressBar.setVisibility(View.VISIBLE);

                                childUpdates.put("Name", username);
                                childUpdates.put("Contact", contact);
                                childUpdates.put("Balance", 0);
                                saveImageToFirebase(bitmap, childUpdates, dbRef);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });










            }
        });




    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.facepay.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 7);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            switch (requestCode) {
                case 7: {
                    if (resultCode == RESULT_OK) {
                        File file = new File(mCurrentPhotoPath);
                        bitmap = MediaStore.Images.Media
                                .getBitmap(getApplicationContext().getContentResolver(), Uri.fromFile(file));

                        if (bitmap != null) {
                            tv_notVerified.setVisibility(View.VISIBLE);
                            tv_notVerified.setText("Face Captured");
                            tv_notVerified.setTextColor(Color.GREEN);
                        }else{
                            tv_notVerified.setVisibility(View.VISIBLE);
                            return;
                        }
                    }
                    break;
                }
            }

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    private void saveImageToFirebase(Bitmap bitmap, final Map<String, Object> childUpdates, final DatabaseReference dbRef) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final StorageReference storageReference = mStorageRef
                .child("images/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] image=baos.toByteArray();
        UploadTask uploadTask= storageReference.putBytes(image);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dbRef.updateChildren(childUpdates);
                Intent intent = new Intent(GetExtraInfo.this, Home.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GetExtraInfo.this, "Upload Failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }






















    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    // main logic
                } else {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("This App requires camera permission to scan your face.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestCameraPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                200);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(GetExtraInfo.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
