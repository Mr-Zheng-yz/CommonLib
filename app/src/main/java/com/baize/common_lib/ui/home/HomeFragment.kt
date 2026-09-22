package com.baize.common_lib.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.baize.common_lib.RabbitMQActivity
import com.baize.common_lib.databinding.FragmentHomeBinding
import com.ocamara.common_libs.dialog.LoadingDialogFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.btnRabbitMQ.setOnClickListener { startActivity(Intent(context, RabbitMQActivity::class.java)) }
        binding.btnLoading.setOnClickListener {
            LoadingDialogFragment.showLoading(childFragmentManager, "测试中...")
            LoadingDialogFragment.hideLoading(childFragmentManager)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}