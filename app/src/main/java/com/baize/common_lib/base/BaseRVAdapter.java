package com.baize.common_lib.base;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRVAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected List<T> list;

    public BaseRVAdapter() {
        this(null);
    }

    public BaseRVAdapter(List<T> list) {
        this.list = list != null ? list : new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public void setNewList(List<T> list) {
        replaceAll(list);
    }

    public void replaceAll(List<T> list) {
        this.list.clear();
        if (list != null) {
            this.list.addAll(list);
        }
        notifyDataSetChanged();
    }

    public T getItem(int position) {
        if (position >= 0 && position < list.size()) {
            return list.get(position);
        } else {
            return null;
        }
    }

    public void add(T data) {
        list.add(data);
        notifyItemInserted(list.size());
    }

    public void remove(T data) {
        int index = list.indexOf(data);
        if (index != -1) {
            list.remove(index);
            notifyItemRemoved(index);
        }
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void notifyItem(int index) {
        if (index > -1 && index < list.size()) {
            notifyItemChanged(index);
        }
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public List<T> getList() {
        return list;
    }

    public static interface OnRvItemListener<T> {
        void onItemClick(T data, int position);
    }
}
