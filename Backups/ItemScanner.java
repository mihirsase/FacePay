package com.example.facepay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import info.androidhive.barcode.BarcodeReader;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemScanner extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {
    BarcodeReader barcodeReader;
    DatabaseReference database;
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_item_scanner);
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.fragment);
        database = FirebaseDatabase.getInstance().getReference();
        fab =findViewById(R.id.back);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemScanner.this, Shop.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onScanned(Barcode barcode) {
        barcodeReader.playBeep();

        StoreToDatabase(barcode);


    }

    private void StoreToDatabase(final Barcode barcode) {


        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.child("Products").child(barcode.displayValue).exists()){
                    Toast.makeText(ItemScanner.this,"Invalid Barcode",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(dataSnapshot.child("UserCart").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(barcode.displayValue).exists()){
                    String qnt= dataSnapshot.child("UserCart").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(barcode.displayValue).child("Quantity").getValue().toString();
                    int quantity = Integer.parseInt(qnt);
                    database.child("UserCart").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(barcode.displayValue).child("Quantity").setValue(quantity+1);

                }else{


                    DatabaseReference dref = FirebaseDatabase.getInstance().getReference().child("Products").child(barcode.displayValue);
                    dref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("ID", barcode.displayValue);
                            childUpdates.put("Name", dataSnapshot.child("Name").getValue().toString());
                            childUpdates.put("Price", dataSnapshot.child("Price").getValue());
                            childUpdates.put("Weight", dataSnapshot.child("Weight").getValue().toString());
                            childUpdates.put("Quantity", 1);
                            database.child("UserCart").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(barcode.displayValue).updateChildren(childUpdates);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });






                }

                Intent intent = new Intent(ItemScanner.this, Shop.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String errorMessage) {

    }

    @Override
    public void onCameraPermissionDenied() {

    }
}
