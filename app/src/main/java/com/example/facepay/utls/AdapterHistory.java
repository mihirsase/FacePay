package com.example.facepay.utls;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.facepay.Models.ModelHistory;
import com.example.facepay.R;

import java.util.ArrayList;
import java.util.List;


public class AdapterHistory extends RecyclerView.Adapter<AdapterHistory.MyViewHolder> {

    List<ModelHistory.CartItem> list ;

    public AdapterHistory() {
        this.list = new ArrayList<>();
    }

    public void addAll(List<ModelHistory.CartItem> cartItemList){
        list.addAll(cartItemList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_cart, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ModelHistory.CartItem model = list.get(position);
        holder.product.setText(model.getName()+"  (x"+model.getQuantity()+")");
        holder.amount.setText("₹"+(model.getPrice()*model.getQuantity()));
        holder.weight.setText(model.getWeight());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView product,weight,amount;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            product = itemView.findViewById(R.id.product);
            weight = itemView.findViewById(R.id.weight);
            amount = itemView.findViewById(R.id.amount);
        }
    }
}
