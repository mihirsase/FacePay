package com.example.facepay.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.facepay.Models.ModelHistory;
import com.example.facepay.Models.ModelWallet;
import com.example.facepay.R;
import com.example.facepay.utls.AdapterHistory;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class History extends AppCompatActivity {

    DatabaseReference database;


    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;
    private static final String TAG = "History";
    private ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.list);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        database = FirebaseDatabase.getInstance().getReference();

        
        fetch();
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }



    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("History").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        FirebaseRecyclerOptions<ModelHistory> options = new FirebaseRecyclerOptions.Builder<ModelHistory>()
                .setQuery(query, new SnapshotParser<ModelHistory>() {
                    @NonNull
                    @Override
                    public ModelHistory parseSnapshot(@NonNull DataSnapshot snapshot) {
                        String transactionId = snapshot.child("Transaction Id").getValue().toString();

                        List<ModelHistory.CartItem> list = new ArrayList<>();
                        for (DataSnapshot postSnapshot: snapshot.child("Cart").getChildren()) {
                            String name = postSnapshot.child("Name").getValue().toString();
                            Log.e(TAG, "parseSnapshot: "+postSnapshot.toString() );
                            list.add(new ModelHistory.CartItem(postSnapshot.child("Name").getValue().toString(),
                                    postSnapshot.child("Weight").getValue().toString(),
                                    Integer.parseInt(postSnapshot.child("Price").getValue().toString()),
                                    Integer.parseInt(postSnapshot.child("Quantity").getValue().toString())
                                    ));
                        }
                        return new ModelHistory(transactionId,list);
                    }
                }).build();

        adapter = new FirebaseRecyclerAdapter<ModelHistory, ViewHolder>(options) {

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_history, parent, false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i, @NonNull final ModelHistory modelHistory) {

                database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        DataSnapshot ds =dataSnapshot.child("Transactions").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(modelHistory.getTransactionId());
                        viewHolder.date.setText(ds.child("Date").getValue().toString());
                        viewHolder.time.setText(ds.child("Time").getValue().toString());
                        viewHolder.closingBalance.setText("Closing Balance: "+ds.child("Closing Balance").getValue().toString());
                        viewHolder.amount.setText("₹"+ds.child("Amount").getValue().toString());
                        
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(History.this);
                linearLayoutManager1.setReverseLayout(true);
                linearLayoutManager1.setStackFromEnd(true);
                viewHolder.recyclerView.setLayoutManager(linearLayoutManager1);
                viewHolder.recyclerView.setHasFixedSize(true);
                AdapterHistory adapterHistory = new AdapterHistory();
                adapterHistory.addAll(modelHistory.getCartItemList());
                viewHolder.recyclerView.setAdapter(adapterHistory);

            }
        };
        recyclerView.setAdapter(adapter);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout root;
        public TextView purpose,amount,date,time,closingBalance;
        RecyclerView recyclerView;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.list_root);
            purpose = itemView.findViewById(R.id.purpose);
            amount = itemView.findViewById(R.id.amount);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            closingBalance = itemView.findViewById(R.id.closingBalance);
            recyclerView = itemView.findViewById(R.id.list);
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
