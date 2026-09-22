package com.baize.common_lib.base;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;
import com.ocamara.common_libs.utils.LogUtil;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class BaseVBActivity<VB extends ViewBinding> extends FragmentActivity {
    public static final String TAG = "BaseVBActivity";
    protected VB vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = createViewBinding();
        if (vb != null) {
            setContentView(vb.getRoot());
        }
    }
    /**
     * 通过反射创建 ViewBinding 实例
     */
    private VB createViewBinding() {
        try {
            // 获取泛型参数的实际类型
            Type superclass = getClass().getGenericSuperclass();
            if (!(superclass instanceof ParameterizedType)) {
                return null;
            }
            Class<VB> clazz = (Class<VB>) ((ParameterizedType)
                    superclass).getActualTypeArguments()[0];

            // 调用 inflate 方法
            Method method = clazz.getMethod("inflate", LayoutInflater.class);
            return (VB) method.invoke(null, getLayoutInflater());
        } catch (Exception e) {
            LogUtil.e(TAG, "!!!ViewBinding 创建失败: " + e.getMessage());
            return null;
        }
    }
}