package com.stdio.warehousecontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public  class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {
    LayoutInflater inflater;
    List<DataModel> modelList;

    public MainAdapter(Context context, List<DataModel> list) {
        inflater = LayoutInflater.from(context);
        modelList = new ArrayList<>(list);
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recycler_row, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        holder.bindData(modelList.get(position));
    }

    public void removeItem(int position) {
        modelList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView tvArticle, tvBarcode, tvName, tvCount, tvSize;

        public MainViewHolder(View itemView) {
            super(itemView);
            tvArticle = itemView.findViewById(R.id.tvArticle);
            tvBarcode = itemView.findViewById(R.id.tvBarcode);
            tvName = itemView.findViewById(R.id.tvName);
            tvCount = itemView.findViewById(R.id.tvCount);
            tvSize = itemView.findViewById(R.id.tvSize);
        }

        public void bindData(DataModel DataModel) {
            tvArticle.setText("Артикул: " + DataModel.article);
            tvBarcode.setText("Штрих-код: " + DataModel.barcode);
            tvName.setText(DataModel.name);
            tvCount.setText("Количество: " + DataModel.count);
            tvSize.setText("Размер: " + DataModel.size);
        }
    }
}
