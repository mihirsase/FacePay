package com.example.facepay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Transfer extends AppCompatActivity {

    private EditText contact;
    private Button proceed;
    private ImageView back,iv_transfer;
    private TextView balance;
    DatabaseReference database;
    int bal,amount,balance1,flag=0;
    String usercontact;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_transfer);

        contact = findViewById(R.id.contact);
        proceed = findViewById(R.id.button_proceed);
        balance = findViewById(R.id.rs);
        iv_transfer = findViewById(R.id.iv_transfer);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Transfer.this,Home.class);
                startActivity(intent);
            }
        });

        database = FirebaseDatabase.getInstance().getReference();
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                bal =  Integer.parseInt(dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Balance").getValue().toString());
                balance.setText(Integer.toString(bal));
                usercontact = dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Contact").getValue().toString();

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });


        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mobile = contact.getText().toString();

                if(mobile.isEmpty()){
                    contact.setError("Contact Required");
                    contact.requestFocus();
                    return;
                }


                if(mobile.length()!=10){
                    contact.setError("Enter Valid Contact");
                    contact.requestFocus();
                    return;
                }

                if(mobile.equals(usercontact)){
                    contact.setError("You cannot transfer to your own account");
                    contact.requestFocus();
                    return;
                }

                searchContact(mobile);


            }
        });



        iv_transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator intentIntegrator=new IntentIntegrator(Transfer.this);
                intentIntegrator.setOrientationLocked(false).initiateScan();
            }
        });


    }

    private void searchContact(String mobile) {

        database = FirebaseDatabase.getInstance().getReference();
        Query searchQuery = database.child("Users").orderByChild("Contact").equalTo(mobile);
        searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){


                    String uid = dataSnapshot.getValue().toString();
                    uid = uid.substring(uid.indexOf("{") + 1);
                    uid = uid.substring(0, uid.indexOf("="));
                    Log.e("$$$",uid.trim());
                    displayDialog(uid);
                }else{
                    contact.setError("Contact Does Not Exists");
                    contact.requestFocus();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayDialog(final String uid) {

        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_transfer_money, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);
        Button button =dialogView.findViewById(R.id.buttonTransfer);
        final EditText editText = dialogView.findViewById(R.id.amount);


        final AlertDialog alertDialog = builder.create();
        //finally creating the alert dialog and displaying it
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText.getText().toString().isEmpty() ){
                    editText.setError("Enter Amount");
                    editText.setFocusable(true);
                    return;
                }
                if(Integer.parseInt(editText.getText().toString())>15000){
                    editText.setError("Amount Below ₹15000");
                    editText.setFocusable(true);
                    return;
                }
                amount = Integer.parseInt(editText.getText().toString());
                if(amount>bal){
                    editText.setError("Insufficient Balanace");
                    editText.requestFocus();
                    return;
                }

                database.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Balance").setValue(bal-amount)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Transfer.this,"Transfer Failed",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        });


                database.child("Users").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        balance1 = Integer.parseInt(dataSnapshot.child("Balance").getValue().toString());
                        database.child("Users").child(uid).child("Balance").setValue(balance1+amount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                String date = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm aa").format(new Date());

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("Purpose","TransferMoneySent");
                childUpdates.put("Amount", amount);
                childUpdates.put("Closing Balance", bal-amount);
                childUpdates.put("Date",date);
                childUpdates.put("Time",time);
                String push = database.push().getKey();
                database.child("Transactions").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(push).updateChildren(childUpdates)
                        .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Transfer.this,"Transfer Failed",Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
                Map<String, Object> childUpdates1 = new HashMap<>();
                childUpdates1.put("Purpose","TransferMoneyReceive");
                childUpdates1.put("Amount", amount);
                childUpdates1.put("Closing Balance", balance1+amount);
                childUpdates1.put("Date",date);
                childUpdates1.put("Time",time);
                database.child("Transactions").child(uid).child(push).updateChildren(childUpdates1).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Transfer.this,"Transfer Successful",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Transfer.this,"Transfer Failed",Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("$$$",push+" "+uid);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //  Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                searchUID(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void searchUID(final String uid) {

        database = FirebaseDatabase.getInstance().getReference();
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("Users").child(uid).exists()){
                    Toast.makeText(Transfer.this,"User Does Not Exist",Toast.LENGTH_SHORT).show();
                    return;
                }
                displayDialog(uid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
