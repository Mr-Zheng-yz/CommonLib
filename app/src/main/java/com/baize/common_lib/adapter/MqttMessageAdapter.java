package com.baize.common_lib.adapter;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baize.common_lib.base.BaseRVAdapter;
import com.baize.common_lib.databinding.ItemMqttMessageBinding;

public class MqttMessageAdapter extends BaseRVAdapter<Pair<String, String>, MqttMessageAdapter.ViewHolder> {

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMqttMessageBinding vb = ItemMqttMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(vb);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pair<String, String> data = list.get(position);
        holder.vb.tvTopic.setText(data.first);
        holder.vb.tvContent.setText(data.second);
    }

    /**
     * VH
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        ItemMqttMessageBinding vb;

        public ViewHolder(@NonNull ItemMqttMessageBinding vb) {
            super(vb.getRoot());
            this.vb = vb;
        }
    }
}
