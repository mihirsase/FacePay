package com.example.facepay;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class Account extends AppCompatActivity {

    private TextView tv_email,tv_name,tv_contact;
    ImageView back,qrcode;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_account);


        tv_email = findViewById(R.id.text_email);
        tv_name = findViewById(R.id.text_name);
        tv_contact = findViewById(R.id.text_contact);
        qrcode = findViewById(R.id.qrcode);


        QRCodeWriter writer = new QRCodeWriter();
        try {
            //content
            BitMatrix bitMatrix = writer.encode(FirebaseAuth.getInstance().getCurrentUser().getUid(), BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            qrcode.setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }




        back = findViewById(R.id.back);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Account.this,Home.class);
                startActivity(intent);
            }
        });

        tv_email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();


        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                tv_name.setText(dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Name").getValue().toString());
                tv_contact.setText(dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Contact").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });




        tv_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final EditText editText = new EditText(getApplicationContext());
                FrameLayout container = new FrameLayout(getApplicationContext());
                FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                editText.setLayoutParams(params);
                container.addView(editText);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        Account.this);
                alertDialog.setTitle("Change Username");
                alertDialog.setMessage("Enter New Username");
                alertDialog.setCancelable(false);
                alertDialog.setView(container);
                alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(editText.getText().toString()==null){
                            Toast.makeText(Account.this,"Name Cannot Be Empty",Toast.LENGTH_SHORT).show();
                        }else{
                            updateName(editText.getText().toString());
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();





            }
        });



        tv_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                final EditText editText = new EditText(getApplicationContext());
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
                FrameLayout container = new FrameLayout(getApplicationContext());
                FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                editText.setLayoutParams(params);
                container.addView(editText);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        Account.this);
                alertDialog.setTitle("Change Contact");
                alertDialog.setMessage("Enter New Contact");
                alertDialog.setCancelable(false);
                alertDialog.setView(container);
                alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(editText.getText().toString().length()!=10){
                            Toast.makeText(Account.this,"Enter Valid Mobile Number",Toast.LENGTH_SHORT).show();
                        }else{

                            updateContact(editText.getText().toString());
                        }


                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();






            }
        });




    }

    private void updateName(String name) {
        databaseReference.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Name").setValue(name);
    }

    private void updateContact(String contact) {
        databaseReference.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Contact").setValue(contact);
    }
}
