package com.example.facepay.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.facepay.Models.ModelWallet;
import com.example.facepay.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Wallet extends AppCompatActivity {

    private TextView balance;
    private FloatingActionButton fabAdd;
    private ImageView back;
    int bal,amount;
    DatabaseReference database;


    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_wallet);


        recyclerView = findViewById(R.id.list);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        fetch();




        balance = findViewById(R.id.rs);
        fabAdd = findViewById(R.id.floating_action_button);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Wallet.this, Home.class);
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

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });


        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDialog();
            }
        });





    }



    private void displayDialog() {
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_money, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);
        Button button =dialogView.findViewById(R.id.buttonAdd);
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
                database.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("Balance").setValue(amount+bal);


                String date = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm aa").format(new Date());

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("Purpose","AddMoney");
                childUpdates.put("Amount", amount);
                childUpdates.put("Closing Balance", amount+bal);
                childUpdates.put("Date",date);
                childUpdates.put("Time",time);
                database.child("Transactions").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push().updateChildren(childUpdates);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();



    }






    private void fetch() {

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Transactions").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        FirebaseRecyclerOptions<ModelWallet> options = new FirebaseRecyclerOptions.Builder<ModelWallet>()
                .setQuery(query, new SnapshotParser<ModelWallet>() {
                    @NonNull
                    @Override
                    public ModelWallet parseSnapshot(@NonNull DataSnapshot snapshot) {
                        return new ModelWallet(snapshot.child("Purpose").getValue().toString(),
                                snapshot.child("Date").getValue().toString(),
                                snapshot.child("Time").getValue().toString(),
                                Integer.parseInt(snapshot.child("Amount").getValue().toString()),
                                Integer.parseInt(snapshot.child("Closing Balance").getValue().toString()));
                    }
                }).build();

        adapter = new FirebaseRecyclerAdapter<ModelWallet,ViewHolder>(options){

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_wallet, parent, false);

                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ModelWallet model) {
                if(model.getPurpose().equals("AddMoney")){
                    holder.setPurpose("Added To Wallet");
                }else if(model.getPurpose().equals("TransferMoneySent")){
                    holder.setPurpose("Money Sent");
                }else if(model.getPurpose().equals("TransferMoneyReceive")){
                    holder.setPurpose("Money Received");
                }else if(model.getPurpose().equals("CartDebit")){
                    holder.setPurpose("Cart Debit");
                }

                holder.setDate(model.getDate());
                holder.setTime(model.getTime());
                holder.setAmount(Integer.toString(model.getAmount()),model.getPurpose());
                holder.setClosingBalance("Closing Balance: "+Integer.toString(model.getClosingBalance()));


            }

        };

        recyclerView.setAdapter(adapter);
    }




    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout root;
        public TextView purpose,amount,date,time,closingBalance;


        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.list_root);
            purpose = itemView.findViewById(R.id.purpose);
            amount = itemView.findViewById(R.id.amount);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            closingBalance = itemView.findViewById(R.id.closingBalance);
        }

        public void setPurpose(String string) {
            purpose.setText(string);
        }

        public void setAmount(String string,String Purpose) {
            if(Purpose.equals("AddMoney")){
                amount.setText("+ ₹"+string);
                amount.setTextColor(Color.parseColor("#00c853"));
            }else if(Purpose.equals("TransferMoneySent")){
                amount.setText("- ₹"+string);
                amount.setTextColor(Color.parseColor("#f44336"));
            }else if(Purpose.equals("TransferMoneyReceive")){
                amount.setText("+ ₹"+string);
                amount.setTextColor(Color.parseColor("#00c853"));
            }else if(Purpose.equals("CartDebit")){
                amount.setText("- ₹"+string);
                amount.setTextColor(Color.parseColor("#f44336"));
            }

        }

        public void setDate(String string) {
            date.setText(string);
        }

        public void setTime(String string) {
            time.setText(string);
        }

        public void setClosingBalance(String string) {
            closingBalance.setText(string);
        }
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
