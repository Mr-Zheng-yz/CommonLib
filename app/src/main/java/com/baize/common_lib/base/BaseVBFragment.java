package com.baize.common_lib.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;


public abstract class BaseVBFragment<VB extends ViewBinding> extends Fragment {
    private static final String TAG = "BaseVBFragment";

    protected VB vb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        vb = createViewBinding(inflater, container);
        return vb.getRoot();
    }

    /**
     * 通过反射创建 ViewBinding 实例
     */
    protected abstract VB createViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);
}
