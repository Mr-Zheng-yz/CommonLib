package com.baize.common_lib.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.baize.common_lib.databinding.FragmentDashboardBinding
import com.ocamara.common_libs.utils.FileLogger
import com.ocamara.common_libs.utils.LogUtil

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.btnCInitLog.setOnClickListener {
            FileLogger.getInstance().init(context)
        }

        binding.btnCLearLog.setOnClickListener {
            FileLogger.getInstance().cleanOldLogs(context)
        }

        binding.btnLog.setOnClickListener {
            repeat(10) {
                Thread {
                    repeat(10) { count ->
                        LogUtil.wi(">>>>>>>>>>>>>>>>>>>>>>>> 日志测试写入 count:" + count + "${Thread.currentThread().name} <<<<<<<<<<<<<<<<<<<<<")
                    }
                }.start()
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}