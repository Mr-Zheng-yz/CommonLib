package com.baize.common_lib.base;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class BaseRVHeadAdapter<T, VH extends RecyclerView.ViewHolder> extends BaseRVAdapter<T, VH> {
    public static final int VIEW_TYPE_HEADER = -1;
    public static final int VIEW_TYPE_FOOTER = -2;
    private View headerView;
    private View footerView;

    public BaseRVHeadAdapter(List<T> list) {
        super(list);
    }

    /**
     * LayoutInflater.inflate(layoutId, parent, false) 在解析 XML 根标签时，会调用 parent.generateLayoutParams(attrs) 来生成 LayoutParams。
     * 而 RecyclerView.generateLayoutParams() 的实现里要求 mLayout（即LayoutManager）必须先存在，否则就会抛 IllegalStateException: RecyclerView has no LayoutManager。
     * 所以需要把 setLayoutManager 提到 inflate head 之前
     * @param headerView
     */
    public void addHeaderView(View headerView) {
        if (this.headerView == headerView) return;
        this.headerView = headerView;
        notifyDataSetChanged();
    }

    public void removeHeaderView() {
        if (headerView != null) {
            headerView = null;
            notifyDataSetChanged();
        }
    }

    public boolean hasHeader() {
        return headerView != null;
    }

    /**
     * 与 addHeaderView 对称的尾布局入口；同对象或 null 直接 return，避免无谓刷新。
     * @param footerView
     */
    public void addFooterView(View footerView) {
        if (this.footerView == footerView) return;
        this.footerView = footerView;
        notifyDataSetChanged();
    }

    public void removeFooterView() {
        if (footerView != null) {
            footerView = null;
            notifyDataSetChanged();
        }
    }

    public boolean hasFooter() {
        return footerView != null;
    }

    @Override
    public final int getItemCount() {
        return list.size() + (hasHeader() ? 1 : 0) + (hasFooter() ? 1 : 0);
    }

    @Override
    public final int getItemViewType(int position) {
        if (hasHeader() && position == 0) return VIEW_TYPE_HEADER;
        if (hasFooter() && position == getItemCount() - 1) return VIEW_TYPE_FOOTER;
        return 0;
    }

    @Override
    public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            if (headerView.getLayoutParams() == null) {
                headerView.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            return (VH) new HeaderViewHolder(headerView);
        }
        if (viewType == VIEW_TYPE_FOOTER) {
            if (footerView.getLayoutParams() == null) {
                footerView.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            return (VH) new FooterViewHolder(footerView);
        }
        return onCreateDataViewHolder(parent, viewType);
    }


    @Override
    public final void onBindViewHolder(VH holder, int position) {
        if (hasHeader() && position == 0) return; // header 无需绑数据
        if (hasFooter() && position == getItemCount() - 1) return; // footer 无需绑数据
        int dataPosition = hasHeader() ? position - 1 : position;
        onBindDataViewHolder(holder, list.get(dataPosition), dataPosition);
    }

    /** 创建数据项的 ViewHolder */
    protected abstract VH onCreateDataViewHolder(ViewGroup parent, int viewType);

    /** 绑定数据项，position 已扣除 header 偏移 */
    protected abstract void onBindDataViewHolder(VH holder, T bean, int dataPosition);

    public int getDataPosition(int position) {
        if (hasHeader()) {
            if (position == 0) return -1;
            position -= 1;
        }
        if (hasFooter() && position == getItemCount() - 1) return -1;
        if (position >= 0 && position < list.size()) return position;
        return -1;
    }

    @Override
    public void add(T data) {
        list.add(data);
        int index = list.size();
        int rvIndex = hasHeader() ? index + 1 : index;
        notifyItemInserted(rvIndex);
    }

    public void remove(T data) {
        int index = list.indexOf(data);
        if (index != -1) {
            list.remove(index);
            int rvIndex = hasHeader() ? index + 1 : index;
            notifyItemRemoved(rvIndex);
        }
    }

    public void notifyItem(int index) {
        if (index > -1 && index < list.size()) {
            int rvIndex = hasHeader() ? index + 1 : index;
            notifyItemChanged(rvIndex);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

}
