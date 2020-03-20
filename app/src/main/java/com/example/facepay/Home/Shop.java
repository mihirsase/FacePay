package com.example.facepay.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.facepay.Models.ModelShop;
import com.example.facepay.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

public class Shop extends AppCompatActivity {

    private  TextView tvbalance,tvTotal,warning;
    private ImageView back;
    private FloatingActionButton fab;
    private int balance;
    DatabaseReference database;
    private RelativeLayout relativeLayout;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;


    String name,weight;
    int price;
    int total=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_shop);

        relativeLayout = findViewById(R.id.relative);
        tvbalance = findViewById(R.id.tvBal);
        warning = findViewById(R.id.warning);
        back = findViewById(R.id.back);
        fab = findViewById(R.id.floating_action_button);
        recyclerView = findViewById(R.id.list);
        tvTotal = findViewById(R.id.tvTotal);
        database = FirebaseDatabase.getInstance().getReference();
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                balance =  Integer.parseInt(dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Balance").getValue().toString());
                tvbalance.setText(Integer.toString(balance));
                if(total>balance){
                    warning.setVisibility(View.VISIBLE);
                }else{
                    warning.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });



        warning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar snackbar = Snackbar
                        .make(relativeLayout, "Insufficient Balance", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Shop.this, Home.class);
                startActivity(intent);
            }
        });
        tvbalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Shop.this, Wallet.class);
                startActivity(intent);
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


              IntentIntegrator intentIntegrator=new IntentIntegrator(Shop.this);
              intentIntegrator.setOrientationLocked(false).initiateScan();
            }
        });



        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        fetch();


    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("UserCart").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        FirebaseRecyclerOptions<ModelShop> options = new FirebaseRecyclerOptions.Builder<ModelShop>()
                .setQuery(query, new SnapshotParser<ModelShop>() {
                    @NonNull
                    @Override
                    public ModelShop parseSnapshot(@NonNull DataSnapshot snapshot) {
                        return new ModelShop(snapshot.child("Name").getValue().toString(),snapshot.child("Weight").getValue().toString(),
                                Integer.parseInt(snapshot.child("Price").getValue().toString()),
                                Integer.parseInt(snapshot.child("Quantity").getValue().toString()),snapshot.child("ID").getValue().toString());
                    }
                }).build();

        adapter = new FirebaseRecyclerAdapter<ModelShop,ViewHolder>(options){

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_shop, parent, false);

                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, int i, @NonNull final ModelShop modelShop) {
                holder.setProduct(modelShop.getProduct());
                holder.setWeight(modelShop.getWeight());
                holder.setAmount(String.valueOf(modelShop.getAmount()*modelShop.getQuantity()));
                holder.setQuantity(String.valueOf(modelShop.getQuantity()));

                holder.quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                    @Override
                    public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                        DatabaseReference dref =FirebaseDatabase.getInstance().getReference();
                        if(Integer.parseInt(holder.quantity.getNumber()) == 0){
                            dref.child("UserCart").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(modelShop.getId()).removeValue();
                        }else {

                            dref.child("UserCart").child(FirebaseAuth.getInstance().getCurrentUser()
                                    .getUid()).child(modelShop.getId()).child("Quantity").setValue(newValue);
                        }
                    }
                });


            }


            @Override
            public void onDataChanged() {
                super.onDataChanged();
                total=0;
                for(int i=0;i<adapter.getItemCount();i++){
                    ModelShop modelShop = (ModelShop) adapter.getItem(i);
                    int amt = modelShop.getAmount();
                    int qnt = modelShop.getQuantity();
                    total = total + (amt*qnt);
                }

                tvTotal.setText("₹ "+total);
                if(total>balance){
                    warning.setVisibility(View.VISIBLE);
                }else{
                    warning.setVisibility(View.GONE);
                }
            }


        };
        recyclerView.setAdapter(adapter);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout root;
        public TextView product,weight,amount;
        public ElegantNumberButton quantity;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.list_root);
            product = itemView.findViewById(R.id.product);
            weight = itemView.findViewById(R.id.weight);
            amount = itemView.findViewById(R.id.amount);
            quantity = itemView.findViewById(R.id.quantity);
        }

        public void setProduct(String string) {
            product.setText(string);
        }


        public void setWeight(String string) {
            weight.setText(string);
        }

        public void setAmount(String string) {
            amount.setText("₹ "+string);
        }
        public void setQuantity(String string) {
            quantity.setNumber(string);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
              //  Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                StoreToDatabase(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void StoreToDatabase(final String barcode) {

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("Products").child(barcode).exists()){
                    Toast.makeText(Shop.this,"Invalid Barcode",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(dataSnapshot.child("UserCart").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(barcode).exists()){
                    String qnt= dataSnapshot.child("UserCart").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(barcode).child("Quantity").getValue().toString();
                    int quantity = Integer.parseInt(qnt);
                    database.child("UserCart").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(barcode).child("Quantity").setValue(quantity+1);
                }else{


                    DatabaseReference dref = FirebaseDatabase.getInstance().getReference().child("Products").child(barcode);
                    dref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("ID", barcode);
                            childUpdates.put("Name", dataSnapshot.child("Name").getValue().toString());
                            childUpdates.put("Price", dataSnapshot.child("Price").getValue());
                            childUpdates.put("Weight", dataSnapshot.child("Weight").getValue().toString());
                            childUpdates.put("Quantity", 1);
                            database.child("UserCart").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(barcode).updateChildren(childUpdates);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });






                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });







    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
