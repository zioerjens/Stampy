package com.example.zioerjens.stampy;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<Vaucher> mDataset;
    private VoucherView voucherView;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public RelativeLayout mTextView;
        private TextView dateTextView;
        private Vaucher mItem;

        public ViewHolder(RelativeLayout v) {
            super(v);
            mTextView = v;
            v.setOnClickListener(this);
            dateTextView = (TextView) v.findViewById(R.id.date);
        }

        public void setItem(Vaucher item){
            mItem = item;
            dateTextView.setText(item.dateTime);
        }

        @Override
        public void onClick(View v) {
            voucherView.createPopUp(mItem.code);
            Log.e("LOG","Click");
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List<Vaucher> myDataset, VoucherView voucherView) {
        this.voucherView = voucherView;
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.voucher_single_row, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.mTextView.setText(mDataset[position]);
        holder.setItem(mDataset.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
