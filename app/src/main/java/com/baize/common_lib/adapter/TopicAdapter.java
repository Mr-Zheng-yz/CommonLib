package com.baize.common_lib.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baize.common_lib.base.BaseRVAdapter;
import com.baize.common_lib.databinding.ItemTextBinding;

import java.util.ArrayList;
import java.util.List;

public class TopicAdapter extends BaseRVAdapter<String, TopicAdapter.TopicViewHolder> {

    public final List<String> selectList = new ArrayList<>();

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTextBinding vb = ItemTextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TopicViewHolder(vb);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        String data = list.get(position);
        holder.vb.tvText.setText(data);
        if (selectList.contains(data)) {
            holder.vb.getRoot().setCardBackgroundColor(Color.BLUE);
            holder.vb.tvText.setTextColor(Color.WHITE);
        } else {
            holder.vb.getRoot().setCardBackgroundColor(Color.WHITE);
            holder.vb.tvText.setTextColor(Color.GRAY);
        }
        holder.vb.getRoot().setOnClickListener((v) -> {
            if (selectList.contains(data)) {
                selectList.remove(data);
            } else {
                selectList.add(data);
            }
            notifyItemChanged(position);
        });
    }

    public List<String> getSelectList() {
        return selectList;
    }

    public void clearSelect() {
        selectList.clear();
        notifyDataSetChanged();
    }

    /**
     * VH
     */
    class TopicViewHolder extends RecyclerView.ViewHolder {
        ItemTextBinding vb;

        public TopicViewHolder(@NonNull ItemTextBinding vb) {
            super(vb.getRoot());
            this.vb = vb;
        }
    }
}
